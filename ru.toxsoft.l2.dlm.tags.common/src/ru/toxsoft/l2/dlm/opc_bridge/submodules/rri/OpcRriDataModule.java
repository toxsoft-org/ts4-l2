package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.skf.legacy.*;
import org.toxsoft.skf.rri.lib.*;
import org.toxsoft.skf.rri.lib.impl.*;
import org.toxsoft.uskat.concurrent.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.CommandsModule.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Модуль работы с RRI данными.
 *
 * @author dima
 */
public class OpcRriDataModule
    extends ConfigurableWorkerModuleBase
    implements ISkRriSectionListener, ISkCommandExecutor {

  /**
   * Журнал работы
   */
  ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст..
   */
  IDlmContext context;

  /**
   * Информация о модуле DLM
   */
  private IDlmInfo dlmInfo;

  /**
   * Инициализатор.
   */
  private IRriDataTransmittersInitializer initializer;

  /**
   * Набор описаний данных, полученный из конфиг информации.
   */
  private IList<IRriDataTransmitter> pinRriDataTransmitters;

  /**
   * Описание команд в терминах сервиса команд, обрабатываемых данным исполнителем.
   */
  private IGwidList commandsDef;

  /**
   * Определение команд из конфигурации
   */
  private IList<ProcessedCommandsDefByObjNames> commandsDefByObjNames;

  /**
   * Очередь команд, пришедших на обработку.
   */
  private IQueue<IDtoCommand> commandsQueue;

  /**
   * Конструктор по DLM контексту
   *
   * @param aContext {@link IDlmContext} - контекст.
   * @param aDlmInfo {@link IDlmInfo} - информация о DLM
   * @param aInitializer {@link IRriDataTransmittersInitializer} - инициализатор пинов.
   */
  public OpcRriDataModule( IDlmContext aContext, IDlmInfo aDlmInfo, IRriDataTransmittersInitializer aInitializer ) {
    dlmInfo = aDlmInfo;
    context = aContext;
    initializer = aInitializer;
  }

  @Override
  protected void doConfigYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {

    IAvTree rriDefs = aConfig.params().nodes().findByKey( RRI_DEFS );

    // наполнение конфигуратора данными (для данных НСИ)
    if( rriDefs != null && rriDefs.isArray() ) {
      for( int i = 0; i < rriDefs.arrayLength(); i++ ) {
        // описание одного НСИ даннного
        IAvTree oneDataDef = rriDefs.arrayElement( i );

        initializer.addDataConfigParamsForTransmitter( oneDataDef );
      }
    }
    // создание по конфигурации описаний для регистрации в сервисе
    IAvTree cmdClassDefs = aConfig.params().nodes().findByKey( RRI_CMD_CLASS_DEFS );
    commandsDefByObjNames = CommandsModule.createCmdDefs( cmdClassDefs );
  }

  @Override
  protected void doStartComponent() {

    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_RRI_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    // регистрируем службу НСИ
    S5SynchronizedRegRefInfoService rriService =
        new S5SynchronizedRegRefInfoService( (S5SynchronizedConnection)context.network().getSkConnection() );
    logger.info( "%s", rriService );

    // инициализирует с помощью конфигуратора основные сущности (на данном этапе идёт выборка информации с сервера)
    initializer.initialize( context );

    // получение объектов, отвечающих за передачу сигнала с единичного пина на единичное данное
    pinRriDataTransmitters = initializer.getDataTransmitters();
    logger.debug( "PinRriDataTransmitters: %s ", String.valueOf( pinRriDataTransmitters.size() ) );
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      // подписываемся под изменения в своих секциях НСИ
      for( ISkRriSection section : transmitter.gwid2Section().values() ) {
        // NB многократная подписка под одно и тоже игнорируется в самом section.eventer().addListener
        section.eventer().addListener( this );
      }
      if( transmitter instanceof OneToOneRriDataTransmitter ) {
        ITag tag = ((OneToOneRriDataTransmitter)transmitter).getTag();
        IRriSetter dataSetter = ((OneToOneRriDataTransmitter)transmitter).getRriSetter();
        logger.debug( "Tag: %s, OneToOneRriDataTransmitter: %s", tag.id(), dataSetter.toString() );
      }
    }

    // инициализация работы с командами
    // создание синхронизованной очереди получаемых команд команд
    commandsQueue = new SynchronizedQueueWrapper<>( new Queue<>() );

    // Опеределения для регистрации исполнителя
    GwidList convertedCommandsDef = new GwidList();

    // обращение к серверу с целью конвертации имён в коды
    for( ProcessedCommandsDefByObjNames cmdDefByObjName : commandsDefByObjNames ) {
      IList<Gwid> cmdDef = cmdDefByObjName.convert();
      convertedCommandsDef.addAll( cmdDef );
    }

    commandsDef = convertedCommandsDef;

    for( Gwid gd : commandsDef ) {
      logger.info( "*** Handler registered on RRI Module command: %s", gd );
    }

    // регистрация модуля в качестве исполнителя команд
    context.network().getSkConnection().coreApi().cmdService().registerExecutor( this, commandsDef );

    logger.info( MSG_RRI_DATA_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );
  }

  @Override
  protected void doDoJob() {

    // текущее время - чтоб у всех данных было одно время
    long currTime = System.currentTimeMillis();

    boolean doRriWrite = false;
    // выполнение работы с каждым передатчиком с проверкой изменения значений НСИ
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      try {
        doRriWrite |= transmitter.transmit( currTime );
      }
      catch( Exception e ) {
        logger.error( e.getMessage() );
      }
    }
    if( doRriWrite ) {
      // nop оставлено на случай если потребуется принудительно записать новые НСИ значения
    }
    // обрабатываем полученные команды
    IDtoCommand cmd = commandsQueue.getHeadOrNull();

    if( cmd != null ) {
      String cmdId = cmd.cmdGwid().propId();
      if( cmdId.compareTo( RRI_OPC_2_USKAT_CMD_ID ) == 0 ) {
        transferRriOPC2Uskat();
        setCmdState( cmd, MSG_COMMAND_COMPLETE_RRI_MODULE, ESkCommandState.SUCCESS );
      }
      if( cmdId.compareTo( RRI_USKAT_2_OPC_CMD_ID ) == 0 ) {
        transferRriUskat2OPC();
        setCmdState( cmd, MSG_COMMAND_COMPLETE_RRI_MODULE, ESkCommandState.SUCCESS );
      }
      // последним идет проверка на то что обработка подписаной команды сделана
      if( cmdId.compareTo( RRI_OPC_2_USKAT_CMD_ID ) != 0 && cmdId.compareTo( RRI_USKAT_2_OPC_CMD_ID ) != 0 ) {
        setCmdState( cmd, MSG_COMMAND_UNDER_DEVELOPMENT_RRI_MODULE, ESkCommandState.FAILED );
      }
    }

  }

  private void transferRriUskat2OPC() {
    // Читаем с USkat сервера и пишем в OPC
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      // читаем из своей секции и пишем в свой node
      IMap<Gwid, ISkRriSection> gwid2Section = transmitter.gwid2Section();
      for( Gwid rriGwid : gwid2Section.keys() ) {
        ISkRriSection section = gwid2Section.getByKey( rriGwid );
        IAtomicValue rriVal = section.getAttrParamValue( rriGwid.skid(), rriGwid.propId() );
        transmitter.writeBack2OpcNode( rriGwid, rriVal );
      }
    }
  }

  private void transferRriOPC2Uskat() {
    // Читаем с OPC сервера и пишем в USkat
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      // читаем из своей секции и пишем в свой node
      transmitter.transmitAnyWay();
    }
  }

  @Override
  protected boolean doQueryStop() {
    // отписываемся от нотификаций
    pinRriDataTransmitters = initializer.getDataTransmitters();
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      // подписываемся по изменения в своих секциях НСИ
      for( ISkRriSection section : transmitter.gwid2Section().values() ) {
        section.eventer().removeListener( this );
      }
    }

    return true;
  }

  @Override
  public void onSectionPropsChanged( ISkRriSection aSource ) {
    // nop
  }

  @Override
  public void onClassParamInfosChanged( ISkRriSection aSource, String aClassId ) {
    // nop
  }

  @Override
  public void onParamValuesChanged( ISkRriSection aSource, IList<SkEvent> aEvents ) {
    // находим свой transmitter и пишем в него новое значение
    pinRriDataTransmitters = initializer.getDataTransmitters();
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      for( ISkRriSection section : transmitter.gwid2Section().values() ) {
        if( section.equals( aSource ) ) {
          for( SkEvent event : aEvents ) {
            Gwid parGwid = event.eventGwid();
            if( transmitter.gwid2Section().hasKey( parGwid ) ) {
              IAtomicValue newVal = event.paramValues().findByKey( ISkRegRefServiceHardConstants.EVPRMID_NEW_VAL_ATTR );
              transmitter.writeBack2OpcNode( parGwid, newVal );
            }
          }
        }
      }
    }
  }

  @Override
  public void executeCommand( IDtoCommand aCmd ) {
    commandsQueue.putTail( aCmd );
    // change cmd state - EXECUTING
    setCmdState( aCmd, MSG_COMMAND_COME_FOR_RRI_MODULE, ESkCommandState.EXECUTING );
    logger.debug( "Get command %s, and put into queue ", aCmd.instanceId() ); //$NON-NLS-1$

  }

  private void setCmdState( IDtoCommand aCmd, String aMsg, ESkCommandState aNewState ) {
    // Изменяем состояние команды
    ValidationResult vr = ValidationResult.info( aMsg, aCmd.instanceId() );
    ValResList result = new ValResList();
    result.add( vr );

    SkCommandState state = new SkCommandState( System.currentTimeMillis(), aNewState );

    changeCommandState( aCmd.instanceId(), state );
  }

  private void changeCommandState( String aExecCmdId, SkCommandState aCmdState ) {

    DtoCommandStateChangeInfo cmdStateChangeInfo = new DtoCommandStateChangeInfo( aExecCmdId, aCmdState );

    try {
      context.network().getSkConnection().coreApi().cmdService().changeCommandState( cmdStateChangeInfo );
      logger.debug( MSG_COMMAND_STATE_CHANGED_FOR_RRI_MODULE, aExecCmdId, aCmdState.state().id() );
    }
    catch( Exception e ) {
      logger.error( MSG_COMMAND_STATE_CANT_CHANGE_FOR_RRI_MODULE, aExecCmdId, e.getMessage() );
    }
  }

}

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
import ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.IStatusRriMonitor.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Модуль работы с RRI данными.
 *
 * @author dima
 */
public class OpcRriDataModule
    extends ConfigurableWorkerModuleBase
    implements ISkCommandExecutor, ISkEventHandler {

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
   * Монитор статуса НСИ контроллера.
   */
  private IStatusRriMonitor statusRriMonitor = new StatusRriMonitor();

  /**
   * индекс текущего передатчика в процессе USkat -> OPC UA
   */
  private int currTransmitterIndex;

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
    // читаем описание конфигурации самого модуля
    statusRriMonitor.config( rriDefs );

    // IAtomicValue statusReadTag = AvUtils.avStr( "status.rri.read.tag.id" );
    //
    // IOptionSet rriCommonParams = rriDefs.fields();
    // if( rriCommonParams.hasValue( "status.rri.read.tag.id" ) ) {
    // statusReadTag = rriCommonParams.getValue( "status.rri.read.tag.id" );
    // }
    // System.out.print( statusReadTag.asString() );

    IAvTree rriNodes = rriDefs.nodes().findByKey( RRI_NODES );

    // наполнение конфигуратора данными (для данных НСИ)
    if( rriNodes != null && rriNodes.isArray() ) {
      for( int i = 0; i < rriNodes.arrayLength(); i++ ) {
        // описание одного НСИ даннного
        IAvTree oneRriAttrDef = rriNodes.arrayElement( i );

        initializer.addDataConfigParamsForTransmitter( oneRriAttrDef );
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
    // запускаем монитор статуса состояния НСИ контроллера
    statusRriMonitor.start( context );

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
    // регистрируемся слушателем событий изменения значений НСИ
    context.network().getSkConnection().coreApi().eventService()
        // work version
        // new GwidList( Gwid.createEvent( "sk.service.sysext.regref.Section", "rri.section.id", "RriParamsChange" )
        .registerHandler( new GwidList( Gwid.createEvent( ISkRriServiceHardConstants.CLASSID_RRI_SECTION,
            ISkRriServiceHardConstants.EVID_RRI_PARAM_CHANGE ) ), this );

    logger.info( MSG_RRI_DATA_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );
  }

  @Override
  protected void doDoJob() {

    // текущее время - чтоб у всех данных было одно время
    long currTime = System.currentTimeMillis();
    // получаем текущий статус блока НСИ контроллера
    ERriControllerState controllerRriState = statusRriMonitor.getState();
    switch( controllerRriState ) {
      case NEED_DOWNLOAD_USKAT_RRI: {
        // контроллер сигнализирует "залейте НСИ с сервера USkat"
        // запускаем процесс передачи
        currTransmitterIndex = 0;
        IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
        transmitter.transmitUskat2OPC();
      }
        break;
      case RRI_CONTROLLER_OK:
        routine( currTime );
        break;
      case UNKNOWN:
        break;
      case USKAT_RRI_LOADING: {
        // мы в стадии передачи значений с USkat сервера на OPC UA
        // проверяем состояние передачи
        IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
        IComplexTag.EComplexTagState transferState = transmitter.getOpcCmdState();
        switch( transferState ) {
          case DONE:
            // все записалось успешно
            ++currTransmitterIndex;
            if( currTransmitterIndex >= pinRriDataTransmitters.size() ) {
              // все записали, гасим флаг "контроллеру нужен НСИ сверху"
              statusRriMonitor.setStatus( Integer.valueOf( 1 ) );
              // переходим в режим нормальной работы
            }
            else {
              // пишем следующий параметр НСИ
              transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
              transmitter.transmitUskat2OPC();
            }

            break;
          case ERROR:
            // произошла ошибка записи, повторяем
            transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
            transmitter.transmitUskat2OPC();
            break;
          case PROCESS:
            // запись в процессе выполнения, ничего не делаем, ждем следующего цикла
            // nop
            break;
          case TIMEOUT:
            break;
          case UNKNOWN:
            break;
          default:
            break;
        }
      }
        break;
      default:
        break;
    }

    // обрабатываем полученные команды
    IDtoCommand cmd = commandsQueue.getHeadOrNull();

    if( cmd != null ) {
      String cmdId = cmd.cmdGwid().propId();
      if( cmdId.compareTo( RRI_USKAT_2_OPC_CMD_ID ) == 0 ) {
        transferRriUskat2OPC();
        setCmdState( cmd, MSG_COMMAND_COMPLETE_RRI_MODULE, ESkCommandState.SUCCESS );
      }
      // последним идет проверка на то что обработка подписаной команды сделана
      if( cmdId.compareTo( RRI_USKAT_2_OPC_CMD_ID ) != 0 ) {
        setCmdState( cmd, MSG_COMMAND_UNDER_DEVELOPMENT_RRI_MODULE, ESkCommandState.FAILED );
      }
    }

  }

  /**
   * Рутина - слушаем изменения тегов и передаем их на сервер USkat
   *
   * @param currTime
   */
  public void routine( long currTime ) {
    // выполнение работы с каждым передатчиком с проверкой изменения значений НСИ
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      try {
        transmitter.transmit( currTime );
      }
      catch( Exception e ) {
        logger.error( e.getMessage() );
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
        // TODO написать код установки своего значения НСИ в контроллер
        // transmitter.writeBack2OpcNode( rriGwid, rriVal );
      }
    }
  }

  @Override
  protected boolean doQueryStop() {
    // отписываемся от всего под что подписались
    context.network().getSkConnection().coreApi().cmdService().unregisterExecutor( this );
    context.network().getSkConnection().coreApi().eventService().unregisterHandler( this );
    return true;
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

  @Override
  public void onEvents( ISkEventList aEvents ) {
    // TODO тут проверяем что это наши события
    pinRriDataTransmitters = initializer.getDataTransmitters();
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      // for( ISkRriSection section : transmitter.gwid2Section().values() ) {
      // if( section.equals( aSource ) ) {
      for( SkEvent event : aEvents ) {
        Gwid parGwid = event.paramValues().findByKey( ISkRriServiceHardConstants.EVPRMID_PARAM_GWID ).asValobj();
        if( transmitter.gwid2Section().hasKey( parGwid ) ) {
          IAtomicValue newVal = event.paramValues().findByKey( ISkRriServiceHardConstants.EVPRMID_NEW_VAL_ATTR );
          System.out.printf( "New val: %s", newVal.asString() );
          // FIXME реализация передачи на контроллер новго значения НСИ
          // transmitter.writeBack2OpcNode( parGwid, newVal );
        }
        // }
        // }
      }
    }

  }

}

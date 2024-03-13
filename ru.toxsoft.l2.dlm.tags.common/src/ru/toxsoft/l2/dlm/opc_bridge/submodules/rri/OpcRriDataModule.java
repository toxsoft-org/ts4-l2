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
import org.toxsoft.skf.rri.lib.impl.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.CommandsModule.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
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
   * Журнал работы.
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
   * Очередь команд пришедших на обработку.
   */
  private IQueue<IDtoCommand> commandsQueue;

  /**
   * Очередь событий изменения значений НСИ требующих обработки.
   */
  private IQueue<SkEvent> eventsQueue;

  /**
   * Монитор статуса НСИ контроллера.
   */
  private IStatusRriMonitor statusRriMonitor = new StatusRriMonitor();

  /**
   * контейнер комплексных тегов
   */
  private final IComplexTagsContainer complexTagsContainer;

  /**
   * Конструктор по DLM контексту
   *
   * @param aContext {@link IDlmContext} - контекст.
   * @param aDlmInfo {@link IDlmInfo} - информация о DLM
   * @param aInitializer {@link IRriDataTransmittersInitializer} - инициализатор пинов.
   * @param aComplexTagsContainer IComplexTagsContainer - контейнер сложных тегов.
   */
  public OpcRriDataModule( IDlmContext aContext, IDlmInfo aDlmInfo, IRriDataTransmittersInitializer aInitializer,
      IComplexTagsContainer aComplexTagsContainer ) {
    dlmInfo = aDlmInfo;
    context = aContext;
    initializer = aInitializer;
    complexTagsContainer = aComplexTagsContainer;
  }

  @Override
  protected void doConfigYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {

    IAvTree rriDefs = aConfig.params().nodes().findByKey( RRI_DEFS );
    // читаем описание конфигурации самого модуля
    statusRriMonitor.config( rriDefs );
    // далее имеет смысл работать только если
    if( statusRriMonitor.isConfigured() ) {
      IAvTree rriNodes = rriDefs.nodes().findByKey( RRI_NODES );

      // наполнение конфигуратора данными (для данных НСИ)
      if( rriNodes != null && rriNodes.isArray() ) {
        for( int i = 0; i < rriNodes.arrayLength(); i++ ) {
          // описание одного НСИ даннного
          IAvTree oneRriAttrDef = rriNodes.arrayElement( i );

          initializer.addDataConfigParamsForTransmitter( oneRriAttrDef, complexTagsContainer );
        }
      }
      // создание по конфигурации описаний для регистрации в сервисе
      IAvTree cmdClassDefs = aConfig.params().nodes().findByKey( RRI_CMD_CLASS_DEFS );
      commandsDefByObjNames = CommandsModule.createCmdDefs( cmdClassDefs );
    }
  }

  @Override
  protected void doStartComponent() {

    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_RRI_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    if( !statusRriMonitor.isConfigured() ) {
      return;
    }

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
    // запускаем монитор статуса состояния НСИ контроллера
    statusRriMonitor.start( context, complexTagsContainer, pinRriDataTransmitters );
    // инициализация работы с событиями
    eventsQueue = new SynchronizedQueueWrapper<>( new Queue<>() );

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
        .registerHandler( new GwidList( Gwid.createEvent( ISkRriServiceHardConstants.CLASSID_RRI_SECTION,
            ISkRriServiceHardConstants.EVID_RRI_PARAM_CHANGE ) ), this );

    logger.info( MSG_RRI_DATA_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );
  }

  @Override
  protected void doDoJob() {
    if( !statusRriMonitor.isConfigured() ) {
      return;
    }

    // текущее время - чтоб у всех данных было одно время
    long currTime = System.currentTimeMillis();
    // получаем текущий статус блока НСИ контроллера
    ERriControllerState controllerRriState = statusRriMonitor.getState();
    // for debug
    // ERriControllerState controllerRriState = ERriControllerState.RRI_CONTROLLER_OK;
    switch( controllerRriState ) {
      case NEED_DOWNLOAD_USKAT_RRI: {
        // контроллер сигнализирует "залейте НСИ с сервера USkat"
        // запускаем процесс передачи
        statusRriMonitor.startDownload();
      }
        break;
      case RRI_CONTROLLER_OK:
        routine( currTime );
        break;
      case UNKNOWN:
        break;
      case USKAT_RRI_LOADING:
        // мы в стадии передачи значений с USkat сервера на OPC UA
        statusRriMonitor.processDownload();
        break;
      default:
        break;
    }
    // обрабатываем полученные события
    processNextEvent();
    // обрабатываем полученные команды
    processNextCommand();

  }

  private void processNextCommand() {
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

  private void processNextEvent() {
    SkEvent event = eventsQueue.getHeadOrNull();

    if( event != null ) {
      pinRriDataTransmitters = initializer.getDataTransmitters();
      for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
        Gwid parGwid = event.paramValues().findByKey( ISkRriServiceHardConstants.EVPRMID_PARAM_GWID ).asValobj();
        // тут проверяем что это наши события
        if( transmitter.gwid2Section().hasKey( parGwid ) ) {
          transmitter.transmitUskat2OPC();
        }
      }
    }
  }

  /**
   * Рутина - слушаем изменения тегов и передаем их на сервер USkat
   *
   * @param aCurrTime текущее время
   */
  public void routine( long aCurrTime ) {
    // выполнение работы с каждым передатчиком с проверкой изменения значений НСИ
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      try {
        transmitter.transmit( aCurrTime );
      }
      catch( Exception e ) {
        logger.error( e.getMessage() );
      }
    }
  }

  private void transferRriUskat2OPC() {
    // Читаем с USkat сервера и пишем в OPC
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      transmitter.transmitUskat2OPC();
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
    // просто помещаем в буфер, обрабатывать будем в doJob
    for( SkEvent event : aEvents ) {
      eventsQueue.putTail( event );
      Gwid parGwid = event.paramValues().findByKey( ISkRriServiceHardConstants.EVPRMID_PARAM_GWID ).asValobj();
      IAtomicValue oldVal = event.paramValues().findByKey( ISkRriServiceHardConstants.EVPRMID_OLD_VAL_ATTR );
      IAtomicValue newVal = event.paramValues().findByKey( ISkRriServiceHardConstants.EVPRMID_NEW_VAL_ATTR );
      logger.debug( "Event param %s change. oldVal = %s newVal = %s", parGwid.asString(), oldVal.asString(), //$NON-NLS-1$
          newVal.asString() );
    }
  }

}

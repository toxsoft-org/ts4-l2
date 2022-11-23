package ru.toxsoft.l2.core.reserve.impl;

import static ru.toxsoft.l2.core.reserve.IReserveHardConstants.*;
import static ru.toxsoft.l2.core.reserve.impl.IL2Resources.*;
import static ru.toxsoft.l2.core.reserve.impl.IReserveParams.*;
import static ru.toxsoft.l2.sysdescr.constants.IL2CoreSysdescrConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

//import org.toxsoft.core.tslib.coll.derivative.

/**
 * Реализация слушателя состояния соединения с сервером и команд с сервера
 *
 * @author max
 */
public class NetworkListener
    extends WorkerComponentBase
    implements INetworkListener, ISkCommandExecutor {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Глобальный контекст.
   */
  private GlobalContext globalContext;

  /**
   * Соединение с сервером.
   */
  private ISkConnection connection;

  /**
   * Параметры настройки резервирования.
   */
  private IOptionSet reserveOps;

  /**
   * Определение команд резервирования.
   */
  private IGwidList reserveCommandsDef;

  /**
   * Гвид шкафа резервирования
   */
  private Gwid boxGwid;

  /**
   * Очередь команд резервирования.
   */
  private IQueue<ReserveCommandOnExecuting> commandsQueue;

  /**
   * Признак наличия связи с сервером.
   */
  private boolean connectedToServer = false;

  /**
   * Интервал между повторнымипопытками регистрации обработчиков команд - настраиваемый параметр.
   */
  private long cmdReregInterval = 3000L;

  /**
   * Конструктор по глобальному контексту и настроечным параметрам
   *
   * @param aGlobalContext GlobalContext - глобальный контекст.
   * @param aReserveOps IOptionSet - настроечные параметры.
   */
  public NetworkListener( GlobalContext aGlobalContext, IOptionSet aReserveOps ) {
    globalContext = aGlobalContext;
    reserveOps = aReserveOps;
    cmdReregInterval = OP_RSRV_CMD_REREG_INTERVAL.getValue( reserveOps ).asLong();
  }

  @Override
  protected void doStartComponent() {
    commandsQueue = new SynchronizedQueueWrapper<>( new Queue<>() );// DerivativeUtils.createSynchronizedQueue();
    connection = globalContext.network().getSkConnection();

    // из сгенерированного skide файла
    String boxClassId = CLSID_L2_HOTSWAP;

    // из настроек
    String boxObjName = reserveOps.getStr( L2_RESERVE_BOX_OBJ_NAME );

    boxGwid = Gwid.create( boxClassId, boxObjName, null, null, null, null );
    initReserveCommandsListener();
  }

  //
  // -----------------------------------------------------------------------
  // Работа с командами

  /**
   * Инициализирует обработчика команд резервирования НУ
   */
  private void initReserveCommandsListener() {
    reserveCommandsDef = createReserveCommansDef();

    // количество попыток зарегать обработчики команд
    int regTriesCount = 1000;

    for( int i = 0; i < regTriesCount; i++ ) {
      logger.debug( "Reserve commands registration try number %d from %d", Integer.valueOf( i + 1 ), //$NON-NLS-1$
          Integer.valueOf( regTriesCount ) );

      try {
        // test
        // if( i < 8 ) {
        // throw new ArithmeticException( "Fore Test" );
        // }
        // end test

        connection.coreApi().cmdService().registerExecutor( this, reserveCommandsDef );
        logger.info( "RESERVE COMMANDS REGISTERED" ); //$NON-NLS-1$
        return;
      }
      catch( Exception e ) {
        logger.debug( "Reserve commands registration error: %s", e.getMessage() ); //$NON-NLS-1$
      }
      // перерыв перед следующей попыткой регистрации
      try {
        Thread.sleep( cmdReregInterval );
      }
      catch( InterruptedException e ) {
        logger.error( e );
      }
    }

    logger.info( "RESERVE COMMANDS HAVE NOT BEEN REGISTERED" ); //$NON-NLS-1$
  }

  /**
   * Создаёт определение команды завершения работы нижнего уровня, в случае ошибки возвращается пустой список.
   *
   * @return IGwidList - набор определений.
   */
  private IGwidList createReserveCommansDef() {
    GwidList result = new GwidList();
    try {
      // из сгенерированного skide файла
      String boxClassId = CLSID_L2_HOTSWAP;

      // из настроек
      String boxObjName = reserveOps.getStr( L2_RESERVE_BOX_OBJ_NAME );

      Gwid cmdMainGwid = Gwid.createCmd( boxClassId, boxObjName, EReserveCommand.CDM_MAIN.id() );
      result.add( cmdMainGwid );

      Gwid cmdReserveGwid = Gwid.createCmd( boxClassId, boxObjName, EReserveCommand.CMD_RESERVE.id() );
      result.add( cmdReserveGwid );

      Gwid cmdMainInvalidGwid = Gwid.createCmd( boxClassId, boxObjName, EReserveCommand.CMD_MAIN_INVALID.id() );
      result.add( cmdMainInvalidGwid );
    }
    catch( TsItemNotFoundRtException e ) {
      logger.error( e, ERR_MSG_CFG_FILE_DOES_NOT_HAVE_SLAVE_LEADER_CMDS_PARAM );
    }
    catch( TsIllegalStateRtException e1 ) {
      logger.error( e1, ERR_MSG_SLAVE_LEADER_CMDS_INIT_FAILED );
    }
    return result;
  }

  @Override
  public void doJob() {
    connectedToServer = connection.state() == ESkConnState.ACTIVE;
  }

  @Override
  public boolean isConnectionBreak() {
    return !connectedToServer;
  }

  @Override
  public boolean isCommandReceived() {
    return commandsQueue.size() > 0;
  }

  @Override
  public ReserveCommandOnExecuting getReceivedCommand() {
    // Dima, 24.03.20
    // return commandsQueue.getHead();
    return commandsQueue.getHeadOrNull();
  }

  @Override
  public void executeCommand( IDtoCommand aCmd ) {
    EReserveCommand receivedCmd = EReserveCommand.findById( aCmd.cmdGwid().propId() );
    if( receivedCmd == EReserveCommand.UNKNOWN ) {
      return;
    }
    commandsQueue.putTail( new ReserveCommandOnExecuting( aCmd.instanceId(), receivedCmd ) );
    DtoCommandStateChangeInfo cmdStateChangeInfo =
        new DtoCommandStateChangeInfo( aCmd.instanceId(), new SkCommandState( System.currentTimeMillis(),
            ESkCommandState.EXECUTING, "Command has been received and come for executing", boxGwid ) ); //$NON-NLS-1$
    try {
      connection.coreApi().cmdService().changeCommandState( cmdStateChangeInfo );
      logger.debug( "Command %s : %s has been received and come for executing", aCmd.cmdGwid().propId(), //$NON-NLS-1$
          aCmd.instanceId() );
    }
    catch( Exception e ) {
      logger.error( "Cant change command state: %s", e.getMessage() ); //$NON-NLS-1$
    }
  }

  @Override
  public void commandHasBeenDone( ReserveCommandOnExecuting aCommand ) {
    DtoCommandStateChangeInfo cmdStateChangeInfo =
        new DtoCommandStateChangeInfo( aCommand.getCmdId(), new SkCommandState( System.currentTimeMillis(),
            ESkCommandState.SUCCESS, "Command has been handled", boxGwid ) ); //$NON-NLS-1$
    try {
      connection.coreApi().cmdService().changeCommandState( cmdStateChangeInfo );
      logger.debug( "Command %s has been handled", aCommand.getCmdId() ); //$NON-NLS-1$
    }
    catch( Exception e ) {
      logger.error( "Cant change command state: %s", e.getMessage() ); //$NON-NLS-1$
    }
  }

}

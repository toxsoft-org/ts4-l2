package ru.toxsoft.l2.core.net.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.core.net.impl.IL2Resources.*;
import static ru.toxsoft.l2.core.net.impl.INetworkConstants.*;

import java.io.File;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.ctx.ITsContext;
import org.toxsoft.core.tslib.bricks.ctx.impl.TsContext;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.ValResList;
import org.toxsoft.core.tslib.coll.primtypes.IIntList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.connection.ESkConnState;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.ISkCoreConfigConstants;
import org.toxsoft.uskat.core.impl.SkCoreUtils;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.client.remote.S5RemoteBackendProvider;
import org.toxsoft.uskat.s5.common.S5Host;
import org.toxsoft.uskat.s5.common.S5HostList;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

import ru.toxsoft.l2.core.main.IL2HardConstants;
import ru.toxsoft.l2.core.main.IProgramQuitCommand;
import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.net.INetworkComponent;

import core.tslib.bricks.synchronize.TsThreadExecutor;

/**
 * Реализация слоя работы с сетью, в частности с S3 сервером.
 *
 * @author max
 */
public class NetworkImpl
    extends AbstractL2Component
    implements INetworkComponent, ISkCommandExecutor {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Глобальный контекст.
   */
  // private IGlobalContext globalContext;

  /**
   * Соединение с сервером Sk.
   */
  private ISkConnection connection;

  /**
   * Параметры настройки NETa.
   */
  private final OptionSet netOps;

  /**
   * Определение команды завершения работы
   */
  private IGwidList quitCommandDef;

  /**
   * редактор состояния команды
   */
  // private ICommandStateEditor editor;

  /**
   * Команда завершения работы НУ
   */
  private IProgramQuitCommand programQuitCommand = null;

  /**
   * Монитор доступа к команде завершения НУ.
   */
  private Object programQuitCommandLocker = new Object();

  /**
   * Разделитель потоков {@link ISkConnection}
   */
  // private SkBackendThreadSeparator skConnectionSeparator;

  /**
   * Конструктор реализации работы с сетью по глобальному контексту.
   *
   * @param aGlobalContext GlobalContext - глобальный контекст.
   */
  public NetworkImpl( GlobalContext aGlobalContext ) {
    super( aGlobalContext );
    // установка компонента работы с сетью (данного экземпляра) в глобальный контекст

    aGlobalContext.setNetwork( this );

    // globalContext = aGlobalContext;

    netOps = new OptionSet();// EHalOps.asOptionSet() );
    netOps.addAll( readConfigFile( new File( IL2HardConstants.L2_NET_CFG_FILE_NAME ) ) );

  }

  /**
   * Создаёт определение команды завершения работы нижнего уровня, в случае ошибки возвращается пустой список.
   *
   * @return IGwidList - набор определений.
   */
  private IGwidList createQuitCommansDef() {
    GwidList result = new GwidList();

    // проверка данных команды сервера на останов
    // если данные не заданы или пустые - выход

    if( !netOps.hasValue( CGF_PARAM_QUIT_CMD_CLASS_ID )
        || netOps.getStr( CGF_PARAM_QUIT_CMD_CLASS_ID ).trim().length() == 0
        || !netOps.hasValue( CGF_PARAM_QUIT_CMD_OBJ_NAME )
        || netOps.getStr( CGF_PARAM_QUIT_CMD_OBJ_NAME ).trim().length() == 0
        || !netOps.hasValue( CGF_PARAM_QUIT_CMD_ID ) || netOps.getStr( CGF_PARAM_QUIT_CMD_ID ).trim().length() == 0 ) {
      return result;
    }

    try {
      String quitCmdClassId = netOps.getStr( CGF_PARAM_QUIT_CMD_CLASS_ID );
      String quitCmdObjName = netOps.getStr( CGF_PARAM_QUIT_CMD_OBJ_NAME );
      String quitCmdId = netOps.getStr( CGF_PARAM_QUIT_CMD_ID );

      Gwid cmdMainGwid = Gwid.createCmd( quitCmdClassId, quitCmdObjName, quitCmdId );
      result.add( cmdMainGwid );
    }
    catch( TsItemNotFoundRtException e ) {
      logger.error( e, ERR_MSG_CFG_FILE_DOES_NOT_HAVE_QUIT_CMD_PARAM );
    }
    catch( TsIllegalStateRtException e1 ) {
      logger.error( e1, ERR_MSG_QUIT_CMD_INIT_FAILED );
    }
    return result;
  }

  @Override
  protected void processStart() {
    boolean startWithoutS5Server =
        netOps.hasKey( CGF_PARAM_START_WITHOUT_S5_SERVER ) ? netOps.getBool( CGF_PARAM_START_WITHOUT_S5_SERVER )
            : false;

    if( startWithoutS5Server ) {
      // тест без соединения с S5
      logger.info( "Start whithout server by cfg param 'startWithoutS5=true'" ); //$NON-NLS-1$
      return;
    }
    connection = SkCoreUtils.createConnection();

    String login = netOps.getStr( CGF_PARAM_LOGIN );
    String password = netOps.getStr( CGF_PARAM_PASSWORD );
    IStringList hostnames = new StringArrayList( netOps.getStr( CGF_PARAM_HOST ) );
    IIntList ports = new IntArrayList( netOps.getInt( CGF_PARAM_PORT ) );
    int connectTimeout = 3000;
    int failureTimeout = 120000;
    int currdataTimeout =
        netOps.hasKey( CGF_PARAM_CURR_DATA_TIMEOUT ) ? netOps.getInt( CGF_PARAM_CURR_DATA_TIMEOUT ) : 50;
    int histdataTimeout = 10000;

    // Создание соединения
    S5HostList hosts = new S5HostList();
    for( int index = 0, n = hostnames.size(); index < n; index++ ) {
      hosts.add( new S5Host( hostnames.get( index ), ports.getValue( index ) ) );
    }
    ITsContext ctx = new TsContext();
    ISkCoreConfigConstants.REFDEF_BACKEND_PROVIDER.setRef( ctx, new S5RemoteBackendProvider() );
    // TODO: main loop thread as param for TsThreadExecutor ???
    ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.setRef( ctx, new TsThreadExecutor() );
    IS5ConnectionParams.OP_USERNAME.setValue( ctx.params(), avStr( login ) );
    IS5ConnectionParams.OP_PASSWORD.setValue( ctx.params(), avStr( password ) );

    IS5ConnectionParams.OP_HOSTS.setValue( ctx.params(), avValobj( hosts ) );
    IS5ConnectionParams.OP_CLIENT_PROGRAM.setValue( ctx.params(), avStr( "l2" ) ); //$NON-NLS-1$
    IS5ConnectionParams.OP_CLIENT_VERSION.setValue( ctx.params(), avValobj( IS5ServerHardConstants.version ) );
    IS5ConnectionParams.OP_CONNECT_TIMEOUT.setValue( ctx.params(), avInt( connectTimeout ) );
    IS5ConnectionParams.OP_FAILURE_TIMEOUT.setValue( ctx.params(), avInt( failureTimeout ) );
    IS5ConnectionParams.OP_CURRDATA_TIMEOUT.setValue( ctx.params(), avInt( currdataTimeout ) );
    IS5ConnectionParams.OP_HISTDATA_TIMEOUT.setValue( ctx.params(), avInt( histdataTimeout ) );

    Thread connectionThread = new Thread( new ConnectionRunnable( ctx ) );

    connectionThread.start();
    try {
      connectionThread.join();
    }
    catch( InterruptedException e ) {
      logger.error( e );
    }

    // регистрация команды останова
    initQuitCommandListener();
  }

  /**
   * Инициализирует обработчика команды завершения работы НУ
   */
  private void initQuitCommandListener() {

    quitCommandDef = createQuitCommansDef();
    if( quitCommandDef.size() > 0 ) {

      // количество попыток зарегать обработчики команд
      int regTriesCount = 50;
      long cmdReregInterval = 3000;

      for( int i = 0; i < regTriesCount; i++ ) {
        logger.debug( "Quit command registration try number %d from %d", Integer.valueOf( i + 1 ), //$NON-NLS-1$
            Integer.valueOf( regTriesCount ) );

        try {
          Gwid quitCmdGwid = quitCommandDef.first();
          ISkClassInfo classInfo = getSkConnection().coreApi().sysdescr().findClassInfo( quitCmdGwid.classId() );
          if( classInfo == null ) {
            logger.debug( "QUIT COMMAND CLASS '%s' doesnt exist", quitCmdGwid.classId() ); //$NON-NLS-1$
            return;
          }
          if( !classInfo.cmds().list().hasKey( quitCmdGwid.propId() ) ) {
            logger.debug( "QUIT COMMAND ID '%s' doesnt exist", quitCmdGwid.propId() ); //$NON-NLS-1$
            return;
          }
          if( getSkConnection().coreApi().objService()
              .find( new Skid( quitCmdGwid.classId(), quitCmdGwid.strid() ) ) == null ) {
            logger.debug( "QUIT COMMAND OBJECT '%s' doesnt exist", quitCmdGwid.strid() ); //$NON-NLS-1$
            return;
          }
          getSkConnection().coreApi().cmdService().registerExecutor( this, quitCommandDef );
          logger.info( "QUIT COMMAND REGISTERED" ); //$NON-NLS-1$

          return;
        }
        catch( Exception e ) {
          logger.debug( "Quit command registration error: %s", e.getMessage() ); //$NON-NLS-1$
        }
        // перерыв перед следующей попыткой регистрации
        try {
          Thread.sleep( cmdReregInterval );
        }
        catch( InterruptedException e ) {
          logger.error( e );
        }
      }

      logger.info( "QUIT COMMAND HAS NOT BEEN REGISTERED" ); //$NON-NLS-1$
    }
  }

  @Override
  protected void processRunStep() {
    // skConnectionSeparator.doJob();
  }

  @Override
  protected synchronized boolean processStopQuery() {
    if( connection != null && connection.state() != ESkConnState.CLOSED ) {
      connection.coreApi().cmdService().unregisterExecutor( this );
      connection.close();
    }
    return processStopStep();
  }

  @Override
  protected synchronized boolean processStopStep() {
    return connection.state() == ESkConnState.CLOSED;
  }

  private synchronized void openConnection( ITsContext aCtx )
      throws RuntimeException {
    connection.open( aCtx );
  }

  //
  // -----------------------------------------------------------------
  // Методы компонентного интерфейса сети

  @Override
  public IProgramQuitCommand getQuitCommandIfAny() {
    synchronized (programQuitCommandLocker) {
      return programQuitCommand;
    }
  }

  //
  // ----------------------------------------------------------
  // Реализация интерфейса INetwork

  @Override
  public synchronized ISkConnection getSkConnection() {
    return connection;
  }

  /**
   * Считывает набор параметров из файла конфигурации.
   * <p>
   * Формат файла конфигурации соответствует {@link IDvWriter#writeTypeConstraints(IOptionSet)} и не допускает никаких
   * комментариев или других данных.
   * <p>
   * Метод не выбрасывает исключений, все исключения ловятся и логируются. В случае ошибки возвращает пустой список
   * параметров.
   *
   * @param aFile {@link File} - файл для чтения конфигурации
   * @return {@link IOptionSetEdit} - счтанный или пустой набор параметров
   */
  private IOptionSet readConfigFile( File aFile ) {
    try {
      // mvk: буферизированное чтение из текстового файла
      // ICharInputStream chIn = new CharInputStreamFile( aFile );
      // ICharInputStream chIn = loadCharInputStreamFromFile( aFile );

      // IStridReader sr = new StridReader( chIn );
      // IDvReader dr = new DvReader( sr );

      IOptionSet result = OptionSetKeeper.KEEPER.read( aFile );
      logger.info( INFO_MSG_CFG_FILE_READ, aFile.getAbsolutePath() );
      return result;
    }
    catch( Exception e ) {
      logger.warning( e, ERR_MSG_ERR_READING_CFG_FILE, aFile.getAbsolutePath() );
      return IOptionSet.NULL;
    }
  }

  //
  // -----------------------------------------------------------------------------------------------------------------------------
  // методы интерфейса ICommandExecutor

  @Override
  public void executeCommand( IDtoCommand aCmd ) {
    int retCode = 100;
    String msg = String.format( "Command '%s' from server", aCmd.cmdGwid().propId() ); //$NON-NLS-1$
    if( aCmd.argValues().size() == 2 ) {
      try {
        // retCode = aCmd.argValues().get( QUIT_CMD_RET_CODE_ARG_ID ).asInt();
        // msg = aCmd.argValues().get( QUIT_CMD_MSG_ARG_ID ).asString();
      }
      catch( TsItemNotFoundRtException e ) {
        logger.error( e, ERR_MSG_SHUTDOWN_L2_CMD_DOES_NOT_HAVE_PARAM_VALUE );
      }
    }
    else {
      logger.error( ERR_MSG_SHUTDOWN_L2_CMD_DOES_NOT_HAVE_PARAM_VALUE );
    }

    synchronized (programQuitCommandLocker) {
      programQuitCommand = new ProgramQuitCommand( retCode, msg );
      // Изменяем состояние команды

      ValResList result = new ValResList();
      result.add( ValidationResult.info( MSG_CLOSE_L2_COMMAND_EXEC ) );

      DtoCommandStateChangeInfo cmdStateChangeInfo = new DtoCommandStateChangeInfo( aCmd.instanceId(),
          new SkCommandState( System.currentTimeMillis(), ESkCommandState.EXECUTING ) );
      try {
        connection.coreApi().cmdService().changeCommandState( cmdStateChangeInfo );
      }
      catch( Exception e ) {
        logger.error( "Cant change command state: %s", e.getMessage() ); //$NON-NLS-1$
      }
    }

  }

  /**
   * Класс соединения с серваком
   *
   * @author max
   */
  class ConnectionRunnable
      implements Runnable {

    /**
     * Журнал работы
     */
    private ILogger connectionThreadlogger = LoggerWrapper.getLogger( ConnectionRunnable.this.getClass().getName() );

    private ITsContext ctx;

    /**
     * Конструктор по контексту.
     *
     * @param aCtx
     */
    public ConnectionRunnable( ITsContext aCtx ) {
      ctx = aCtx;
    }

    @Override
    public void run() {
      int attemptCount = 100;
      for( int i = 0; i < attemptCount; i++ ) {
        connectionThreadlogger.info( "Connection attempt number %d (from %d)", Integer.valueOf( i + 1 ), //$NON-NLS-1$
            Integer.valueOf( attemptCount ) );
        try {
          openConnection( ctx );
          connectionThreadlogger.info( "Connection established" ); //$NON-NLS-1$
          return;
        }
        catch( Exception ex ) {
          connectionThreadlogger.error( ex, ERR_MSG_S5_CONNECTION_IS_NOT_ESTEBLISHED );
        }
        try {
          Thread.sleep( 2000L );
        }
        catch( InterruptedException e ) {
          connectionThreadlogger.error( e );
        }
      }
    }

  }

}

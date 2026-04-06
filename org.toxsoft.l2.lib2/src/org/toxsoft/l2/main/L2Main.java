package org.toxsoft.l2.main;

import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;
import static org.toxsoft.l2.main.IL2MainConstants.*;
import static org.toxsoft.l2.main.l10n.IL2MainSharedResources.*;

import java.io.*;
import java.lang.reflect.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.misc.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.files.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.core.tslib.utils.progargs.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.metainf.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * L2 application starter, contains {@link #main(String[])} method.
 *
 * @author hazard157
 */
public class L2Main {

  // TODO set Ctrl+C handler
  // TODO try several times to open SkConnection

  // TODO change to new getLogger()
  private static final ILogger logger = LoggerWrapper.getLogger( L2Main.class.getName() );

  /**
   * Text console is initialized only if {@link IL2MainConstants#OPDEF_USE_TEXT_CONSOLE} is <code>true</code>.
   */
  private static L2MainTextConsole textConsole = null;

  /**
   * Guards main thread in {@link #runApplication(IOptionSet)}.
   * <p>
   * This is a thread separator passed to all components of {@link L2Application} in the reference
   * {@link IL2ApplicationConstants#REFDEF_MAIN_THREAD_GUARD}. This guard is passed also to the {@link ISkConnection}
   * passed as {@link IL2ApplicationConstants#REFDEF_SK_CONNECTION}.
   */
  private static ITsThreadExecutor mainThreadGuard = null;

  /**
   * Application startup.
   *
   * @param aArgs String[] - command line arguments
   */
  public static void main( String[] aArgs ) {
    LoggerUtils.setLoggerFactory( LoggerWrapper::getLogger );
    sayHello();
    // create and validate global options from all sources
    IOptionSetEdit globalOps = prepareGlobalOptions( aArgs );
    ValidationResult vr = validateGlobalOps( globalOps );
    logResult( vr );
    if( vr.isError() ) {
      System.exit( ECODE_INIT_FAILED );
      return; // just for clear code - here stops the program
    }
    // setup L2Main
    setupL2Main( globalOps );
    // run application with restart if requested
    L2AppQuitCommand quitCmd;
    do {
      quitCmd = runApplication( globalOps );
    } while( quitCmd == null || quitCmd.exitCode() == ECODE_RESTART_L2APP );
    // finish the program
    sayGoodbye( quitCmd );
    Runtime.getRuntime().halt( quitCmd.exitCode() ); // halt() to avoid program hang because of crap threads
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static L2AppQuitCommand runApplication( IOptionSet aGlobalOps ) {
    // prepare thread guard
    mainThreadGuard = new TsThreadExecutor( L2Main.class.getSimpleName(), logger );
    // open SkConnection
    ISkConnection skConn;
    try {
      skConn = openConnection( aGlobalOps );
    }
    catch( Exception ex ) {
      logger.error( ex );
      return new L2AppQuitCommand( ECODE_CONN_OPEN_FAILED, ex.getMessage() );
    }
    // prepare L2Application arguments
    ITsContext l2AppArgs = new TsContext();
    l2AppArgs.params().setAll( aGlobalOps );
    REFDEF_MAIN_THREAD_GUARD.setRef( l2AppArgs, mainThreadGuard );
    REFDEF_SK_CONNECTION.setRef( l2AppArgs, skConn );
    // initialize application
    L2Application app = new L2AppImpl();
    ValidationResult vr = app.init( l2AppArgs );
    if( vr.isError() ) {
      logger.error( vr.message() );
      return new L2AppQuitCommand( ECODE_INIT_FAILED, vr.message() );
    }
    // start application
    try {
      app.start();
    }
    catch( Exception ex ) {
      logger.error( ex );
      return new L2AppQuitCommand( ECODE_START_FAILED, ex.getMessage() );
    }

    // run normal main loop until quit command
    L2AppQuitCommand quitCmd = null;
    while( quitCmd == null ) {
      app.doJob();
      quitCmd = app.getQuitCommandIfAny();
      if( quitCmd == null && textConsole != null ) {
        quitCmd = textConsole.getQuitCommandIfAny();
      }
      if( quitCmd != null ) {
        app.queryStop();
        // wait till stop or timeout
        long timeStopWasQueried = System.currentTimeMillis();
        int timeoutSecs = OPDEF_SHUTDOWN_TIMEOUT_SECS.getValue( l2AppArgs.params() ).asInt();
        long timeoutMsecs = 1000L * timeoutSecs;
        while( !app.isStopped() ) {
          app.doJob();
          // process timeout and change exit code
          if( System.currentTimeMillis() - timeStopWasQueried > timeoutMsecs ) {
            String msg = String.format( FMT_ERR_STOP_TIMEOUTED, HmsUtils.autoHms( timeoutSecs ) );
            quitCmd = new L2AppQuitCommand( ECODE_STOP_TIMEOUTED, msg );
            break;
          }
        }
      }
    }
    // clean-up (do we need timeout during clean-up?)
    app.destroy();
    closeConnection( skConn );
    if( textConsole != null ) {
      textConsole.close();
    }
    return quitCmd;
  }

  /**
   * Prepares arguments and opens USkat connection.
   *
   * @param aGlobalOps {@link IOptionSet} - global options
   * @return {@link ISkConnection} - the open connection
   * @throws TsRuntimeException on any error, non-TS exceptions are wrapped into {@link TsIoRtException}
   */
  private static ISkConnection openConnection( IOptionSet aGlobalOps ) {
    ITsContext connArgs = new TsContext();
    connArgs.params().addAll( aGlobalOps );
    // get backend provider
    ISkBackendProvider backendProvider = getSkBackendProvider( aGlobalOps );
    ISkCoreConfigConstants.REFDEF_BACKEND_PROVIDER.setRef( connArgs, backendProvider );
    ISkBackendMetaInfo metaInfo = backendProvider.getMetaInfo();
    // separate backend and L2 main threads
    ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.setRef( connArgs, mainThreadGuard );
    // validate arguments
    TsValidationFailedRtException.checkError( metaInfo.checkArguments( connArgs ) );
    // open the connection
    ISkConnection skConn = SkCoreUtils.createConnection();
    skConn.open( connArgs );
    return skConn;
  }

  private static ISkBackendProvider getSkBackendProvider( IOptionSet aGlobalOps ) {
    String className = OPDEF_BACKEND_PROVIDER_CLASS.getValue( aGlobalOps ).asString();
    try {
      Class<?> rawClass = Class.forName( className );
      if( !ISkBackendProvider.class.isAssignableFrom( rawClass ) ) {
        throw new TsIoRtException( FMT_ERR_INV_BACKEND_CLASS, className );
      }
      @SuppressWarnings( "unchecked" )
      Class<ISkBackendProvider> clazz = (Class<ISkBackendProvider>)rawClass;
      Constructor<ISkBackendProvider> constructor = clazz.getConstructor();
      return constructor.newInstance();
    }
    catch( TsRuntimeException ex ) {
      throw ex;
    }
    catch( Exception ex ) {

      // DEBUG ---
      ex.printStackTrace();
      // ---

      throw new TsIoRtException( ex );
    }
  }

  private static void closeConnection( ISkConnection aSkConn ) {
    aSkConn.close();
  }

  /**
   * Returns all parameters values read from all sources as one global option set.
   * <p>
   * All parameters means following options:
   * <ul>
   * <li>{@link L2Main} specific options listed in {@link IL2MainConstants#ALL_L2_MAIN_PARAMS};</li>
   * <li>{@link IL2Application} specific options listed in {@link IL2ApplicationConstants#ALL_L2_INIT_PARAMS};</li>
   * <li>Any other parameters specified by user in configuration file or in command line..</li>
   * </ul>
   * <p>
   * Option values are initialized in following order:
   * <ul>
   * <li>filled by default values from {@link IL2MainConstants#ALL_L2_MAIN_PARAMS} and
   * {@link IL2ApplicationConstants#ALL_L2_INIT_PARAMS};</li>
   * <li>updated by the values read from main configuration file
   * {@link IL2MainConstants#CLINEARG_MAIN_CFG_FILE_NAME};</li>
   * <li>updated by the values parsed from command line, when parsing command line {@link AvTextParser} is used to read
   * argument values in pairs "<code>-argName argValue</code>".</li>
   * </ul>
   *
   * @param aClineArgs String[] - command line arguments
   * @return {@link IOptionSetEdit} - global option values
   */
  private static IOptionSetEdit prepareGlobalOptions( String[] aClineArgs ) {
    ProgramArgs clineArgs = makeClineArgsAndProcessHelp( aClineArgs );
    // create global options
    IOptionSetEdit globalOps = new OptionSet();
    OptionSetUtils.initOptionSet( globalOps, ALL_L2_MAIN_PARAMS );
    OptionSetUtils.initOptionSet( globalOps, ALL_L2_INIT_PARAMS );
    // read and apply settings from config file
    File cfgFile = extractConfigFile( clineArgs );
    readAndApplyCfgFileOps( cfgFile, globalOps );
    // parse command line and apply to the options
    parseAndApplyCommandLineOps( clineArgs, globalOps );
    return globalOps;
  }

  private static ProgramArgs makeClineArgsAndProcessHelp( String[] aClineArgs ) {
    ProgramArgs clineArgs = new ProgramArgs( aClineArgs );
    // process help request
    String argVal = clineArgs.removeArg( CLINEARG_HELP, null );
    if( argVal != null ) {
      String valToParse = argVal.isEmpty() ? IStrioHardConstants.STR_BOOLEAN_TRUE : argVal;
      boolean isHelp = valToParse.equalsIgnoreCase( IStrioHardConstants.STR_BOOLEAN_TRUE );
      if( isHelp ) {
        displayHelpToStdoutAndExit();
        // the program never reaches this point
      }
    }
    return clineArgs;
  }

  private static File extractConfigFile( ProgramArgs aClineArgs ) {
    String argVal = aClineArgs.removeArg( CLINEARG_MAIN_CFG_FILE_NAME, DEFAULT_MAIN_CFG_FILE_NAME );
    return new File( argVal );
  }

  private static void readAndApplyCfgFileOps( File aFile, IOptionSetEdit aGlobalOps ) {
    ValidationResult vr = TsFileUtils.VALIDATOR_FILE_READABLE.validate( aFile );
    if( vr.isError() ) {
      logger.warning( vr.message() );
      // try to create an empty configuration file
      if( !aFile.exists() && TsFileUtils.isFileAppendable( aFile ) ) {
        try {
          OptionSetKeeper.KEEPER.write( aFile, IOptionSet.NULL );
          logger.info( FMT_INF_EMPTY_CFG_FILE_CREATED, aFile.getAbsolutePath() );
        }
        catch( Exception ex ) {
          LoggerUtils.error( ex );
        }
      }
      return;
    }
    try {
      IOptionSet result = OptionSetKeeper.KEEPER.read( aFile );
      logger.info( FMT_INF_CFG_FILE_READ_OK, aFile.getAbsolutePath() );
      aGlobalOps.addAll( result );
    }
    catch( Exception e ) {
      logger.warning( e, FMT_ERR_CFG_FILE_READ_FAIL, aFile.getAbsolutePath() );
    }
  }

  private static void parseAndApplyCommandLineOps( ProgramArgs aArgs, IOptionSetEdit aGlobalOps ) {
    CharInputStreamString chIn = new CharInputStreamString();
    IStrioReader sr = new StrioReader( chIn );
    IOptionSetEdit ops = new OptionSet();
    for( String argName : aArgs.argValues().keys() ) {
      if( !StridUtils.isValidIdPath( argName ) ) {
        logger.warning( FMT_WARN_NON_IDPATH_CLINE_ARG_IGNORED, argName );
        continue;
      }
      String argValue = aArgs.getArgValue( argName );
      chIn.setSource( argValue );
      IAtomicValue av;
      try {
        av = new AvTextParser().parse( sr.readLine() );
      }
      catch( @SuppressWarnings( "unused" ) Exception ex ) {
        logger.warning( FMT_WARN_INV_CLINE_ARG_VALUE_IGNORED, argName, argValue );
        continue;
      }
      ops.setValue( argName, av );
    }
    aGlobalOps.addAll( ops );
  }

  private static ValidationResult validateGlobalOps( IOptionSet aGlobalOps ) {
    ValidationResult vr = OptionSetUtils.validateOptionSet( aGlobalOps, ALL_L2_MAIN_PARAMS );
    if( vr.isError() ) {
      return vr;
    }
    return ValidationResult.firstNonOk( vr, OptionSetUtils.validateOptionSet( aGlobalOps, ALL_L2_INIT_PARAMS ) );
  }

  private static void setupL2Main( IOptionSet aGlobalOps ) {
    long rescanSecs = 1000L * OPDEF_LOGGER_CFG_RESCAN_SECS.getValue( aGlobalOps ).asInt();
    LoggerWrapper.setScanPropertiesTimeout( rescanSecs );
    if( OPDEF_USE_TEXT_CONSOLE.getValue( aGlobalOps ).asBool() ) {
      textConsole = new L2MainTextConsole();
    }
  }

  private static void displayHelpToStdoutAndExit() {
    // help message is always displayed in stdout
    TsTestUtils.pl( FMT_MSG_COMMAND_LINE_HELP );
    final String fmtStr = "  -%s - %s (%s)"; //$NON-NLS-1$
    // only command line arguments
    for( IDataDef claDef : ALL_L2_MAIN_SOLE_COMMAND_LINE_ARGS ) {
      TsTestUtils.pl( fmtStr, claDef.id(), claDef.nmName(), claDef.description() );
    }
    // arguments passed to the L2Application
    for( IDataDef dd : ALL_L2_MAIN_PARAMS ) {
      TsTestUtils.pl( fmtStr, dd.id(), dd.nmName(), dd.description() );
    }
    TsTestUtils.nl();
    logger.info( STR_HELP_SHOWN_EXIT );
    System.exit( ECODE_HELP_DISPLAYED );
  }

  private static void sayHello() {
    if( logger.isSeverityOn( ELogSeverity.INFO ) ) {
      logger.info( STR_HELLO );
    }
    else {
      TsTestUtils.pl( STR_HELLO );
    }
  }

  private static void sayGoodbye( L2AppQuitCommand aQuitCommand ) {
    if( logger.isSeverityOn( ELogSeverity.INFO ) ) {
      logger.info( FMT_LOG_L2MAIN_FINISHED, Integer.valueOf( aQuitCommand.exitCode() ), aQuitCommand.message() );
    }
    TsTestUtils.pl( FMT_L2MAIN_FINISHED, aQuitCommand.message(), Integer.valueOf( aQuitCommand.exitCode() ) );
  }

  private static ValidationResult logResult( ValidationResult aVr ) {
    if( !aVr.isOk() ) {
      logger.error( aVr.message() );
    }
    return aVr;
  }

}

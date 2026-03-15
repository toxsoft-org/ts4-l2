package org.toxsoft.l2.main.new_l2app.main;

import static org.toxsoft.l2.lib.main.IGlobalOps.*;
import static org.toxsoft.l2.main.new_l2app.app.IL2ApplicationConstants.*;
import static org.toxsoft.l2.main.new_l2app.l10n.IL2MainSharedResources.*;
import static org.toxsoft.l2.main.new_l2app.main.IL2MainConstants.*;

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
import org.toxsoft.l2.lib.main.*;
import org.toxsoft.l2.lib.main.impl.*;
import org.toxsoft.l2.main.new_l2app.app.*;
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

  private static final ILogger logger = LoggerWrapper.getLogger( L2Main.class.getName() );
  // DEBUG ---
  // private static final ILogger logger = LoggerUtils.defaultLogger();
  // ---

  /**
   * Text console is initialized only if {@link IL2MainConstants#OPDEF_USE_TEXT_CONSOLE} is <code>true</code>.
   */
  private static L2MainTextConsole textConsole = null;

  /**
   * Application startup.
   *
   * @param aArgs String[] - command line arguments
   */
  public static void main( String[] aArgs ) {
    // prepare default and error logger and log program startup
    LoggerUtils.setDefaultLogger( logger );
    LoggerUtils.setErrorLogger( logger );
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
    IProgramQuitCommand quitCmd;
    do {
      quitCmd = runApplication( globalOps );
    } while( quitCmd == null || quitCmd.programRetCode() == ECODE_RESTART_L2APP );
    // finish the program
    sayGoodbye( quitCmd );
    Runtime.getRuntime().halt( quitCmd.programRetCode() ); // halt() to avoid program hang because of crap threads
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  // DEBUG ---
  static int num = 0;
  // ---

  private static IProgramQuitCommand runApplication( IOptionSet aGlobalOps ) {
    // open SkConnection
    ISkConnection skConn;
    try {
      skConn = openConnection( aGlobalOps );
    }
    catch( Exception ex ) {
      logger.error( ex );
      return new ProgramQuitCommand( ECODE_CONN_OPEN_FAILED, ex.getMessage() );
    }
    // prepare L2Application context
    ITsContext l2AppArgs = new TsContext();
    l2AppArgs.params().setAll( aGlobalOps );
    REFDEF_UNIT_LOGGER.setRef( l2AppArgs, LoggerUtils.defaultLogger() );
    REFDEF_SK_CONNECTION.setRef( l2AppArgs, skConn );
    // initialize application
    L2Application app = new L2AppImpl();
    ValidationResult vr = app.init( l2AppArgs );
    if( vr.isError() ) {
      logger.error( vr.message() );
      return new ProgramQuitCommand( ECODE_INIT_FAILED, vr.message() );
    }
    // start application
    try {
      app.start();
    }
    catch( Exception ex ) {
      logger.error( ex );
      return new ProgramQuitCommand( ECODE_START_FAILED, ex.getMessage() );
    }

    // run normal main loop until quit command
    IProgramQuitCommand quitCmd = null;
    while( quitCmd == null ) {
      app.doJob();
      quitCmd = app.getQuitCommandIfAny();
      if( quitCmd == null && textConsole != null ) {
        quitCmd = textConsole.getQuitCommandIfAny();
      }

      // DEBUG ---
      if( ++num > 100 ) {
        quitCmd = new ProgramQuitCommand( ECODE_OK, "Normal finish" );
      }
      // ---

      if( quitCmd != null ) {
        app.queryStop();
      }
    }
    // wait till stop or timeout
    long timeStopWasQueried = System.currentTimeMillis();
    long timeoutMsecs = 1000L * OPDEF_SHUTDOWN_TIMEOUT_SECS.getValue( l2AppArgs.params() ).asInt();
    while( app.isStopped() ) {
      app.doJob();
      // process timeout
      if( System.currentTimeMillis() - timeStopWasQueried > timeoutMsecs ) {
        break;
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
    TsThreadExecutor threadExecutor = new TsThreadExecutor( L2Main.class.getSimpleName(), logger );
    ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.setRef( connArgs, threadExecutor );
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
        throw new TsIoRtException( "jhkgjhgjhg" ); // TODO add message
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
   * TODO 2026-03-15: {@link IGlobalOps#ALL_L2_GLOBAL_OPS} are used until they'll be moved to ALL_L2_INIT_PARAMS
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
    OptionSetUtils.initOptionSet( globalOps, ALL_L2_GLOBAL_OPS );
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
    vr = ValidationResult.firstNonOk( vr, OptionSetUtils.validateOptionSet( aGlobalOps, ALL_L2_GLOBAL_OPS ) );
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
    // non-listed arguments
    TsTestUtils.pl( fmtStr, CLINEARG_HELP, "", "" );
    TsTestUtils.pl( fmtStr, CLINEARG_MAIN_CFG_FILE_NAME, "", "" );
    // listed arguments
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

  private static void sayGoodbye( IProgramQuitCommand aQuitCommand ) {
    if( logger.isSeverityOn( ELogSeverity.INFO ) ) {
      logger.info( FMT_LOG_L2MAIN_FINISHED, Integer.valueOf( aQuitCommand.programRetCode() ), aQuitCommand.message() );
    }
    TsTestUtils.pl( FMT_L2MAIN_FINISHED, aQuitCommand.message(), Integer.valueOf( aQuitCommand.programRetCode() ) );
  }

  private static ValidationResult logResult( ValidationResult aVr ) {
    if( !aVr.isOk() ) {
      aVr.logTo( LoggerUtils.errorLogger() );
    }
    else {
      if( aVr != ValidationResult.SUCCESS ) {
        aVr.logTo( LoggerUtils.defaultLogger() );
      }
    }
    return aVr;
  }

}

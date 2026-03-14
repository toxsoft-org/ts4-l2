package org.toxsoft.l2.main;

import static org.toxsoft.l2.main.IL2ApplicationConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

/**
 * L2 application starter, contains {@link #main(String[])} method.
 *
 * @author hazard157
 */
public class L2Main {

  public static final int ECODE_INIT_FAILED  = 1;
  public static final int ECODE_START_FAILED = 2;

  /**
   * Application startup.
   *
   * @param aArgs String[] - command line arguments
   */
  public static void main( String[] aArgs ) {
    // TODO prepare default and error loggers and log main() start
    // LoggerUtils.setDefaultLogger( ??? );
    // LoggerUtils.setErrorLogger( ??? );

    // TODO prepare command line arguments

    // TODO prpare L2Application context
    ITsContext l2AppArgs = new TsContext();
    REFDEF_DEFAULT_LOGGER.setRef( l2AppArgs, LoggerUtils.defaultLogger() );
    REFDEF_ERROR_LOGGER.setRef( l2AppArgs, LoggerUtils.errorLogger() );

    // TODO prepare L2Application parameters
    String l2AppId = IStridable.NONE_ID;
    IOptionSetEdit l2AppParams = new OptionSet();

    // TODO initialize application
    L2Application app = new L2Application( l2AppId, l2AppParams );
    ValidationResult vr = logResult( app.init( l2AppArgs ) );
    if( vr.isError() ) {
      // TODO release allocated resources
      System.exit( ECODE_INIT_FAILED );
      return; // just for compiler
    }

    // TODO start application
    try {
      app.start();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
      // TODO release allocated resources
      System.exit( ECODE_START_FAILED );
      return; // just for compiler
    }

    // TODO run application
    Object quitCmd = null;
    while( quitCmd != null ) {

    }

  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static ValidationResult logResult( ValidationResult aVr ) {
    if( !aVr.isOk() ) {
      aVr.logTo( LoggerUtils.errorLogger() );
    }
    else {
      aVr.logTo( LoggerUtils.defaultLogger() );
    }
    return aVr;
  }

}

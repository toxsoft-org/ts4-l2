package org.toxsoft.l2.main.new_l2app.app;

import static org.toxsoft.l2.main.new_l2app.app.IL2ApplicationConstants.*;
import static org.toxsoft.l2.main.new_l2app.main.IL2MainConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.wub.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.main.*;
import org.toxsoft.l2.lib.main.impl.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link IL2Application} implementation.
 *
 * @author hazard157
 */
public class L2Application
    extends AbstractWubUnit
    implements IL2Application {

  private ILogger       logger;
  private ISkConnection skConn;

  /**
   * Constructor.
   *
   * @param aId String - the application ID (an IDpath)
   * @param aParame {@link IOptionSet} - creation parameters describing the application
   */
  public L2Application( String aId, IOptionSet aParame ) {
    super( aId, aParame );
  }

  // ------------------------------------------------------------------------------------
  // AbstractWubUnit
  //

  @Override
  protected ValidationResult doInit( ITsContextRo aEnviron ) {
    logger = REFDEF_UNIT_LOGGER.getRef( aEnviron );
    skConn = REFDEF_SK_CONNECTION.getRef( aEnviron );

    // TODO L2Application.doInit()

    logger.info( "L2App init()" );
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doStart() {

    // TODO L2Application.doStart()

    logger.info( "L2App start()" );
  }

  int counter = 0;

  @Override
  protected void doDoJob() {

    // TODO L2Application.doStart()
    ++counter;
    try {
      Thread.sleep( 50 );
    }
    catch( InterruptedException ex ) {
      LoggerUtils.errorLogger().error( ex );
    }

  }

  @Override
  protected boolean doQueryStop() {

    // TODO L2Application.doQueryStop()

    logger.info( "L2App doQueryStop()" );
    return false;
  }

  @Override
  protected boolean doStopping() {

    // TODO L2Application.doStopping()

    logger.info( "L2App doStopping()" );
    return true;
  }

  @Override
  protected void doDestroy() {

    // TODO L2Application.doDestroy()

    logger.info( "L2App doDestroy()" );
  }

  @Override
  public IProgramQuitCommand getQuitCommandIfAny() {

    // TODO L2Application.getQuitCommandIfAny()

    if( counter >= 50 ) {

      counter = 0;
      return new ProgramQuitCommand( ECODE_RESTART_L2APP, "Test restart" );

    }

    return null;
  }

}

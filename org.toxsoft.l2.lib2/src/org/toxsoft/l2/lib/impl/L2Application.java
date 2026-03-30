package org.toxsoft.l2.lib.impl;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;
import static org.toxsoft.l2.main.IL2MainConstants.*;

import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link IL2Application} implementation.
 *
 * @author hazard157
 */
public class L2Application
    extends AbstractTsCoopCompMultiUse
    implements IL2Application {

  private final String       appId;
  private final L2Hal        hal;
  private final L2DlmManager dlmMgr;
  private final L2Network    net;

  private ILogger           logger;
  private ITsThreadExecutor threadGuard;
  private ISkConnection     skConn;

  /**
   * Constructor.
   *
   * @param aAppId String - L2 application ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not an ID path
   */
  public L2Application( String aAppId ) {
    appId = StridUtils.checkValidIdPath( aAppId );
    hal = new L2Hal( appId );
    dlmMgr = new L2DlmManager( appId );
    net = new L2Network( appId );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {

    // TODO logger = new L2LoggerWrapper( "L2App(appId)", REFDEF_UNIT_LOGGER.getRef( aArgs ) );
    // where L2Loggerwrapper adds prefix #appId to the log messages

    logger = REFDEF_UNIT_LOGGER.getRef( aArgs );
    threadGuard = REFDEF_MAIN_THREAD_GUARD.getRef( aArgs );
    skConn = REFDEF_SK_CONNECTION.getRef( aArgs );
    ValidationResult vr = hal.init( aArgs );
    if( !vr.isError() ) {
      vr = firstNonOk( vr, net.init( aArgs ) );
      if( !vr.isError() ) {
        vr = firstNonOk( vr, dlmMgr.init( aArgs ) );
      }
    }
    logger.info( "L2Application init() - %s: %s", vr.type().id(), vr.message() );
    return vr;
  }

  @Override
  protected void doStart() {
    hal.start();
    net.start();
    dlmMgr.start();
    logger.info( "L2Application start()" );
  }

  int counter = 0;

  @Override
  protected void doDoJob() {

    // TODO L2Application.doStart()

    logger.info( "L2App doJob()" );
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
    hal.destroy();
    dlmMgr.destroy();
    net.destroy();
  }

  // ------------------------------------------------------------------------------------
  // IL2Application
  //

  @Override
  final public String appId() {
    return appId;
  }

  @Override
  public L2AppQuitCommand getQuitCommandIfAny() {

    // TODO L2Application.getQuitCommandIfAny()

    if( counter >= 9 ) {

      counter = 0;
      return new L2AppQuitCommand( ECODE_RESTART_L2APP, "Test restart" ); //$NON-NLS-1$

    }

    return null;
  }

}

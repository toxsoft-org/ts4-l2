package org.toxsoft.l2.lib.impl;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;

import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.app.*;

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
  private final ILogger      logger;

  private ITsThreadExecutor threadGuard;

  /**
   * Constructor.
   *
   * @param aAppId String - L2 application ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not an ID path
   */
  public L2Application( String aAppId ) {
    appId = StridUtils.checkValidIdPath( aAppId );
    logger = LoggerUtils.getLogger( this.getClass(), appId );
    hal = new L2Hal( this );
    dlmMgr = new L2DlmManager( this );
    net = new L2Network( this );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    threadGuard = REFDEF_MAIN_THREAD_GUARD.getRef( aArgs );
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
    // DEBUG logger.info( "L2Application start()" );
    hal.start();
    net.start();
    dlmMgr.start();
  }

  @Override
  protected void doDoJob() {
    // DEBUG logger.info( "L2App doJob()" );
    hal.doJob();
    dlmMgr.doJob();
    // FIXME --- HAL must process I/O read BEFORE DLM manager and I/O write AFTER
    // hal.doJob();
    // ---
    net.doJob();
  }

  @Override
  protected boolean doQueryStop() {
    // DEBUG logger.info( "L2App doQueryStop()" );
    boolean s1 = hal.queryStop();
    boolean s2 = dlmMgr.queryStop();
    boolean s3 = net.queryStop();
    return s1 && s2 && s3;
  }

  @Override
  protected boolean doStopping() {
    // DEBUG logger.info( "L2App doStopping()" );
    boolean s1 = hal.isStopped();
    boolean s2 = dlmMgr.isStopped();
    boolean s3 = net.isStopped();
    return s1 && s2 && s3;
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
    L2AppQuitCommand cmd = hal.getQuitCommandIfAny();
    if( cmd == null ) {
      cmd = dlmMgr.getQuitCommandIfAny();
      if( cmd == null ) {
        cmd = net.getQuitCommandIfAny();
      }
    }
    return cmd;
  }

}

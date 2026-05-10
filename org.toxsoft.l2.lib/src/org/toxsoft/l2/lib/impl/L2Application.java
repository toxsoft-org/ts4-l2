package org.toxsoft.l2.lib.impl;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
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

  private final ILogger         logger;
  private final L2SharedContext l2Context;

  /**
   * Constructor.
   *
   * @param aAppId String - L2 application ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException argument is not an ID path
   */
  public L2Application( String aAppId ) {
    l2Context = new L2SharedContext( aAppId );
    logger = LoggerUtils.getLogger( this.getClass(), appId() );
    l2Context.setHal( new L2Hal( l2Context ) );
    l2Context.setDlmMgr( new L2DlmManager( l2Context ) );
    l2Context.setNetwork( new L2Network( l2Context ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    l2Context.setThreadGuard( REFDEF_MAIN_THREAD_GUARD.getRef( aArgs ) );
    ValidationResult vr = l2Context.hal().init( aArgs );
    if( !vr.isError() ) {
      vr = firstNonOk( vr, l2Context.net().init( aArgs ) );
      if( !vr.isError() ) {
        vr = firstNonOk( vr, l2Context.dlmMgr().init( aArgs ) );
      }
    }
    logger.info( FMT_L2_APP_FINISH_STATUS, vr.type().id(), vr.message() );
    return vr;
  }

  @Override
  protected void doStart() {
    l2Context.hal().start();
    l2Context.net().start();
    l2Context.dlmMgr().start();
  }

  @Override
  protected void doDoJob() {
    l2Context.hal().doJob();
    l2Context.dlmMgr().doJob();
    // FIXME --- HAL must process I/O read BEFORE DLM manager and I/O write AFTER
    // l2Context.hal().doJob();
    // ---
    l2Context.net().doJob();
  }

  @Override
  protected boolean doQueryStop() {
    boolean s1 = l2Context.hal().queryStop();
    boolean s2 = l2Context.dlmMgr().queryStop();
    boolean s3 = l2Context.net().queryStop();
    return s1 && s2 && s3;
  }

  @Override
  protected boolean doStopping() {
    boolean s1 = l2Context.hal().isStopped();
    boolean s2 = l2Context.dlmMgr().isStopped();
    boolean s3 = l2Context.net().isStopped();
    return s1 && s2 && s3;
  }

  @Override
  protected void doDestroy() {
    l2Context.hal().destroy();
    l2Context.dlmMgr().destroy();
    l2Context.net().destroy();
  }

  // ------------------------------------------------------------------------------------
  // IL2Application
  //

  @Override
  final public String appId() {
    return l2Context.appId();
  }

  @Override
  public L2AppQuitCommand getQuitCommandIfAny() {
    L2AppQuitCommand cmd = l2Context.hal().getQuitCommandIfAny();
    if( cmd == null ) {
      cmd = l2Context.dlmMgr().getQuitCommandIfAny();
      if( cmd == null ) {
        cmd = l2Context.net().getQuitCommandIfAny();
      }
    }
    return cmd;
  }

}

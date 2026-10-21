package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.app.*;
import org.toxsoft.l2.lib.api.module.*;

/**
 * {@link IL2SharedContext} implementation.
 *
 * @author hazard157
 */
final class L2SharedContext
    implements IL2SharedContext, ITsInitializable {

  private final String appId;

  private L2Hal        hal;
  private L2DlmManager dlmMgr;
  private L2Network    net;
  private EL2SweepStep sweepStep;

  private ITsContextRo                 args             = null;
  private ITsThreadExecutor            threadGuard      = null;
  private IL2ModuleConfigManager       modConfigMgr     = null;
  private IL2ModuleFactoriesRepository modFactoriesRepo = null;

  private long sweepStartTime  = 0L;
  private long sweepStartNanos = 0L;

  public L2SharedContext( String aAppId ) {
    appId = StridUtils.checkValidIdPath( aAppId );
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  void setHal( L2Hal aHal ) {
    hal = aHal;
  }

  void setDlmMgr( L2DlmManager aDlmManager ) {
    dlmMgr = aDlmManager;
  }

  void setNetwork( L2Network aNet ) {
    net = aNet;
  }

  void setSweepStep( EL2SweepStep aStep ) {
    sweepStep = aStep;
  }

  void sweepStarted() {
    sweepStartNanos = System.nanoTime();
    sweepStartTime = System.currentTimeMillis();
  }

  // ------------------------------------------------------------------------------------
  // ITsInitializable
  //

  @Override
  public ValidationResult init( ITsContextRo aArgs ) {
    args = TsNullArgumentRtException.checkNull( aArgs );
    modConfigMgr = IL2ApplicationArguments.REFDEF_L2_MODULE_CONFIG_MANAGER.getRef( aArgs );
    modFactoriesRepo = IL2ApplicationArguments.REFDEF_L2_MODULE_FACTORIES_REPOSITORY.getRef( aArgs );
    threadGuard = IL2ApplicationArguments.REFDEF_L2_MAIN_THREAD_GUARD.getRef( aArgs );
    return ValidationResult.SUCCESS;
  }

  // ------------------------------------------------------------------------------------
  // IL2GlobalContext
  //

  @Override
  public String appId() {
    return appId;
  }

  @Override
  public L2Hal hal() {
    TsIllegalStateRtException.checkNull( hal );
    return hal;
  }

  @Override
  public L2DlmManager dlmMgr() {
    TsIllegalStateRtException.checkNull( dlmMgr );
    return dlmMgr;
  }

  @Override
  public L2Network net() {
    TsIllegalStateRtException.checkNull( net );
    return net;
  }

  @Override
  public ITsThreadExecutor threadGuard() {
    return threadGuard;
  }

  @Override
  public EL2SweepStep sweepStep() {
    return sweepStep;
  }

  @Override
  public ITsContextRo args() {
    return args;
  }

  @Override
  public long sweepStartTime() {
    return sweepStartTime;
  }

  @Override
  public long sweepStartNanos() {
    return sweepStartNanos;
  }

  @Override
  public IL2ModuleConfigManager moduleConfigManager() {
    return modConfigMgr;
  }

  @Override
  public IL2ModuleFactoriesRepository moduleFactoriesRepository() {
    return modFactoriesRepo;
  }

  @Override
  public IOptionSet params() {
    return args.params();
  }

}

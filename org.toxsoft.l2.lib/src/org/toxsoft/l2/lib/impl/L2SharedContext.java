package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.common.*;

/**
 * {@link IL2SharedContext} implementation.
 *
 * @author hazard157
 */
final class L2SharedContext
    implements IL2SharedContext {

  private final String appId;

  private L2Hal             hal;
  private L2DlmManager      dlmMgr;
  private L2Network         net;
  private ITsThreadExecutor threadGuard;

  public L2SharedContext( String aAppId ) {
    appId = StridUtils.checkValidIdPath( aAppId );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  public void setHal( L2Hal aHal ) {
    hal = aHal;
  }

  public void setDlmMgr( L2DlmManager aDlmManager ) {
    dlmMgr = aDlmManager;
  }

  public void setNetwork( L2Network aNet ) {
    net = aNet;
  }

  public void setThreadGuard( ITsThreadExecutor aThreadGuard ) {
    threadGuard = aThreadGuard;
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

}

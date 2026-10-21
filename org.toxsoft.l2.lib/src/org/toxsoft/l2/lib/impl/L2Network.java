package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.api.app.IL2ApplicationArguments.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.app.*;
import org.toxsoft.l2.lib.api.module.net.*;
import org.toxsoft.l2.lib.impl.comps.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link IL2Network} implementation.
 *
 * @author hazard157
 */
public final class L2Network
    extends L2AbstractComponent<L2AbstractNetModule>
    implements IL2Network, ICooperativeWorkerComponent {

  private ISkConnection skConn = null;

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2SharedContext} - the L2 context
   */
  public L2Network( IL2SharedContext aL2Context ) {
    super( aL2Context, ALL_NET_COMP_OPDEFS );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractComponent
  //

  @Override
  public EL2ComponentKind kind() {
    return EL2ComponentKind.NETWORK;
  }

  @Override
  protected ValidationResult doDoInit( ITsContextRo aArgs ) {
    skConn = IL2ApplicationArguments.REFDEF_SK_CONNECTION.getRef( aArgs );

    // TODO L2Network.doDoInit()
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doDoStart() {
    // TODO L2Network.doDoStart()
  }

  @Override
  protected void doDoDoJob() {
    // TODO L2Network.doDoDoJob()
  }

  @Override
  protected boolean doDoQueryStop() {
    // TODO L2Network.doDoQueryStop()
    return false;
  }

  // ------------------------------------------------------------------------------------
  // IL2Network
  //

  @Override
  public IStridablesList<IL2NetModule> netModules() {
    return super.modules();
  }

  @Override
  public ISkConnection getSkConnection() {
    TsIllegalStateRtException.checkNull( skConn );
    return skConn;
  }

}

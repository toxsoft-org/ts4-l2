package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.api.app.IL2ApplicationArguments.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
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
  // IL2Component
  //

  @Override
  public EL2ComponentKind kind() {
    return EL2ComponentKind.NETWORK;
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractComponent
  //

  @Override
  protected ValidationResult doL2Init( ITsContextRo aArgs ) {
    // TODO Auto-generated method stub
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doL2DestroyBeforeModules() {
    // TODO Auto-generated method stub
  }

  @Override
  protected void doL2DestroyAfterModules() {
    // TODO Auto-generated method stub
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

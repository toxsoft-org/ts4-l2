package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.api.app.IL2ApplicationArguments.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.l2.lib.api.module.hal.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
public final class L2Hal
    extends L2AbstractComponent<L2AbstractHalDriver>
    implements IL2Hal, ICooperativeWorkerComponent {

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2SharedContext} - the L2 context
   */
  public L2Hal( IL2SharedContext aL2Context ) {
    super( aL2Context, ALL_HAL_COMP_OPDEFS );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // IL2Component
  //

  @Override
  public EL2ComponentKind kind() {
    return EL2ComponentKind.HAL;
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
  // IL2Hal
  //

  @Override
  public IStridablesList<IL2HalDriver> drivers() {
    return super.modules();
  }

  @Override
  public IStridablesList<IL2Tag> tags() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStridablesList<IL2Tag> listDeviceTags( String aDeviceId ) {
    // TODO Auto-generated method stub
    return null;
  }

}

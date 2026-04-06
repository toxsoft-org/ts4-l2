package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
class L2Hal
    extends L2AbstractComponent
    implements IL2Hal, ICooperativeWorkerComponent {

  private final IStridablesListEdit<L2AbstractSignal> signalsList = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aL2App {@link L2Application} - the L2 Application
   */
  public L2Hal( L2Application aL2App ) {
    super( aL2App, IL2HalConstants.ALL_HAL_ARG_OPDEFS );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doDoInit( ITsContextRo aArgs ) {

    // TODO Auto-generated method stub

    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doDoJob() {

    // TODO L2Hal.doDoJob()

  }

  @Override
  protected boolean doQueryStop() {

    // TODO L2Hal.doQueryStop()

    return true;
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractComponent
  //

  // ------------------------------------------------------------------------------------
  // IL2Component
  //

  @Override
  final public EL2ComponentKind kind() {
    return EL2ComponentKind.HAL;
  }

  // ------------------------------------------------------------------------------------
  // IL2Hal
  //

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesList<IL2Signal> signals() {
    return (IStridablesList)signalsList;
  }

  @Override
  public IStridablesList<IL2SpecificDevice> specificDevices() {
    // TODO Auto-generated method stub
    return null;
  }

}

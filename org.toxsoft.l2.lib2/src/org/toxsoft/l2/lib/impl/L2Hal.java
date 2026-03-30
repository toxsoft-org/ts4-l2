package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
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
   * @param aAppId String - the L2Application ID
   */
  public L2Hal( String aAppId ) {
    super( aAppId, IL2HalConstants.ALL_HAL_ARG_OPDEFS );
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
    // TODO Auto-generated method stub

  }

  @Override
  protected boolean doQueryStop() {
    // TODO Auto-generated method stub
    return false;
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

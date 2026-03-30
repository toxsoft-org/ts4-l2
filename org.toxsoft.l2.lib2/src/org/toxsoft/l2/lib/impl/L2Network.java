package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.net.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
class L2Network
    extends L2AbstractComponent
    implements IL2Network, ICooperativeWorkerComponent {

  /**
   * Constructor.
   *
   * @param aAppId String - the L2Application ID
   */
  public L2Network( String aAppId ) {
    super( aAppId, IL2NetworkConstants.ALL_NETWORK_ARG_OPDEFS );
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
  // IL2Network
  //

}

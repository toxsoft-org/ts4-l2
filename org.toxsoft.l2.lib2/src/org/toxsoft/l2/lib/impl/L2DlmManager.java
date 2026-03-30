package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.l2.lib.dlms.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
class L2DlmManager
    extends L2AbstractComponent
    implements IL2DlmManager, ICooperativeWorkerComponent {

  /**
   * Constructor.
   *
   * @param aAppId String - the L2Application ID
   */
  public L2DlmManager( String aAppId ) {
    super( aAppId, IL2DlmManagerConstants.ALL_DLM_MANAGER_ARG_OPDEFS );
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
  // IL2DlmManager
  //

}

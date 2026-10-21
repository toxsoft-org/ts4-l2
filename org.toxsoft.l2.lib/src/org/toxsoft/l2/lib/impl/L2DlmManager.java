package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.api.app.IL2ApplicationArguments.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.l2.lib.api.module.dlm.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * {@link IL2DlmManager} implementation.
 *
 * @author hazard157
 */
public final class L2DlmManager
    extends L2AbstractComponent<L2AbstractDlm>
    implements IL2DlmManager, ICooperativeWorkerComponent {

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2SharedContext} - the L2 context
   */
  public L2DlmManager( IL2SharedContext aL2Context ) {
    super( aL2Context, ALL_DLMMGR_COMP_OPDEFS );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractComponent
  //

  @Override
  public EL2ComponentKind kind() {
    return EL2ComponentKind.DLMMGR;
  }

  @Override
  protected ValidationResult doDoInit( ITsContextRo aArgs ) {
    // TODO L2DlmManager.doDoInit()
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doDoStart() {
    // TODO L2DlmManager.doDoStart()
  }

  @Override
  protected void doDoDoJob() {
    // TODO L2DlmManager.doDoDoJob()
  }

  @Override
  protected boolean doDoQueryStop() {
    // TODO L2DlmManager.doDoQueryStop()
    return true;
  }

  // ------------------------------------------------------------------------------------
  // IL2DlmManager
  //

  @Override
  public IStridablesList<IL2Dlm> dlms() {
    return super.modules();
  }

}

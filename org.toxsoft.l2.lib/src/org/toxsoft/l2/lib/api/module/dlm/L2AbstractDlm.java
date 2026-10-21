package org.toxsoft.l2.lib.api.module.dlm;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * {@link IL2Dlm} implementation base.
 *
 * @author hazard157
 */
public abstract class L2AbstractDlm
    extends L2AbstractModule
    implements IL2Dlm {

  private final L2DlmManager owner;

  /**
   * Constructor.
   *
   * @param aOwner {@link L2AbstractComponent} - the owner component creating the module
   * @param aConfig {@link L2ModuleConfig} - the module configuration
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException owner is not {@link L2DlmManager}
   */
  public L2AbstractDlm( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig ) {
    super( aOwner, aConfig );
    owner = L2DlmManager.class.cast( aOwner );
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractModule
  //

  @Override
  final public L2DlmManager owner() {
    return owner;
  }

  @Override
  protected abstract ValidationResult doInit( ITsContextRo aArgs );

  @Override
  protected abstract void doStart();

  @Override
  protected abstract void doDoJob();

  @Override
  protected abstract boolean doQueryStop();

  @Override
  protected abstract boolean doStopping();

  @Override
  protected abstract void doDestroy();

  // ------------------------------------------------------------------------------------
  // IL2Dlm
  //

  // nop

}

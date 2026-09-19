package org.toxsoft.l2.lib.dlms;

import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.common.*;

/**
 * {@link IL2Dlm} base implementation.
 *
 * @author hazard157
 */
public class L2AbstractDlm
    extends AbstractTsCoopCompMultiUse
    implements IL2Dlm {

  private final L2ModuleConfigFile cfg;
  private final DlmInfo            info;

  /**
   * Constructor.
   * <p>
   * Note: reference to <code>aParams</code> are stored directly, without creating a defensive copy.
   *
   * @param aConfig {@link L2ModuleConfigFile} - configuration data
   * @param aDlmInfo - DLM information
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public L2AbstractDlm( L2ModuleConfigFile aConfig, DlmInfo aDlmInfo ) {
    TsNullArgumentRtException.checkNulls( aConfig, aDlmInfo );
    cfg = aConfig;
    info = aDlmInfo;
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  final public String id() {
    return cfg.id();
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  final public String nmName() {
    return cfg.nmName();
  }

  @Override
  final public String description() {
    return cfg.description();
  }

  // ------------------------------------------------------------------------------------
  // IDlm
  //

  @Override
  public DlmInfo info() {
    return info;
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    // TODO Auto-generated method stub
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doStart() {
    // TODO Auto-generated method stub
    super.doStart();
  }

  @Override
  protected void doDoJob() {
    // TODO Auto-generated method stub

  }

  @Override
  protected boolean doQueryStop() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  protected boolean doStopping() {
    // TODO Auto-generated method stub
    return super.doStopping();
  }

  @Override
  protected void doDestroy() {
    // TODO Auto-generated method stub
    super.doDestroy();
  }

}

package org.toxsoft.l2.lib.dlms;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link IDlm} base implementation.
 *
 * @author hazard157
 */
public class L2AbstractDlm
    extends AbstractTsCoopCompMultiUse
    implements IDlm {

  private final String     id;
  private final DlmInfo    info;
  private final IOptionSet params;

  /**
   * Constructor.
   * <p>
   * Note: reference to <code>aParams</code> are stored directly, without creating a defensive copy.
   *
   * @param aInstanceId String - the instance ID
   * @param aDlmInfo - DLM information
   * @param aParams {@link IOptionSet} - creation parameters, values of {@link #params}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public L2AbstractDlm( String aInstanceId, DlmInfo aDlmInfo, IOptionSet aParams ) {
    StridUtils.checkValidIdPath( aInstanceId );
    TsNullArgumentRtException.checkNulls( aDlmInfo, aParams );
    id = aInstanceId;
    info = aDlmInfo;
    params = aParams;
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  final public String id() {
    return id;
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  final public String nmName() {
    return params.getStr( DDEF_NAME );
  }

  @Override
  final public String description() {
    return params.getStr( DDEF_DEFAULT_VALUE );
  }

  @Override
  final public IOptionSet params() {
    return params;
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

package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.logs.*;

/**
 * Base implementation of L2 core components.
 * <p>
 * {@link #params()} contains component-specific options update from arguments in {@link #init(ITsContextRo)}.
 *
 * @author hazard157
 */
abstract class L2AbstractComponent
    extends AbstractTsCoopCompMultiUse
    implements IParameterizedEdit {

  private final String                    l2AppId;
  private final IStridablesList<IDataDef> opDefs;
  private final IOptionSetEdit            params = new OptionSet();

  private ILogger           logger;
  private ITsThreadExecutor guardThread;

  /**
   * Constructor.
   *
   * @param aL2AppId String - the L2Application ID
   * @param aOpDefs {@link IStridablesListEdit}&lt;{@link IDataDef}&gt; - component-specific option definitions
   */
  protected L2AbstractComponent( String aL2AppId, IStridablesList<IDataDef> aOpDefs ) {
    l2AppId = aL2AppId;
    opDefs = aOpDefs;
    OptionSetUtils.initOptionSet( params, opDefs );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  final protected ValidationResult doInit( ITsContextRo aArgs ) {
    // initialize mandatory references from arguments
    // String shortName = this.getClass().getSimpleName();
    // TODO logger = new L2LoggerWrapper( sahortName+"(appId)", REFDEF_UNIT_LOGGER.getRef( aArgs ) );
    logger = REFDEF_UNIT_LOGGER.getRef( aArgs );
    guardThread = REFDEF_MAIN_THREAD_GUARD.getRef( aArgs );
    // initialize and check options
    IOptionSetEdit tmpOps = new OptionSet( params ); // default values
    tmpOps.refreshSet( aArgs.params() ); // update known options
    ValidationResult vr = OptionSetUtils.validateOptionSet( tmpOps, opDefs );
    if( !vr.isError() ) {
      vr = ValidationResult.firstNonOk( vr, doDoInit( aArgs ) );
    }
    return vr;
  }

  // ------------------------------------------------------------------------------------
  // IParameterizedEdit
  //

  @Override
  public IOptionSetEdit params() {
    return params;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  public String appId() {
    return l2AppId;
  }

  public ILogger logger() {
    return logger;
  }

  public ITsThreadExecutor guardThread() {
    return guardThread;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  protected abstract ValidationResult doDoInit( ITsContextRo aArgs );

}

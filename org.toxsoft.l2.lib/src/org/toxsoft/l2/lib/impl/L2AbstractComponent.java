package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.IL2GlobalOptions.*;
import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import java.io.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.common.*;

/**
 * Base implementation of L2 core components.
 *
 * @author hazard157
 */
abstract class L2AbstractComponent
    extends AbstractTsCoopCompMultiUse
    implements IL2Component, IParameterizedEdit {

  private final IL2SharedContext          l2Context;
  private final IStridablesList<IDataDef> opDefs;
  private final IOptionSetEdit            params = new OptionSet();
  private final ILogger                   logger;

  private ITsThreadExecutor    guardThread = null;
  private L2ComponentConfigDir cfgDir      = null;

  /**
   * The quit command when quit is initialized by this component or <code>null</code>.
   * <p>
   * Quit command is reset to <code>null</code> when initializing, non-<code>null</code> value may be set by subclass by
   * method {@link #setQuitCommand(L2AppQuitCommand)}.
   */
  private L2AppQuitCommand quitCmd = null;

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2SharedContext} - the L2 context
   * @param aOpDefs {@link IStridablesListEdit}&lt;{@link IDataDef}&gt; - component-specific option definitions
   */
  protected L2AbstractComponent( IL2SharedContext aL2Context, IStridablesList<IDataDef> aOpDefs ) {
    l2Context = aL2Context;
    opDefs = aOpDefs;
    OptionSetUtils.initOptionSet( params, opDefs );
    logger = LoggerUtils.getLogger( this.getClass(), l2Context.appId() );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  final protected ValidationResult doInit( ITsContextRo aArgs ) {
    // reset class fields
    quitCmd = null;
    cfgDir = null;
    guardThread = null;
    OptionSetUtils.initOptionSet( params, opDefs );

    // initialize mandatory references from arguments
    guardThread = REFDEF_MAIN_THREAD_GUARD.getRef( aArgs );
    // initialize and check options
    IOptionSetEdit tmpOps = new OptionSet( params ); // default values
    tmpOps.refreshSet( aArgs.params() ); // update known options
    ValidationResult vr = OptionSetUtils.validateOptionSet( tmpOps, opDefs );
    if( vr.isError() ) {
      return vr;
    }
    // initialize configuration directory
    String rootdir = OPDEF_L2_COMP_CFG_DIR_ROOT.getValue( params() ).asString();
    File f = new File( rootdir, kind().id() ); // Note: directory is specified relative to the program working directory
    try {
      cfgDir = new L2ComponentConfigDir( f, kind().getModuleConfigFileExtension() );
    }
    catch( Exception ex ) {
      return ValidationResult.error( ex );
    }
    // subclass initialization
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
  // IL2Component
  //

  @Override
  final public IL2SharedContext l2Context() {
    return l2Context;
  }

  @Override
  final public L2AppQuitCommand getQuitCommandIfAny() {
    return quitCmd;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  public ILogger logger() {
    return logger;
  }

  public ITsThreadExecutor guardThread() {
    TsIllegalStateRtException.checkNull( guardThread );
    return guardThread;
  }

  public L2ComponentConfigDir cfgDir() {
    TsIllegalStateRtException.checkNull( cfgDir );
    return cfgDir;
  }

  final protected void setQuitCommand( L2AppQuitCommand aQuitCmd ) {
    if( aQuitCmd != null ) {
      logger.info( FMT_INFO_L2COMP_INITED_QUIT, aQuitCmd.toString() );
    }
    quitCmd = aQuitCmd;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  protected abstract ValidationResult doDoInit( ITsContextRo aArgs );

}

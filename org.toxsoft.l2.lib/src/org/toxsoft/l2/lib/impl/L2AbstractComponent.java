package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.api.app.IL2ApplicationArguments.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.vrl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.api.*;
import org.toxsoft.l2.lib.api.app.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.api.module.dlm.*;
import org.toxsoft.l2.lib.api.module.hal.*;
import org.toxsoft.l2.lib.api.module.net.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * Base implementation of L2 application component {@link IL2Component}.
 *
 * @author hazard157
 * @param <M> - modules class may be {@link L2AbstractDlm}, {@link L2AbstractHalDriver} or {@link L2AbstractNetModule}
 */
public abstract class L2AbstractComponent<M extends L2AbstractModule>
    extends AbstractTsCoopCompMultiUse
    implements IL2Component, IParameterized {

  // TODO listen to the module config changes and reload modules

  private final IL2SharedContext          l2Context;
  private final IStridablesList<IDataDef> opDefs;
  private final IOptionSetEdit            params = new OptionSet();
  private final ILogger                   logger;

  private final IStridablesListEdit<L2AbstractModule> modulesList = new StridablesList<>();

  /**
   * The quit command when quit is initialized by this component or <code>null</code>.
   * <p>
   * Quit command is reset to <code>null</code> when initializing, non-<code>null</code> value may be set by subclass by
   * method {@link #setQuitCommand(L2AppCommand)}.
   */
  private L2AppCommand quitCmd = null;

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
  // Implementation
  //

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  private IStridablesListEdit<L2AbstractModule> createModuleInstances( IVrListEdit aVrl, boolean aThrowExOnError ) {
    // prepare
    IStridablesListEdit<L2AbstractModule> llMods = new StridablesList<>();
    IStridablesList<IL2ModuleFactory<L2AbstractModule>> llFactories =
        l2Context.moduleFactoriesRepository().listFactories( kind() );
    // load module instances
    for( L2ModuleConfig modCfg : l2Context.moduleConfigManager().listModuleConfigs( kind() ) ) {
      IL2ModuleFactory<L2AbstractModule> factory = llFactories.findByKey( modCfg.factoryId() );
      if( factory != null ) {
        L2AbstractModule moduleInstance;
        try {
          moduleInstance = factory.create( (L2AbstractComponent)this, modCfg );
        }
        catch( Exception ex ) {
          logger.error( ex );
          if( aThrowExOnError ) {
            String msg = String.format( FMT_ERR_FACTORY_CANT_CREATE_MODULE, kind().id(), modCfg.id(), factory.id() );
            logger.error( msg );
            aVrl.error( msg );
            return null;
          }
          String msg = String.format( FMT_WARN_FACTORY_CANT_CREATE_MODULE, kind().id(), modCfg.id(), factory.id() );
          logger.warning( msg );
          aVrl.warn( msg );
          continue;
        }
        TsInternalErrorRtException.checkNull( moduleInstance );
        TsInternalErrorRtException.checkFalse( moduleInstance.id().equals( modCfg.id() ) );
        TsInternalErrorRtException.checkFalse( moduleInstance.moduleConfig().equals( modCfg ) );
        llMods.add( moduleInstance );
      }
      else {
        if( aThrowExOnError ) {
          String msg = String.format( FMT_ERR_NO_MODULE_FACTORY, kind().id(), modCfg.id(), modCfg.factoryId() );
          logger.error( msg );
          aVrl.error( msg );
          return null;
        }
        String msg = String.format( FMT_WARN_NO_MODULE_FACTORY, kind().id(), modCfg.id(), modCfg.factoryId() );
        logger.warning( msg );
        aVrl.warn( msg );
        continue;
      }
    }
    return llMods;
  }

  private void initLoadedModules( ITsContextRo aArgs, boolean aThrowExOnError ) {
    for( L2AbstractModule m : modulesList ) {
      try {
        m.init( aArgs );
        logger.info( FMT_INFO_MODULE_INITED, kind().id(), m.id() );
      }
      catch( Exception ex ) {
        logger.error( ex );
        if( aThrowExOnError ) {
          String msg = String.format( FMT_ERR_CANT_INIT_MODULE, kind().id(), m.id(), ex.getMessage() );
          logger.error( msg );
          throw new L2RtException( msg );
        }
        String msg = String.format( FMT_WARN_CANT_INIT_MODULE, kind().id(), m.id(), ex.getMessage() );
        logger.warning( msg );
      }
    }
  }

  private void startInitedModules( boolean aThrowExOnError ) {
    for( L2AbstractModule m : modulesList ) {
      // ignore uninitialized modules
      if( m.compState() != ETsCoopCompState.INITIALIZED ) {
        continue;
      }
      // try to start module
      try {
        m.start();
        logger.info( FMT_INFO_MODULE_STARTED, kind().id(), m.id() );
      }
      catch( Exception ex ) {
        logger.error( ex );
        if( aThrowExOnError ) {
          String msg = String.format( FMT_ERR_CANT_START_MODULE, kind().id(), m.id(), ex.getMessage() );
          logger.error( msg );
          throw new L2RtException( msg );
        }
        String msg = String.format( FMT_WARN_CANT_START_MODULE, kind().id(), m.id(), ex.getMessage() );
        logger.warning( msg );
      }
    }
  }

  /**
   * Checks module changes (previously provided by {@link IL2ModuleConfigManager}) and reload modules if needed.
   */
  private void checkChangesAndRestartModules() {
    // TODO L2AbstractComponent.checkChangesAndRestartModules()
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCooperativeComponent
  //

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    // reset class fields
    quitCmd = null;
    modulesList.clear();
    OptionSetUtils.initOptionSet( params, opDefs );
    IVrListEdit vrl = new VrList();
    // initialize and check options
    boolean throwExOnError = OPDEF_FAIL_APP_ON_MODULE_START_FAIL.getValue( aArgs.params() ).asBool();
    IOptionSetEdit tmpOps = new OptionSet( params ); // default values
    tmpOps.refreshSet( aArgs.params() ); // update known options
    vrl.add( OptionSetUtils.validateOptionSet( tmpOps, opDefs ) );
    if( vrl.isError() ) {
      return vrl.firstWorstResult();
    }
    // create module instances
    IStridablesListEdit<L2AbstractModule> llMods = createModuleInstances( vrl, throwExOnError );
    if( llMods == null ) {
      return vrl.firstWorstResult();
    }
    modulesList.addAll( llMods );
    // initialize module instances
    initLoadedModules( aArgs, throwExOnError );
    // subclass initialization
    vrl.add( doDoInit( aArgs ) );
    return vrl.firstWorstResult();
  }

  @Override
  protected void doStart() {
    boolean throwExOnError = OPDEF_FAIL_APP_ON_MODULE_START_FAIL.getValue( l2Context.params() ).asBool();
    startInitedModules( throwExOnError );
  }

  @Override
  protected void doDoJob() {
    checkChangesAndRestartModules();
    // iterate over existing modules and call doJob()
    for( int i = 0, c = modulesList.size(); i < c; i++ ) { // index iteration to avoid any memory allocation
      L2AbstractModule m = modulesList.get( i );
      // bypass ignored modules
      if( !m.isDoJobCallable() ) {
        continue;
      }
      try {
        m.doJob();
      }
      catch( Exception ex ) {
        logger().error( ex );
        // TODO what to do with the modules with errors?
        // maybe we need some strategy: A) ignore failed, B) pause/resume C) stop D) destroy E) something else?
      }
    }
  }

  @Override
  protected boolean doQueryStop() {
    boolean isStopped = true;
    for( int i = 0, c = modulesList.size(); i < c; i++ ) { // index iteration to avoid any memory allocation
      L2AbstractModule m = modulesList.get( i );
      if( !m.queryStop() ) {
        isStopped = false;
      }
    }
    return isStopped;
  }

  @Override
  protected boolean doStopping() {
    boolean isStopped = true;
    for( int i = 0, c = modulesList.size(); i < c; i++ ) { // index iteration to avoid any memory allocation
      L2AbstractModule m = modulesList.get( i );
      if( !m.isStopped() ) {
        isStopped = false;
      }
    }
    return isStopped;
  }

  @Override
  protected void doDestroy() {
    for( L2AbstractModule m : modulesList ) {
      m.destroy();
    }
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  final public IOptionSetEdit params() {
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
  final public L2AppCommand getQuitCommandIfAny() {
    return quitCmd;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  final public <T extends IL2Module> IStridablesList<T> modules() {
    return (IStridablesList)modulesList;
  }

  @Override
  final public <T extends IL2Module> T findModule( String aModuleId, Class<T> aExpectedClass ) {
    TsNullArgumentRtException.checkNulls( aModuleId, aExpectedClass );
    IL2Module found = modulesList.findByKey( aModuleId );
    return aExpectedClass.cast( found );
  }

  @Override
  final public <T extends IL2Module> T getModule( String aModuleId, Class<T> aExpectedClass ) {
    TsNullArgumentRtException.checkNulls( aModuleId, aExpectedClass );
    IL2Module found = modulesList.getByKey( aModuleId );
    return aExpectedClass.cast( found );
  }

  // ------------------------------------------------------------------------------------
  // API for module developers
  //

  /**
   * Returns the component logger.
   *
   * @return {@link ILogger} - logger for this component
   */
  public final ILogger logger() {
    return logger;
  }

  /**
   * Sends to L2 application the application command (usually to quit/restart).
   *
   * @param aQuitCmd {@link L2AppCommand} - the application command
   */
  public final void setQuitCommand1( L2AppCommand aQuitCmd ) {
    if( aQuitCmd != null ) {
      logger.info( FMT_INFO_L2COMP_INITED_QUIT, kind().id(), aQuitCmd.toString() );
    }
    quitCmd = aQuitCmd;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  /**
   * Subclass may perform additional initialization.
   * <p>
   * Called from {@link #init(ITsContextRo)} when internal fields are initialized and component modules are loaded..
   *
   * @param aArgs {@link ITsContextRo} - the initialization arguments
   * @return {@link ValidationResult} - determines the success of the initialization
   */
  protected abstract ValidationResult doDoInit( ITsContextRo aArgs );

  protected abstract void doDoStart();

  protected abstract void doDoDoJob();

  protected abstract boolean doDoQueryStop();

}

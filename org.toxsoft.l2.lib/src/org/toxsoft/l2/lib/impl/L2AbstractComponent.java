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
 * <p>
 * Notes for subclass implementes:<br>
 * The L2-component contains modules that themselves implement cooperative multitasking within the component. So
 * L2-component executes {@link ITsCooperativeComponent} methods in three steps:
 * <ul>
 * <li><b>before</b> modules are called;</li>
 * <li>modules are called in sequence of their order;</li>
 * <li><b>after</b> the modules are called.</li>
 * </ul>
 * Respective protected methods (to be overridden or implemented) are named as
 * <code>doL2<i>Operation<b>[Before|After]</b>Modules()</i></code>.
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
   * method {@link #setQuitCommand1(L2AppCommand)}.
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

  /**
   * Helper for {@link L2AbstractComponent#callVoid(VoidMethod, String)}.
   *
   * @author hazard157
   */
  interface VoidMethod {
    void call();
  }

  /**
   * Helper caller to mask the exception
   *
   * @param aMethod {@link VoidMethod} - method of this class to be called
   * @param aMethodName String - name of method, used in log error message
   */
  private final void callVoid( VoidMethod aMethod, String aMethodName ) {
    try {
      aMethod.call();
    }
    catch( Exception ex ) {
      LoggerUtils.error( ex, FMT_L2COMP_VOID_ERROR, kind().id(), aMethodName, ex.getMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCooperativeComponent
  //

  @Override
  final protected ValidationResult doInit( ITsContextRo aArgs ) {
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
    vrl.add( doL2Init( aArgs ) );
    return vrl.firstWorstResult();
  }

  @Override
  final protected void doStart() {
    doL2StartBeforeModules();
    boolean throwExOnError = OPDEF_FAIL_APP_ON_MODULE_START_FAIL.getValue( l2Context.params() ).asBool();
    startInitedModules( throwExOnError );
    doL2StartAfterModules();
  }

  @Override
  final protected void doDoJob() {
    checkChangesAndRestartModules();
    callVoid( this::doL2JobBeforeModules, "doL2JobBeforeModules" ); //$NON-NLS-1$
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
    callVoid( this::doL2JobAfterModules, "doL2JobAfterModules" ); //$NON-NLS-1$
  }

  @Override
  final protected boolean doQueryStop() {
    boolean isStopped = true;
    callVoid( this::doL2QueryStopBeforeModules, "doL2QueryStopBeforeModules" ); //$NON-NLS-1$
    for( int i = 0, c = modulesList.size(); i < c; i++ ) { // index iteration to avoid any memory allocation
      L2AbstractModule m = modulesList.get( i );
      if( !m.queryStop() ) {
        isStopped = false;
      }
    }
    return doL2QueryStopAfterModules( isStopped );
  }

  @Override
  final protected boolean doStopping() {
    boolean isStopped = true;
    for( int i = 0, c = modulesList.size(); i < c; i++ ) { // index iteration to avoid any memory allocation
      L2AbstractModule m = modulesList.get( i );
      if( !m.isStopped() ) {
        isStopped = false;
      }
    }
    return doL2IsStopped( isStopped );
  }

  @Override
  protected void doDestroy() {
    callVoid( this::doL2DestroyBeforeModules, "doL2DestroyBeforeModules" ); //$NON-NLS-1$
    for( L2AbstractModule m : modulesList ) {
      m.destroy();
    }
    callVoid( this::doL2DestroyAfterModules, "doL2DestroyAfterModules" ); //$NON-NLS-1$
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
   * Subclass must perform additional, com[component-specific initialization.
   *
   * @param aArgs {@link ITsContextRo} - the initialization arguments
   * @return {@link ValidationResult} - determines the success of the initialization
   */
  protected abstract ValidationResult doL2Init( ITsContextRo aArgs );

  /**
   * Implementation may perform additional actions before modules {@link #start()} method are called.
   * <p>
   * Default implementation does nothing. There is no need to call superclass method when overriding.
   */
  protected void doL2StartBeforeModules() {
    // nop
  }

  /**
   * Implementation may perform additional actions after modules {@link #start()} method are called.
   * <p>
   * Default implementation does nothing. There is no need to call superclass method when overriding.
   */
  protected void doL2StartAfterModules() {
    // nop
  }

  /**
   * Implementation may perform additional actions before modules {@link #doJob()} method are called.
   * <p>
   * Default implementation does nothing. There is no need to call superclass method when overriding.
   */
  protected void doL2JobBeforeModules() {
    // nop
  }

  /**
   * Implementation may perform additional actions after modules {@link #doJob()} method are called.
   * <p>
   * Default implementation does nothing. There is no need to call superclass method when overriding.
   */
  protected void doL2JobAfterModules() {
    // nop
  }

  /**
   * Implemetation may perfiorm actions to correctly stop the component.
   * <p>
   * Called from {@link #isStopped()} before modules are queried to stop.
   * <p>
   * Default implementation does nothing. There is no need to call superclass method when overriding.
   */
  protected void doL2QueryStopBeforeModules() {
    // nop
  }

  /**
   * Implemetation may perfiorm actions to correctly stop the component.
   * <p>
   * Called from {@link #isStopped()} after modules are queried to stop.
   * <p>
   * If all modules has stopped successfully the argument is <code>true</code>. If at least one module are still
   * stopping, <code>aIdAllModulesStopped</code> = <code>false</code>.
   * <p>
   * Default implementation simply returns value of argument. There is no need to call superclass method when
   * overriding.
   *
   * @param aIdAllModulesStopped boolean - <code>true</code> if all modules are stopped already
   * @return <b>true</b> - the component has stopped working;<br>
   *         <b>false</b> - component is still stopping.
   */
  protected boolean doL2QueryStopAfterModules( boolean aIdAllModulesStopped ) {
    return aIdAllModulesStopped;
  }

  /**
   * Implementation may perform additional checks if component has stopped.
   * <p>
   * Called from {@link #isStopped()} after modules are asked.
   * <p>
   * If all modules has stopped successfully the argument is <code>true</code>. If at least one module are still
   * stopping, <code>aIdAllModulesStopped</code> = <code>false</code>.
   * <p>
   * Default implementation simply returns value of argument. There is no need to call superclass method when
   * overriding.
   *
   * @param aIdAllModulesStopped boolean - <code>true</code> if all modules are stopped already
   * @return <b>true</b> - the component has stopped working;<br>
   *         <b>false</b> - component is still stopping.
   */
  protected boolean doL2IsStopped( boolean aIdAllModulesStopped ) {
    return aIdAllModulesStopped;
  }

  /**
   * Called from {@link #destroy()} after modules are destroyed.
   * <p>
   * Any thrown exception will be caught an logged by the called.
   */
  protected abstract void doL2DestroyBeforeModules();

  /**
   * Called from {@link #destroy()} before modules are destroyed.
   * <p>
   * Any thrown exception will be caught an logged by the called.
   */
  protected abstract void doL2DestroyAfterModules();

}

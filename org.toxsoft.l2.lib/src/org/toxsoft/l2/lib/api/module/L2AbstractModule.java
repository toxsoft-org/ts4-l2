package org.toxsoft.l2.lib.api.module;

import static org.toxsoft.l2.lib.api.module.IL2ModuleConfigConstants.*;

import java.util.concurrent.atomic.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * {@link IL2Module} implementation base.
 * <p>
 * Module implements {@link IParameterized} returning parameters as specified in
 * {@link IL2ModuleConfigConstants#L2_HAL_MODCFG_NODEID_MODULE_PARAMS} if any.
 *
 * @author hazard157
 */
public abstract class L2AbstractModule
    extends AbstractTsCoopCompSingleUse
    implements IL2Module, IParameterized {

  private final ILogger        logger;
  private final L2ModuleConfig moduleConfig;
  private final IOptionSet     params;

  /**
   * Flag used in {@link #isDoJobCallable()}.
   * <p>
   * Flag allows subclasses exclude module from cooperative multi-tasking loop calling {@link #doJob()}. Flag may be
   * set/reset by subclass in {@link #setDoJobCallable(boolean)} to temporary avoid {@link #doJob()} calls.
   * <p>
   * There is two main use-cases of using this flag:
   * <ul>
   * <li>common module becomes broken and excluded from cooperative multi-tasking. Because {@link #doJob()} is not
   * called, the module becomes unavailable forever;</li>
   * <li>modules responding to some external threads (like HAL drivers or Net modules bind to Sk-connection callback)
   * may temporary reset this flag to set it from external thread.</li>
   * </ul>
   */
  private AtomicBoolean doJobCallableFlag = new AtomicBoolean( true );

  /**
   * Constructor.
   *
   * @param aOwner {@link L2AbstractComponent} - the owner component creating the module
   * @param aConfig {@link L2ModuleConfig} - the module configuration
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public L2AbstractModule( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig ) {
    TsNullArgumentRtException.checkNulls( aOwner, aConfig );
    logger = LoggerUtils.getLogger( getClass(), l2Context().appId(), owner().kind().id(), id() );
    moduleConfig = aConfig;
    IAvTree moduleParameNode = moduleConfig.cfg().nodes().findByKey( L2_HAL_MODCFG_NODEID_MODULE_PARAMS );
    if( moduleParameNode != null && !moduleParameNode.isArray() ) {
      params = moduleParameNode.fields();
    }
    else {
      params = IOptionSet.NULL;
    }
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  final public String id() {
    return moduleConfig.id();
  }

  @Override
  final public String nmName() {
    return moduleConfig.nmName();
  }

  @Override
  final public String description() {
    return moduleConfig.description();
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  public IOptionSet params() {
    return params;
  }

  // ------------------------------------------------------------------------------------
  // IL2Module
  //

  @Override
  final public L2ModuleConfig moduleConfig() {
    return moduleConfig;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Determines if {@link ITsCooperativeComponent#doJob()} must be called on this module.
   * <p>
   * Note: owner L2 component will be use this module (that is call {@link ITsCooperativeComponent#doJob()}) when
   * component was successfully started, and is not stopped yet.
   *
   * @return boolean - L2 component must use this module
   */
  public boolean isDoJobCallable() {
    boolean flag = switch( compState() ) {
      case CREATED -> false;
      case DESTROYED -> false;
      case INITIALIZED -> false;
      case STOPPING -> true;
      case WORKING -> true;
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    if( flag ) {
      flag = doJobCallableFlag.get();
    }
    return flag;
  }

  /**
   * Controls if method {@link #doJob()} will be called from main loop.
   * <p>
   * Important notes:
   * <ul>
   * <li>method is thread-safe - it is safe to call this method from any thread.;</li>
   * <li>ensure to enable multi-tasking in {@link #doQueryStop()}, otherwise periodical {@link #doStopping()} will not
   * be called!.</li>
   * </ul>
   *
   * @param aFlag boolean - <code>true</code> {@link #doJob()} will be called, <code>false</code> - module is excluded
   *          from the cooperative multi-tasking
   */
  public void setDoJobCallable( boolean aFlag ) {
    doJobCallableFlag.set( aFlag );
  }

  /**
   * Returns the L2 application shared context.
   *
   * @return {@link IL2SharedContext} - the L2 context
   */
  public final IL2SharedContext l2Context() {
    return owner().l2Context();
  }

  /**
   * Returns the module logger.
   *
   * @return {@link ILogger} - the module logger
   */
  public ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompSingleUse
  //

  /**
   * Direct subclass returns the owner component of concrete type.
   *
   * @return {@link L2AbstractComponent} - the owner component creating the module
   */
  public abstract L2AbstractComponent<? extends L2AbstractModule> owner();

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

}

package org.toxsoft.l2.lib.api.module;

import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * {@link IL2Module} implementation base.
 *
 * @author hazard157
 */
public abstract class L2AbstractModule
    extends AbstractTsCoopCompSingleUse
    implements IL2Module {

  private final L2ModuleConfig moduleConfig;

  /**
   * Constructor.
   *
   * @param aOwner {@link L2AbstractComponent} - the owner component creating the module
   * @param aConfig {@link L2ModuleConfig} - the module configuration
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public L2AbstractModule( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig ) {
    TsNullArgumentRtException.checkNulls( aOwner, aConfig );
    moduleConfig = aConfig;
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
    return switch( compState() ) {
      case CREATED -> false;
      case DESTROYED -> false;
      case INITIALIZED -> false;
      case STOPPING -> true;
      case WORKING -> true;
      default -> throw new TsNotAllEnumsUsedRtException();
    };
  }

  /**
   * Returns the L2 application shared context.
   *
   * @return {@link IL2SharedContext} - the L2 context
   */
  public final IL2SharedContext l2Context() {
    return owner().l2Context();
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

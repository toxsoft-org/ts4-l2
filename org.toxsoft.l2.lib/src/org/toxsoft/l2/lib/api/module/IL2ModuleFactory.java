package org.toxsoft.l2.lib.api.module;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * L2 modules factory.
 * <p>
 * Creates modules of type &lt;T&gt; for the specified {@link #componentKind()}.
 *
 * @author hazard157
 * @param <T> - concrete type of the created modules
 */
public sealed interface IL2ModuleFactory<T extends L2AbstractModule>
    extends IStridable
    permits L2AbstractModuleFactory {

  /**
   * Returns the component type for which this factory creates modules..
   *
   * @return {@link EL2ComponentKind} - the component kind
   */
  EL2ComponentKind componentKind();

  /**
   * Creates the module instance based on the specified configuration.
   *
   * @param aOwner {@link L2AbstractComponent} - the owner component creating the module
   * @param aConfig {@link L2ModuleConfig} - the configuration
   * @return &lt;T&gt; - created instance of the module
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException module instance can not be created
   */
  T create( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig );

}

package org.toxsoft.l2.lib.api.module;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.l2.lib.api.module.dlm.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * The repository of the module factories.
 * <p>
 * Repository may have different implementations. For example, based on {@link IPluginManagerComponent} or simply
 * contain pre-defined factories.
 *
 * @author hazard157
 */
public interface IL2ModuleFactoriesRepository {

  /**
   * Returns registered factories of the specified component modules.
   *
   * @param <T> - expected type of modules. subclass of {@link L2AbstractModule}
   * @param aCompKind {@link EL2ComponentKind} - the component kind
   * @return {@link IStridablesListBasicEdit}&lt;T&gt; - list of factories
   */
  <T extends L2AbstractModule> IStridablesList<IL2ModuleFactory<T>> listFactories( EL2ComponentKind aCompKind );

  // ------------------------------------------------------------------------------------
  // inline methods for convenience

  @SuppressWarnings( "javadoc" )
  default IStridablesList<IL2ModuleFactory<L2AbstractDlm>> listDlmFactories() {
    return listFactories( EL2ComponentKind.DLMMGR );
  }

  @SuppressWarnings( "javadoc" )
  default IStridablesList<IL2ModuleFactory<L2AbstractDlm>> listHalFactories() {
    return listFactories( EL2ComponentKind.HAL );
  }

  @SuppressWarnings( "javadoc" )
  default IStridablesList<IL2ModuleFactory<L2AbstractDlm>> listNetFactories() {
    return listFactories( EL2ComponentKind.NETWORK );
  }

}

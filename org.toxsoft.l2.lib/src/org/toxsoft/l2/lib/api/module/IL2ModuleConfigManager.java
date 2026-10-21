package org.toxsoft.l2.lib.api.module;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * Provides information for L2 application components and modules configuration.
 * <p>
 * TYhere may be different implementations of the manager. For example, reading configuration from the files or
 * retrieving from the USkat-server. Predefined and fixed configuration is also possible.
 *
 * @author hazard157
 */
public interface IL2ModuleConfigManager {

  /**
   * Returns current configurations of the specified component modules.
   *
   * @param aCompKind {@link EL2ComponentKind} - asked component kind
   * @return {@link IStridablesList}&lt;{@link L2ModuleConfig}&gt; - list of module configurations
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IStridablesList<L2ModuleConfig> listModuleConfigs( EL2ComponentKind aCompKind );

  /**
   * Returns the manager eventer.
   *
   * @return {@link ITsEventer}&lt;{@link IL2ModuleConfigManagerListener}&gt; - the eventer
   */
  ITsEventer<IL2ModuleConfigManagerListener> eventer();

  // ------------------------------------------------------------------------------------
  // inline methods for convenience

  @SuppressWarnings( "javadoc" )
  default IStridablesList<L2ModuleConfig> listDlmConfigs() {
    return listModuleConfigs( EL2ComponentKind.DLMMGR );
  }

  @SuppressWarnings( "javadoc" )
  default IStridablesList<L2ModuleConfig> listDriverConfigs() {
    return listModuleConfigs( EL2ComponentKind.HAL );
  }

  @SuppressWarnings( "javadoc" )
  default IStridablesList<L2ModuleConfig> listNetConfigs() {
    return listModuleConfigs( EL2ComponentKind.NETWORK );
  }

}

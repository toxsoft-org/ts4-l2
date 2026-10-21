package org.toxsoft.l2.lib.api.module;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.diff.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * {@link IL2ModuleConfigManager} listener.
 * <p>
 * Listener is called when manager finds changes in module configurations while L2 application is running.
 *
 * @author hazard157
 */
public interface IL2ModuleConfigManagerListener {

  /**
   * Informs about found changes in the module configurations.
   * <p>
   * Changes map contains only changed modules configurations. Hence meaningful entries are entries with the keys
   * {@link EDiffNature#LEFT}, {@link EDiffNature#RIGHT} and {@link EDiffNature#DIFF}. Each entry contains list of
   * configurations corresponding to the enum constant: removed (LEFT), added (RIGHT) and changed (DIFF).
   *
   * @param aCompKind {@link EL2ComponentKind} - the kind of component with changed module configurations
   * @param aInfo {@link IMap}&lt;{@link EDiffNature},{@link IList}&lt;L2ModuleConfig}&gt;&gt; - changes map
   */
  void onModuleConfigChange( EL2ComponentKind aCompKind, IMap<EDiffNature, IList<L2ModuleConfig>> aInfo );

}

package org.toxsoft.l2.lib;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.common.*;

/**
 * Base interface of the L2 framework component, either HAL, DLM manager of network.
 * <p>
 * {@link #params()} contains component-specific initialization options.
 *
 * @author hazard157
 */
public interface IL2Component
    extends IParameterized {

  /**
   * Returns the L2 application shared context.
   *
   * @return {@link IL2SharedContext} - the L2 context
   */
  IL2SharedContext l2Context();

  /**
   * Returns the kind of this component.
   *
   * @return {@link EL2ComponentKind} - the component kind
   */
  EL2ComponentKind kind();

  /**
   * Returns application quit command if quit was initialized by this component.
   * <p>
   * Note that depending on {@link L2AppQuitCommand#exitCode()} application may request restart, not a finish.
   *
   * @return {@link L2AppQuitCommand} - quit command or <code>null</code>
   */
  L2AppQuitCommand getQuitCommandIfAny();

}

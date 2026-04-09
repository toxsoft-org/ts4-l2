package org.toxsoft.l2.lib;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.l2.lib.app.*;

/**
 * Base interface of the L2 framework component, either HAL, DLM manager of network.
 * <p>
 * {@link #params()} contains component-specific inititlization options.
 *
 * @author hazard157
 */
public interface IL2Component
    extends IParameterized {

  /**
   * Returns the owner application.
   *
   * @return {@link IL2Application} - application - creator and owner of this component
   */
  IL2Application l2App();

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

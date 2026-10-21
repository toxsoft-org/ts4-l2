package org.toxsoft.l2.lib.impl.comps;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.app.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.impl.*;

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
   * Returns the kind of this component.
   *
   * @return {@link EL2ComponentKind} - the component kind
   */
  EL2ComponentKind kind();

  /**
   * Returns the L2 application shared context.
   *
   * @return {@link IL2SharedContext} - the L2 context
   */
  IL2SharedContext l2Context();

  /**
   * Returns application quit command if quit was initialized by this component.
   * <p>
   * Note that depending on {@link L2AppCommand#code()} application may request restart, not a finish.
   *
   * @return {@link L2AppCommand} - quit command or <code>null</code>
   */
  L2AppCommand getQuitCommandIfAny();

  /**
   * Lists currently running modules of this component.
   * <p>
   * During internal module that fails any step (creation/initialization/starting) is removed from the list, When
   * running, list of modules remains intact.
   *
   * @param <T> - expected module interface extending {@link IL2Module}
   * @return {@link IStridablesList}&lt;T&gt; - list of running modules
   */
  <T extends IL2Module> IStridablesList<T> modules();

  /**
   * Finds module with runtime type check.
   * <p>
   * Does not throws an exception, returns <code>null</code> instead.
   *
   * @param <T> - expected type of module
   * @param aModuleId String - the module ID
   * @param aExpectedClass {@link Class}&lt;T&gt; - expected Java class of the module
   * @return &lt;T&gt; - found module or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  <T extends IL2Module> T findModule( String aModuleId, Class<T> aExpectedClass );

  /**
   * Finds module with runtime type check.
   *
   * @param <T> - expected type of module
   * @param aModuleId String - the module ID
   * @param aExpectedClass {@link Class}&lt;T&gt; - expected Java class of the module
   * @return &lt;T&gt; - the module
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException module woth spe specified ID does not exists
   * @throws ClassCastException found module is not of the specified type
   */
  <T extends IL2Module> T getModule( String aModuleId, Class<T> aExpectedClass );

}

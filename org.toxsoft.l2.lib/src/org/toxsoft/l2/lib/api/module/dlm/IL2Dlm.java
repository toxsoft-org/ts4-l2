package org.toxsoft.l2.lib.api.module.dlm;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.l2.lib.api.module.*;

/**
 * The basic interface of the Dynamically Loadable Modules (DLM) implementation.
 * <p>
 * Implements {@link IStridable}, where:
 * <ul>
 * <li>{@link #id()} - is the DLM instance ID;</li>
 * <li>{@link #nmName()} - the instance name, just for developer to distinguish instances;</li>
 * <li>{@link #description()} - the instance description, usually empty, not used;</li>
 * </ul>
 *
 * @author hazard157
 */
public interface IL2Dlm
    extends IL2Module {

  // nop

}

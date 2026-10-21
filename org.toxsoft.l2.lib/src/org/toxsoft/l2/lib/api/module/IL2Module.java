package org.toxsoft.l2.lib.api.module;

import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Base interface of L2 modules (DLMs. HAL device drivers, network modules).
 * <p>
 * Implements {@link IStridable} returning the same values as stridable {@link L2ModuleConfig},
 * <p>
 * The only allowed implementation {@link L2AbstractModule} is a cooperative single-use component extending
 * {@link AbstractTsCoopCompSingleUse}.
 *
 * @author hazard157
 */
public interface IL2Module
    extends IStridable {

  /**
   * Returns configuration of the module.
   *
   * @return {@link L2ModuleConfig} - the module configuration
   */
  L2ModuleConfig moduleConfig();

}

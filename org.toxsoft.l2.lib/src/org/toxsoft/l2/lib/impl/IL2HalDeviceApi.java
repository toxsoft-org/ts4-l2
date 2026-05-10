package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * HAL device package-private API used by {@link L2Hal}.
 * <p>
 * The only implementation of this interface is {@link L2AbstractHalDevice}. {@link L2AbstractHalDevice} has
 * {@link IL2SharedContext} and respective {@link L2ModuleConfigFile} specified in constructor.
 *
 * @author hazard157
 */
sealed interface IL2HalDeviceApi
    extends IWorkerComponent
    permits L2AbstractHalDevice {

  /**
   * Returns tags created by the device (if any).
   * <p>
   * Some devices (called <i>specific devices</i>) does not create tags rather they are publishing device-dependent API
   * to be used by DLMs.
   * <p>
   * Note: IDs of created tags must be unique though all tags of the HAL {@link IL2Hal#tags()}.
   *
   * @return {@link IStridablesList}&lt;{@link L2AbstractTag}&gt; - L2 tag implementations list
   */
  IStridablesList<L2AbstractTag> getTags();

}

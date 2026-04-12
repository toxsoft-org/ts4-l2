package org.toxsoft.l2.lib.hal;

import org.toxsoft.core.tslib.bricks.strid.coll.*;

/**
 * HAL - I/O Hardware Abstraction Layer provides L2 entities access to the physical world data.
 *
 * @author hazard157
 */
public interface IL2Hal
    extends IHealthMeasurable {

  /**
   * Returns all tags in provided by I/O to the physical world.
   *
   * @return {@link IStridablesList}&lt;{@link IL2Tag}&gt; - list of tags
   */
  IStridablesList<IL2Tag> tags();

  /**
   * Returns all device drivers.
   *
   * @return {@link IStridablesList}&lt;{@link IL2HalDevice}&gt; - list of devices
   */
  IStridablesList<IL2HalDevice> deviceList();

}

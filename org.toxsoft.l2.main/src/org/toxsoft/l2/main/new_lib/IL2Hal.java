package org.toxsoft.l2.main.new_lib;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.l2.lib.hal.devices.*;
import org.toxsoft.l2.lib.reserve.*;

/**
 * HAL - I/O Hardware Abstraction Layer provides L2 entities access to the physical world data.
 * <p>
 * {@link #params()} returns the HAL configuration option values.
 *
 * @author hazard157
 */
public interface IL2Hal
    extends IHealthMeasurable, IParameterized {

  /**
   * Returns all tags in provided by I/O to the physical world.
   *
   * @return {@link IStridablesList}&lt;{@link IL2Tag}&gt; - list of tags
   */
  IStridablesList<IL2Tag> tags();

  /**
   * Returns all registered specific devices.
   *
   * @return {@link IStridablesList}&lt;{@link ISpecificDevice}&gt; - list of specific devices
   */
  IStridablesList<ISpecificDevice> specificDevices();

}

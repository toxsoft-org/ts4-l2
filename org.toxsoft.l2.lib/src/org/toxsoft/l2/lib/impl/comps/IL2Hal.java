package org.toxsoft.l2.lib.impl.comps;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.module.hal.*;

/**
 * HAL - I/O Hardware Abstraction Layer provides L2 entities access to the physical world data.
 *
 * @author hazard157
 */
public interface IL2Hal
    extends IHealthMeasurable, IL2Component {

  /**
   * Returns all device drivers.
   *
   * @return {@link IStridablesList}&lt;{@link IL2HalDriver}&gt; - list of devices
   */
  IStridablesList<IL2HalDriver> drivers();

  /**
   * Returns all tags in provided by I/O to the physical world.
   *
   * @return {@link IStridablesList}&lt;{@link IL2Tag}&gt; - list of tags
   */
  IStridablesList<IL2Tag> tags();

  /**
   * Returns the tags provided by the asked device.
   * <p>
   * This list may contains tags not in {@link #tags()}. During HAL initialization each device supplies tags to the
   * global list {@link #tags()}. If device tries to add tag with the existing ID then warning will be logged and tag
   * will not be added to {@link #tags()}, however ramaining in the device-provided tags list.
   *
   * @param aDeviceId String - ID of the tags provider device
   * @return {@link IStridablesList}&lt;{@link IL2HalDriver}&gt; - list of tags by device
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException nbo such device
   */
  IStridablesList<IL2Tag> listDeviceTags( String aDeviceId );

}

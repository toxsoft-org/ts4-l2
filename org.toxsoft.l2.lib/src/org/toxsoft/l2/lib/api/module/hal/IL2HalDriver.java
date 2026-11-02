package org.toxsoft.l2.lib.api.module.hal;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * Interface of HAL I/O device driver, either tag producer or specific one.
 *
 * @author hazard157
 */
public interface IL2HalDriver
    extends IL2Module {

  /**
   * Returns the tags provided by this driver.
   *
   * @return {@link IStridablesList}&lt;{@link L2Tag}&gt; - the tags list
   */
  IStridablesList<L2Tag> tags();

}

package org.toxsoft.l2.lib.hal;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * Factory creates the instance of the HAL device driver {@link L2AbstractHalDevice}.
 *
 * @author hazard157
 */
public interface IL2HalDeviceFactory {

  /**
   * Creates the device driver instance based on specified configuration.
   *
   * @param aCfg {@link L2ModuleConfigFile} - the device configuration, never is <code>null</code>
   * @return {@link L2AbstractHalDevice} - created instance
   * @throws TsRuntimeException or subclasses are thrown on any creation error
   */
  L2AbstractHalDevice createDevice( L2ModuleConfigFile aCfg );

}

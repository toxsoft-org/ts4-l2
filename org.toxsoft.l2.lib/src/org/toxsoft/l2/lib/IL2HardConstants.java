package org.toxsoft.l2.lib;

import static org.toxsoft.core.tslib.ITsHardConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.math.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.l2.lib.dlms.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * L2 subsystem hard-coded constants.
 *
 * @author hazard157
 */
public interface IL2HardConstants {

  /**
   * L2 entities short ID prefix.
   */
  String L2_ID = "l2"; //$NON-NLS-1$ general short ID prefix (IDname)

  /**
   * L2 entities full ID prefix.
   */
  String L2_FULL_ID = TS_FULL_ID + "." + L2_ID; //$NON-NLS-1$ general full ID prefix (IDpath)

  /**
   * Allowed range of the L2 entities health value.
   * <p>
   * The {@link IntRange#maxValue()} means absolutely healthy entity, {@link IntRange#minValue()} is a dead one.
   */
  IntRange L2_HEALTH_RANGE = new IntRange( 0, 100 );

  /**
   * Allowed range of the L2 entities health contribution measure to the system overall health.
   * <p>
   * The {@link IntRange#maxValue()} means most influent entity, {@link IntRange#minValue()} is a least important one.
   */
  IntRange L2_HEALTH_CONTRIBUTION_RANGE = new IntRange( 1, 10 );

  /**
   * Default value for health contribution if not specified.
   */
  int DEFAULT_HEALTH_CONTRIBUTION = 5;

  // ------------------------------------------------------------------------------------
  // L2 component: DLM manager

  /**
   * Type of the plugin containing the DLM.
   * <p>
   * This constant is used as the value of the {@link IPluginsHardConstants#PLUGIN_TYPE_ID} parameter. Plugin JAR files
   * must be created according to the rules of the {@link IPluginManagerComponent} plugin subsystem from the tslib
   * library.
   */
  String DLM_PLUGIN_TYPE_ID = "DLM"; //$NON-NLS-1$

  /**
   * The name of the property {@link DlmInfo#moduleName()} in the corresponding section of the plugin JAR file manifest.
   */
  String MF_ATTR_DLM_NAME = "DlmName"; //$NON-NLS-1$

  /**
   * Extension (without dot) of the HAL I/O device driver configuration files.
   */
  String DLM_MODULE_CONFIG_FILE_EXT = "dlmcfg"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // L2 component: HAL

  /**
   * Extension (without dot) of the HAL I/O device driver configuration files.
   */
  String HAL_MODULE_CONFIG_FILE_EXT = "devcfg"; //$NON-NLS-1$

  /**
   * Field name containing full name of {@link IL2HalDeviceFactory} implementation.
   * <p>
   * This is the option of type {@link EAtomicType#STRING} in device configuration {@link IAvTree#fields()}.
   */
  String HAL_DEVICE_PARAM_PRODUCER_CLASS = "DeviceProducerClass"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // L2 component: Network

  /**
   * Extension (without dot) of the HAL I/O device driver configuration files.
   */
  String NET_MODULE_CONFIG_FILE_EXT = "netcfg"; //$NON-NLS-1$

}

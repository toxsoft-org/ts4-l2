package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Base
 *
 * @author hazard157
 */
public class L2AbstractHalDevice
    implements IL2HalDevice {

  private final ILogger          logger;
  private final IL2SharedContext l2Context;
  private final String           deviceId;
  private final String           nmName;
  private final String           description;
  private final IAvTree          config;

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2Component} - L2 application context
   * @param aDeviceCfg {@link L2ModuleConfigFile} - device configuration
   */
  public L2AbstractHalDevice( IL2SharedContext aL2Context, L2ModuleConfigFile aDeviceCfg ) {
    l2Context = aL2Context;
    logger = LoggerUtils.getLogger( getClass(), l2Context.appId() );
    deviceId = aDeviceCfg.id();
    nmName = aDeviceCfg.nmName();
    description = aDeviceCfg.description();
    config = aDeviceCfg.cfg();
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  final public String id() {
    return deviceId;
  }

  @Override
  final public String nmName() {
    return nmName;
  }

  @Override
  final public String description() {
    return description;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the configuration, specified i the constructor.
   *
   * @return {@link IAvTree} - specified device configuration
   */
  final public IAvTree config() {
    return config;
  }

  /**
   * Returns the logger.
   *
   * @return {@link ILogger} - the logger
   */
  final ILogger logger() {
    return logger;
  }

}

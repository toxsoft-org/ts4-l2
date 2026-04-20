package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
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
public non-sealed abstract class L2AbstractHalDevice
    implements IL2HalDevice, IL2HalDeviceApi {

  private final ILogger          logger;
  private final IL2SharedContext l2Context;
  private final String           deviceId;
  private final String           nmName;
  private final String           description;
  private final IAvTree          config;

  /**
   * Tags created by the subclass in the method {@link #getTags()}.
   */
  private IStridablesList<L2AbstractTag> createdTags = null;

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
  // API for subclasses
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

  // ------------------------------------------------------------------------------------
  // IWorkerComponent
  //

  @Override
  final public void start() {
    // TODO Auto-generated method stub

  }

  @Override
  final public boolean queryStop() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  final public boolean isStopped() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  final public void destroy() {
    // TODO Auto-generated method stub

  }

  // ------------------------------------------------------------------------------------
  // IL2HalDeviceApi
  //

  @Override
  public IStridablesList<L2AbstractTag> getTags() {
    if( createdTags == null ) {
      createdTags = doCreateTags();
      TsInternalErrorRtException.checkNull( createdTags );
    }
    return createdTags;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  /**
   * Subclass must create and return tag implementations.
   * <p>
   * This method is guaranteed to be called once in this instance lifetime.
   * <p>
   * Returned list may be empty for specific devices.
   *
   * @return {@link IStridablesList}&lt;{@link L2AbstractTag}&gt; - created instance of the tags list
   */
  protected abstract IStridablesList<L2AbstractTag> doCreateTags();

}

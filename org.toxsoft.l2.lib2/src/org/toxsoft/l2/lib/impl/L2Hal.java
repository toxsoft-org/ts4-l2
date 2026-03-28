package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
class L2Hal
    implements IL2Hal, ICooperativeWorkerComponent {

  /**
   * Constructor.
   */
  public L2Hal() {
    // TODO HalImpl.HalImpl()
  }

  // ------------------------------------------------------------------------------------
  // IL2Hal
  //

  @Override
  public IStridablesList<IL2Signal> signals() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStridablesList<IL2SpecificDevice> specificDevices() {
    // TODO Auto-generated method stub
    return null;
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeWorkerComponent
  //

  @Override
  public void start() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean queryStop() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isStopped() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  public void doJob() {
    // TODO Auto-generated method stub

  }

}

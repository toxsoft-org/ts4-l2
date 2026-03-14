package org.toxsoft.l2.main;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.wub.*;

public class L2Application
    extends AbstractWubUnit {

  public L2Application( String aId, IOptionSet aParame ) {
    super( aId, aParame );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // AbstractWubUnit
  //

  @Override
  protected ValidationResult doInit( ITsContextRo aEnviron ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doStart() {
    // TODO Auto-generated method stub
    super.doStart();
  }

  @Override
  protected void doDoJob() {
    // TODO Auto-generated method stub

  }

  @Override
  protected boolean doQueryStop() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected boolean doStopping() {
    // TODO Auto-generated method stub
    return super.doStopping();
  }

  @Override
  protected void doDestroy() {
    // TODO Auto-generated method stub
    super.doDestroy();
  }

}

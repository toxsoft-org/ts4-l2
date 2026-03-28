package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;

/**
 * L2 core - the actual runner of the L2 application.
 * <p>
 * The L2 core instance is created when {@link L2Application} starts end is destroyed when at stop.
 *
 * @author hazard157
 */
class L2Core {

  record L2CoreContext ( ITsThreadExecutor guard, IOptionSet ops, long startTime, L2Hal hal, L2DlmManager dlmMgr,
      L2Network net ) {
  }

  public L2Core( ITsContextRo aArgs ) {

    // TODO Auto-generated constructor stub

  }

}

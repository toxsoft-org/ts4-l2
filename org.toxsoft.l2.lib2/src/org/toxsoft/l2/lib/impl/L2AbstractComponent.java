package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;

/**
 * Base implementation of L2 core components.
 *
 * @author hazard157
 */
class L2AbstractComponent {

  private final ITsThreadExecutor threadGuard;

  protected L2AbstractComponent( ITsContextRo aArgs ) {
    threadGuard = REFDEF_MAIN_THREAD_GUARD.getRef( aArgs );
  }

}

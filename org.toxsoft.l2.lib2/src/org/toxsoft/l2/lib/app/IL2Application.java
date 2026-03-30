package org.toxsoft.l2.lib.app;

import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.wub.*;

/**
 * Interface used to create and run L2 applications in different environmanets.
 * <p>
 * L2 application is implemented as {@link IWubUnit} and may be run both in containres and as a atandalone application
 * directly from <code>main()</code> method.
 *
 * @author hazard157
 */
public interface IL2Application
    extends ITsCooperativeComponent {

  /**
   * Returns the application identifier used in logs and environment with several L2 applications.
   *
   * @return String - the L2 application ID (an IDpath)
   */
  String appId();

  /**
   * Returns application quit command if quit was initialized by application itself.
   * <p>
   * Note that depending on {@link L2AppQuitCommand#equals(Object)} application may request restart, not a finish.
   * <p>
   * Program quit may be requested for several reasons: remotely by network component or restart will be needed to apply
   * configuration file change.
   *
   * @return {@link L2AppQuitCommand} - quit command or <code>null</code>
   */
  L2AppQuitCommand getQuitCommandIfAny();

}

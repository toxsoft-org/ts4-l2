package org.toxsoft.l2.lib.api.app;

import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.wub.*;
import org.toxsoft.l2.lib.*;

/**
 * Interface used to create and run L2 applications in different environments.
 * <p>
 * L2 application is implemented as {@link IWubUnit} and may be run both in containers and as a standalone application
 * directly from <code>main()</code> method.
 * <p>
 * Note: each :L2 application must have corresponding Sk-object (of class {@link IL2HardConstants#CLSID_L2_APPLICATION})
 * defined in the USkat server. While Sk-object STRID is fixed in the USkat, L2 application ID {@link #appId()} is used
 * as a STRID when establishing connection to the USkat server.
 *
 * @author hazard157
 */
public interface IL2Application
    extends ITsCooperativeComponent {

  /**
   * Returns the application identifier used in logs and environment with several L2 applications.
   * <p>
   * This is the STRID of application Sk-object in USkat server.
   *
   * @return String - the L2 application ID (an IDpath)
   */
  String appId();

  /**
   * Returns application quit command if quit was initialized by application itself.
   * <p>
   * Note that depending on {@link L2AppCommand#code()} application may request restart, not a finish.
   * <p>
   * Program quit may be requested for several reasons: remotely by network component or restart will be needed to apply
   * configuration file change.
   *
   * @return {@link L2AppCommand} - quit command or <code>null</code>
   */
  L2AppCommand getQuitCommandIfAny();

}

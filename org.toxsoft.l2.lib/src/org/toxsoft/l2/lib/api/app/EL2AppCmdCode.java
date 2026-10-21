package org.toxsoft.l2.lib.api.app;

import org.toxsoft.l2.lib.impl.*;

/**
 * Code of application command (exit code of quit command).
 *
 * @author max
 */
public enum EL2AppCmdCode {

  /**
   * L2 application restart requested.
   * <p>
   * This exit code is never returned by program. It is used to restart existing instant {@link L2Application}.
   */
  CODE_RESTART_L2APP( -1 ),

  /**
   * L2 application terminated normally.
   */
  CODE_OK( 0 ),

  /**
   * Help message was displayed by the request, L2 application not even started.
   */
  CODE_HELP_DISPLAYED( 1 ),

  /**
   * Connection to USkat server can't be established, application can't start.
   */
  CODE_CONN_OPEN_FAILED( 3 ),

  /**
   * L2 application initialization failure, possibly incorrect configuration.
   */
  CODE_INIT_FAILED( 4 ),

  /**
   * L2 application start failure, possibly incorrect runtime environment.
   */
  CODE_START_FAILED( 5 ),

  /**
   * L2 application normal finish was requested but stop take too long time and was foced to shut down.
   */
  CODE_STOP_TIMEOUTED( 6 );

  private final int retCode;

  EL2AppCmdCode( int aRetCode ) {
    retCode = aRetCode;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns program return code corresponding to the enum constant.
   *
   * @return int - program return code when L2 application ended with this constant
   */
  public int getRetCode() {
    return retCode;
  }

}

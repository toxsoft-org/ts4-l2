package org.toxsoft.l2.lib.app;

import org.toxsoft.l2.lib.impl.*;

/**
 * Code of application command (exit code of quit command)
 *
 * @author max
 */
public enum EL2AppCmdCode {

  /**
   * This exit code is never returned by program.
   * <p>
   * It is used internally to restart {@link L2Application} in program.
   */
  CODE_RESTART_L2APP( -1 ),

  CODE_OK( 0 ),

  CODE_HELP_DISPLAYED( 1 ),

  CODE_CONN_OPEN_FAILED( 3 ),

  CODE_INIT_FAILED( 4 ),

  CODE_START_FAILED( 5 ),

  CODE_STOP_TIMEOUTED( 6 );

  private int code;

  EL2AppCmdCode( int aCode ) {
    code = aCode;
  }

  public int getCode() {
    return code;
  }

}

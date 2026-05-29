package org.toxsoft.l2.lib.app;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Command to finish/restart L2 application.
 *
 * @author hazard157
 * @param code EL2AppCmdCode - the application exit code, never is <code>null</code>
 * @param message String - the message, never is <code>null</code>
 */
public record L2AppCommand ( EL2AppCmdCode code, String message ) {

  /**
   * Constructor.
   *
   * @param code EL2AppCmdCode - the application exit code
   * @param message String - the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public L2AppCommand( EL2AppCmdCode code, String message ) {
    TsNullArgumentRtException.checkNulls( code, message );
    this.code = code;
    this.message = message;
  }

}

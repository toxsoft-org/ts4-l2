package org.toxsoft.l2.new_app;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Command to finish/restart L2 application.
 *
 * @author hazard157
 * @param exitCode short - the application exit code
 * @param message String - the message, never is <code>null</code>
 */
public record L2AppQuitCommand ( short exitCode, String message ) {

  /**
   * Constructor.
   *
   * @param exitCode short - the application exit code
   * @param message String - the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public L2AppQuitCommand( short exitCode, String message ) {
    TsNullArgumentRtException.checkNull( message );
    this.exitCode = exitCode;
    this.message = message;
  }

}

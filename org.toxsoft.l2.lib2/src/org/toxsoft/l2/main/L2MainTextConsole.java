package org.toxsoft.l2.main;

import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.l2.lib.app.*;

/**
 * Optional class to initialize console for the {@link L2Main} application.
 * <p>
 * The text console runs in separate thread.
 * <p>
 * All methods are thread-safe.
 *
 * @author hazard157
 */
public class L2MainTextConsole
    implements ICloseable {

  // FIXME class needs to be developed

  public L2MainTextConsole() {

    // TODO L2MainTextConsole.L2MainTextConsole()

  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {

    // TODO L2MainTextConsole.close()

  }

  // ------------------------------------------------------------------------------------
  // API
  //

  public L2AppQuitCommand getQuitCommandIfAny() {

    // TODO L2MainTextConsole.getQuitCommandIfAny()

    return null;
  }

}

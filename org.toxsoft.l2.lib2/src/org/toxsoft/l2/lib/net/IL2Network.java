package org.toxsoft.l2.lib.net;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * L2 Network - communication to the USkat server via network.
 *
 * @author hazard157
 */
public interface IL2Network {

  /**
   * Returns the connection to the USkat server.
   * <p>
   * Connection may be used only from L2 application main thread, only from {@link ICooperativeMultiTaskable#doJob()}.
   *
   * @return {@link ISkConnection} - connection used for network communication
   */
  ISkConnection getSkConnection();

}

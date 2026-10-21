package org.toxsoft.l2.lib.impl.comps;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.l2.lib.api.module.net.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * L2 Network - communication to the USkat server via network.
 *
 * @author hazard157
 */
public interface IL2Network
    extends IL2Component {

  /**
   * Returns all network modules.
   *
   * @return {@link IStridablesList}&lt;{@link IL2NetModule}&gt; - list of network modules
   */
  IStridablesList<IL2NetModule> netModules();

  /**
   * Returns the connection to the USkat server.
   * <p>
   * Connection may be used only from L2 application main thread, only from {@link ICooperativeMultiTaskable#doJob()}.
   *
   * @return {@link ISkConnection} - connection used for network communication
   */
  ISkConnection getSkConnection();
}

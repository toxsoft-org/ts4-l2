package org.toxsoft.l2.lib.dlms;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.l2.lib.*;

/**
 * DLM Manager - manages Dynamic Loadable Modules.
 *
 * @author hazard157
 */
public interface IL2DlmManager
    extends IL2Component {

  /**
   * Returns list of active DLM in the current state of the DLM manager.
   * <p>
   * Some DLMs may fail to start during initialization and startup, and the manager will remove them from the list. Even
   * during normal operation, the manager may remove a faulty DLM.
   *
   * @return {@link IList}&lt;{@link IDlm}&gt; - DLMs list
   */
  IStridablesList<IStridable> dlms();

}

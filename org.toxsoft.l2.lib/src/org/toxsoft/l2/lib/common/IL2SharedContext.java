package org.toxsoft.l2.lib.common;

import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.dlms.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.net.*;

/**
 * Context (references and options) shared between all components and modules.
 * <p>
 * Important note: during the application initialization when components and modules are created som methods of this
 * interface may return <code>null</code> values or even throw an exception.
 *
 * @author hazard157
 */
public interface IL2SharedContext {

  /**
   * Returns the application identifier used in logs and environment with several L2 applications.
   *
   * @return String - the L2 application ID (an IDpath)
   */
  String appId();

  /**
   * Returns the HAL component;
   *
   * @return {@link IL2Hal} - the HAL component
   * @throws TsIllegalStateRtException component was not created yet
   */
  IL2Hal hal();

  /**
   * Returns the DLM manager component;
   *
   * @return {@link IL2DlmManager} - the DLM manager component;
   * @throws TsIllegalStateRtException component was not created yet
   */
  IL2DlmManager dlmMgr();

  /**
   * Returns the network component;
   *
   * @return {@link IL2Network} - the network component
   * @throws TsIllegalStateRtException component was not created yet
   */
  IL2Network net();

  /**
   * Returns the main loop thread guardian.
   *
   * @return {@link ITsThreadExecutor} - thread safety means
   */
  ITsThreadExecutor threadGuard();

}

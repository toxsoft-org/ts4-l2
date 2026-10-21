package org.toxsoft.l2.lib.impl;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.app.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * Context (references and options) shared between all components and modules.
 * <p>
 * Important note: during the application initialization when components and modules are created, some methods of this
 * interface may return <code>null</code> values or even throw an exception.
 * <p>
 * {@link #params()} returns global options passes as parameters of {@link IL2Application#init(ITsContextRo)}. That is
 * returns the same as {@link #args()} args().params()}.
 *
 * @author hazard157
 */
public interface IL2SharedContext
    extends IParameterized {

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

  /**
   * Returns current sweep step of the L2 application.
   * <p>
   * Components and modules may perform different action depending on current sweep step. For example, HAL will read
   * device drivers on {@link EL2SweepStep#IO_READ} and write outputs on {@link EL2SweepStep#IO_WRITE}.
   *
   * @return {@link EL2SweepStep} - steep step
   */
  EL2SweepStep sweepStep();

  /**
   * Returns sweep starting time in milliseconds get by {@link System#currentTimeMillis()}.
   *
   * @return long - system time in milliseconds
   */
  long sweepStartTime();

  /**
   * Returns sweep starting time in nanoseconds get by {@link System#nanoTime()}.
   *
   * @return long - relative time in nanoseconds
   */
  long sweepStartNanos();

  /**
   * Returns the reference to arguments passed by the user to {@link IL2Application#init(ITsContextRo)},
   *
   * @return {@link ITsContextRo} - L2 application initialization arguments
   */
  ITsContextRo args();

  /**
   * Returns module configuration manager,
   * <p>
   * Manager is specified by the L2 application user as a reference
   * {@link IL2ApplicationArguments#REFDEF_L2_MODULE_CONFIG_MANAGER} in method
   * {@link IL2Application#init(ITsContextRo)}.
   *
   * @return {@link IL2ModuleConfigManager} - module configuration manager,
   */
  IL2ModuleConfigManager moduleConfigManager();

  /**
   * Returns module factories repository,
   * <p>
   * Repository is specified by the L2 application user as a reference
   * {@link IL2ApplicationArguments#REFDEF_L2_MODULE_FACTORIES_REPOSITORY} in method
   * {@link IL2Application#init(ITsContextRo)}.
   *
   * @return {@link IL2ModuleConfigManager} - module factories repository,
   */
  IL2ModuleFactoriesRepository moduleFactoriesRepository();

}

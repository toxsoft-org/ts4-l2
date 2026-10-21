package org.toxsoft.l2.lib.api.app;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Mandatory and known arguments to be passed to {@link IL2Application#init(ITsContextRo)}.
 * <p>
 * Arguments are references placed in {@link ITsContextRo}, and options in {@link ITsContextRo#params()}.
 *
 * @author hazard157
 */
public interface IL2ApplicationArguments {

  // ------------------------------------------------------------------------------------
  // References
  //

  /**
   * Initialization reference: thread guard of type {@link ITsThreadExecutor}.
   */
  ITsContextRefDef<ITsThreadExecutor> REFDEF_L2_MAIN_THREAD_GUARD = TsContextRefDef.create( //
      "l2.app.ctxref.MainThreadGuard", //$NON-NLS-1$
      ITsThreadExecutor.class, //
      TSID_IS_MANDATORY, AV_TRUE//
  );

  /**
   * Initialization reference: module configuration provider of type {@link IL2ModuleConfigManager}.
   */
  ITsContextRefDef<IL2ModuleConfigManager> REFDEF_L2_MODULE_CONFIG_MANAGER = TsContextRefDef.create( //
      "l2.app.ctxref.L2ModuleConfigManager", //$NON-NLS-1$
      IL2ModuleConfigManager.class, //
      TSID_IS_MANDATORY, AV_TRUE//
  );

  /**
   * Initialization reference: module factories repository of type {@link IL2ModuleFactoriesRepository}.
   */
  ITsContextRefDef<IL2ModuleFactoriesRepository> REFDEF_L2_MODULE_FACTORIES_REPOSITORY = TsContextRefDef.create( //
      "l2.app.ctxref.L2ModuleFactoriesRepository", //$NON-NLS-1$
      IL2ModuleFactoriesRepository.class, //
      TSID_IS_MANDATORY, AV_TRUE//
  );

  /**
   * Initialization reference: connection to the USkat server of type {@link ISkConnection}.
   */
  ITsContextRefDef<ISkConnection> REFDEF_SK_CONNECTION = TsContextRefDef.create( //
      "l2.app.ctxref.SkConnection", //$NON-NLS-1$
      ISkConnection.class, //
      TSID_IS_MANDATORY, AV_TRUE//
  );

  // ------------------------------------------------------------------------------------
  // Options
  //

  /**
   * The flag to interpret module load/init/start as critical failure rather than ignore failure.
   */
  IDataDef OPDEF_FAIL_APP_ON_MODULE_START_FAIL = DataDef.create( "l2.abort_on_module_init_failure", BOOLEAN, //$NON-NLS-1$
      TSID_NAME, "Abort on fail?", //
      TSID_NAME, "The flag to interpret module load/init/start as critical failure rather than ignore failure", //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  /**
   * Common option definitions.
   */
  IStridablesListEdit<IDataDef> ALL_COMMON_OPDEFS = new StridablesList<>( //
      OPDEF_FAIL_APP_ON_MODULE_START_FAIL //
  );

  /**
   * DLM manager component option definitions.
   */
  IStridablesListEdit<IDataDef> ALL_DLMMGR_COMP_OPDEFS = new StridablesList<>( //
  //
  );

  /**
   * HAL component option definitions.
   */
  IStridablesListEdit<IDataDef> ALL_HAL_COMP_OPDEFS = new StridablesList<>( //
  //
  );

  /**
   * Network component option definitions.
   */
  IStridablesListEdit<IDataDef> ALL_NET_COMP_OPDEFS = new StridablesList<>( //
  //
  );

  /**
   * Returns all options listed above.
   *
   * @return {@link IStridablesListEdit}&lt;{@link IDataDef}&gt; - all knoen global options
   */
  static IStridablesListEdit<IDataDef> listAllGlobalOpDefs() {
    IStridablesListEdit<IDataDef> llOps = new StridablesList<>();
    llOps.addAll( ALL_COMMON_OPDEFS );
    llOps.addAll( ALL_DLMMGR_COMP_OPDEFS );
    llOps.addAll( ALL_HAL_COMP_OPDEFS );
    llOps.addAll( ALL_NET_COMP_OPDEFS );
    return llOps;
  }

}

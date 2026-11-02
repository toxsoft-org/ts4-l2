package org.toxsoft.l2.lib.api.module;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.l2.lib.api.app.*;

/**
 * Built-in L2 modules configuration constants.
 *
 * @author hazard157
 */
public interface IL2ModuleConfigConstants {

  /**
   * Optional module configuration node ontaining module-specific parameters {@link L2AbstractModule#params()}.
   * <p>
   * Any {@link L2ModuleConfig#cfg()} may have node with this ID. If node is a single (i.e. {@link IAvTree#isArray()} =
   * <code>false</code>) then {@link IAvTree#fields()} will be used as {@link L2AbstractModule#params()}.
   */
  String L2_HAL_MODCFG_NODEID_MODULE_PARAMS = "l2.ModuleParams"; //$NON-NLS-1$

  /**
   * HAL driver module configuration tree node ID in {@link L2ModuleConfig#cfg()}.
   * <p>
   * This is the mandatory node for HAL driver configuration. Each {@link L2ModuleConfig#cfg()} must have node with this
   * ID. Node must be a single, not an array (i.e. {@link IAvTree#isArray()} = <code>false</code>). Node fields
   * {@link IAvTree#fields()} contains pairs "Tag ID" - "Tag info with the same ID". Tag configuration is a string
   * created with {@link IEntityKeeper#ent2str(Object) L2TagInfo.KEEPER.ent2str()}.
   */
  String L2_HAL_MODCFG_NODEID_TAGINFOS = "l2.TagInfos"; //$NON-NLS-1$

  /**
   * ID of option {@link #OPDEF_HALDRV_HARD_IO_THREAD_PRIORITY}.
   */
  String OPID_HALDRV_HARD_IO_THREAD_PRIORITY = "l2.haldrv.hard_io_thread_priority"; //$NON-NLS-1$

  /**
   * MHAL driver hardware I/O thread priority overrides default
   * {@link IL2ApplicationArguments#OPDEF_HAL_HARDIO_THREAD_DEFAULT_PRIORITY}.
   */
  IDataDef OPDEF_HALDRV_HARD_IO_THREAD_PRIORITY = DataDef.create( OPID_HALDRV_HARD_IO_THREAD_PRIORITY, INTEGER, //
      TSID_NAME, "Hard I/O priority", //
      TSID_DESCRIPTION,
      "Optional hardware I/O thread priority " + '(' + Thread.MIN_PRIORITY + '-' + Thread.MAX_PRIORITY + ')', //
      TSID_MIN_INCLUSIVE, avInt( Thread.MIN_PRIORITY ), //
      TSID_MAX_INCLUSIVE, avInt( Thread.MAX_PRIORITY ) //
  );

}

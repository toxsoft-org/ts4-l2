package org.toxsoft.l2.lib.app;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.l2.lib.IL2HardConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Constants to work with {@link L2Application}.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IL2ApplicationConstants {

  String L2APP_ARGS_CTX_OPID_PREFIX  = L2_ID + ".opid";   //$NON-NLS-1$
  String L2APP_ARGS_CTX_REFID_PREFIX = L2_ID + ".ctxref"; //$NON-NLS-1$

  String OPID_L2_COMP_CFG_DIR_ROOT = L2APP_ARGS_CTX_OPID_PREFIX + ".cfgRootDir";       //$NON-NLS-1$
  String REFID_UNIT_LOGGER         = L2APP_ARGS_CTX_REFID_PREFIX + ".logger";          //$NON-NLS-1$
  String REFID_MAIN_THREAD_GUARD   = L2APP_ARGS_CTX_REFID_PREFIX + ".mainThreadGuard"; //$NON-NLS-1$
  String REFID_SK_CONNECTION       = L2APP_ARGS_CTX_REFID_PREFIX + ".skConn";          //$NON-NLS-1$

  String DEFAULT_L2_COMP_CFG_DIR_ROOT = "l2-cfg"; //$NON-NLS-1$

  IDataDef OPDEF_L2_COMP_CFG_DIR_ROOT = DataDef.create( OPID_L2_COMP_CFG_DIR_ROOT, STRING, //
      TSID_NAME, "Config root", //
      TSID_DESCRIPTION,
      "L2 compenents configuration files root directory relative to the application working directory", //
      TSID_DEFAULT_VALUE, avStr( DEFAULT_L2_COMP_CFG_DIR_ROOT ) //
  );

  ITsContextRefDef<ITsThreadExecutor> REFDEF_MAIN_THREAD_GUARD = TsContextRefDef.create( //
      REFID_MAIN_THREAD_GUARD, ITsThreadExecutor.class );

  ITsContextRefDef<ISkConnection> REFDEF_SK_CONNECTION = TsContextRefDef.create( //
      REFID_SK_CONNECTION, ISkConnection.class );

  IStridablesList<IDataDef> ALL_L2_INIT_PARAMS = new StridablesList<>( //
  );

}

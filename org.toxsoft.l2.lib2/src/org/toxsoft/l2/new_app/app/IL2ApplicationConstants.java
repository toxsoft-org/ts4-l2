package org.toxsoft.l2.new_app.app;

import static org.toxsoft.l2.lib2.IL2HardConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Constants to work with {@link L2Application}.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IL2ApplicationConstants {

  String L2APP_ARGS_CTX_REF_ID_PREFIX = L2_ID + ".ctxref."; //$NON-NLS-1$

  String REFID_UNIT_LOGGER   = L2APP_ARGS_CTX_REF_ID_PREFIX + ".logger"; //$NON-NLS-1$
  String REFID_SK_CONNECTION = L2APP_ARGS_CTX_REF_ID_PREFIX + ".skConn"; //$NON-NLS-1$

  ITsContextRefDef<ILogger> REFDEF_UNIT_LOGGER = TsContextRefDef.create( //
      REFID_UNIT_LOGGER, ILogger.class );

  ITsContextRefDef<ISkConnection> REFDEF_SK_CONNECTION = TsContextRefDef.create( //
      REFID_SK_CONNECTION, ISkConnection.class );

  IStridablesList<IDataDef> ALL_L2_INIT_PARAMS = new StridablesList<>( //
  );

}

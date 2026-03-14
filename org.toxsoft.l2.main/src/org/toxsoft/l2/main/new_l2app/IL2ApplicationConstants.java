package org.toxsoft.l2.main.new_l2app;

import static org.toxsoft.l2.lib.IL2LibConstants.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Constants to work with {@link L2Application}.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IL2ApplicationConstants {

  String L2APP_ARGS_CTX_ID_PREFIX = L2_FULL_ID + ".ctxid."; //$NON-NLS-1$

  String REFID_UNIT_LOGGER   = L2APP_ARGS_CTX_ID_PREFIX + ".logger"; //$NON-NLS-1$
  String REFID_SK_CONNECTION = L2APP_ARGS_CTX_ID_PREFIX + ".skConn"; //$NON-NLS-1$

  ITsContextRefDef<ILogger> REFDEF_UNIT_LOGGER = TsContextRefDef.create( //
      REFID_UNIT_LOGGER, ILogger.class );

  ITsContextRefDef<ISkConnection> REFDEF_SK_CONNECTION = TsContextRefDef.create( //
      REFID_SK_CONNECTION, ISkConnection.class );

}

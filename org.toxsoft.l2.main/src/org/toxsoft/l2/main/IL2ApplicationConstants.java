package org.toxsoft.l2.main;

import static org.toxsoft.l2.lib.IL2LibConstants.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

/**
 * Constants to work with {@link L2Application}.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IL2ApplicationConstants {

  String L2APP_ARGS_CTX_ID_PREFIX = L2_FULL_ID + ".ctxid."; //$NON-NLS-1$

  String REFID_ERROR_LOGGER   = L2APP_ARGS_CTX_ID_PREFIX + ".logger.error";   //$NON-NLS-1$
  String REFID_DEFAULT_LOGGER = L2APP_ARGS_CTX_ID_PREFIX + ".logger.default"; //$NON-NLS-1$

  ITsContextRefDef<ILogger> REFDEF_ERROR_LOGGER   = TsContextRefDef.create( REFID_ERROR_LOGGER, ILogger.class );
  ITsContextRefDef<ILogger> REFDEF_DEFAULT_LOGGER = TsContextRefDef.create( REFID_DEFAULT_LOGGER, ILogger.class );

}

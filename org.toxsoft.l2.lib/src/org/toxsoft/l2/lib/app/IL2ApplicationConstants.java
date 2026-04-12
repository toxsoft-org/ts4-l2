package org.toxsoft.l2.lib.app;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Constants to work with {@link L2Application}.
 *
 * @author hazard157
 */
public interface IL2ApplicationConstants {

  /**
   * Initialization reference: thread guard of type {@link ITsThreadExecutor}.
   */
  ITsContextRefDef<ITsThreadExecutor> REFDEF_MAIN_THREAD_GUARD = TsContextRefDef.create( //
      "l2.app.ctxref.MainThreadGuard", //$NON-NLS-1$
      ITsThreadExecutor.class );

  /**
   * Initialization reference: connection to the USkat server of type {@link ISkConnection}.
   */
  ITsContextRefDef<ISkConnection> REFDEF_SK_CONNECTION = TsContextRefDef.create( //
      "l2.app.ctxref.SkCOnnection", //$NON-NLS-1$
      ISkConnection.class );

}

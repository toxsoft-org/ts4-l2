package org.toxsoft.l2.main.old_refactor.helpers;

import static org.toxsoft.l2.main.old_refactor.helpers.IL2MainSharedResources.*;

import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.main.impl.*;
import org.toxsoft.l2.main.old_refactor.*;

/**
 * Класс потока завершения приложения (CTRL+C)
 *
 * @author max
 */
public class ShutdownHookThread
    extends Thread {

  private final ILogger logger;

  public ShutdownHookThread( ILogger aLogger ) {
    logger = aLogger;
  }

  @Override
  public void run() {
    logger.error( "ShutdownHookThread Started" ); //$NON-NLS-1$
    L2CoreMain.setShutdownHookQuitCmd( new ProgramQuitCommand( 0, MSG_SHUTDOWN_BY_CTRL_C ) );

    synchronized (L2CoreMain.class) {
      while( !L2CoreMain.isShutedDown() ) {
        try {
          L2CoreMain.class.wait();
        }
        catch( InterruptedException e ) {
          e.printStackTrace();
        }
      }
    }

    logger.error( "ShutdownHookThread Quit" ); //$NON-NLS-1$
  }

}

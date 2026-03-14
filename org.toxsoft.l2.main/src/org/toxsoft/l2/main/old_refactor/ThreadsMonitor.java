package org.toxsoft.l2.main.old_refactor;

import java.lang.management.*;

import org.toxsoft.core.tslib.utils.logs.*;

public class ThreadsMonitor
    implements Runnable {

  private final ILogger logger;

  public ThreadsMonitor( ILogger aLogger ) {
    logger = aLogger;
  }

  @Override
  public void run() {
    logger.info( "L2 Threads Monitor Thread Started" );
    long count = 0;
    boolean first = true;
    while( true ) {
      count++;
      // проверка раз в секунду
      try {
        Thread.sleep( 1000L );
      }
      catch( InterruptedException e ) {
        logger.error( e );
      }

      ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

      long[] deadlockedThreads = mxBean.findDeadlockedThreads();
      long[] monitorDeadlockedThreads = mxBean.findMonitorDeadlockedThreads();

      // раз в 10 мин или если таки случилась блокировка (первый раз)
      if( count % 600 == 0 || ((deadlockedThreads != null || monitorDeadlockedThreads != null)) && first ) {
        // на тот случай если блокировка пропдёт (такое возможно вообще?)
        first = deadlockedThreads == null && monitorDeadlockedThreads == null;

        logger.info( "<-------------" );

        logger.info( "deadlockedThreads = %d", (deadlockedThreads == null ? 0 : deadlockedThreads.length) );
        logger.info( "monitorDeadlockedThreads = %d",
            (monitorDeadlockedThreads == null ? 0 : monitorDeadlockedThreads.length) );

        if( deadlockedThreads != null ) {
          for( long tId : deadlockedThreads ) {
            ThreadInfo threadInfo = mxBean.getThreadInfo( tId );
            logger.info( "deadlocked - %s - %s - %s", threadInfo.getThreadName(), threadInfo.getLockOwnerName(),
                threadInfo.getLockInfo().toString() );
          }
        }

        if( monitorDeadlockedThreads != null ) {
          for( long tId : monitorDeadlockedThreads ) {
            ThreadInfo threadInfo = mxBean.getThreadInfo( tId );
            logger.info( "monitorDeadlocked - %s - %s - %s", threadInfo.getThreadName(), threadInfo.getLockOwnerName(),
                threadInfo.getLockInfo().toString() );
          }
        }

        logger.info( "------------->" );

      }

    }

  }

}

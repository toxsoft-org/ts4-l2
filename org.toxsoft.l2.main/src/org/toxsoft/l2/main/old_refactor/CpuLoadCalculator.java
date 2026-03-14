package org.toxsoft.l2.main.old_refactor;

import java.lang.management.*;

import org.toxsoft.core.tslib.utils.logs.*;

/**
 * Класс счётчика загруженности. Реализация с суммированием всех потоков программы (а не системы целиком)
 *
 * @author max
 */
public class CpuLoadCalculator
    implements Runnable {

  /**
   * Период проверки загруженности системы программой НУ.
   */
  private static final long CHECK_CPU_LOAD_PERIOD = 60000L;

  /**
   * Уровень загруженности системы программой НУ при котором следует выводить отладочную информацию по потокам.
   */
  private static final double CPU_LOAD__DEBUG_INFO_LEVEL = 0.6d;

  private static final long calcPeriod = 5000L;

  private double cpuLoad = 0;

  long prevSystemTime;
  long prevThreadsTime;
  int  cpuCount;

  private final ILogger logger;

  public CpuLoadCalculator( ILogger aLogger ) {
    logger = aLogger;
  }

  void init() {
    try {
      prevSystemTime = System.nanoTime();
      prevThreadsTime = 0;
      cpuCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
    }
    catch( Exception e ) {
      logger.error( e );
      return;
    }

    Thread t = new Thread( this );
    t.setName( "L2 Thread CpuLoadCalculator" ); //$NON-NLS-1$
    t.setDaemon( true );
    t.start();
    logger.info( "CPU Load Calculator started" ); //$NON-NLS-1$
  }

  public synchronized double getCpuLoad() {
    return cpuLoad;
  }

  synchronized void setCpuLoad( double aCpuLoad ) {
    cpuLoad = aCpuLoad;
  }

  @Override
  public void run() {
    while( true ) {
      try {
        Thread.sleep( calcPeriod );
        calcCpuLoad();
      }
      catch( Exception e ) {
        logger.error( e );
        break;
      }
    }

  }

  private void calcCpuLoad() {

    ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
    long[] threadIds = mxBean.getAllThreadIds();

    long total = 0;
    for( long id : threadIds ) {
      total += mxBean.getThreadCpuTime( id );
    }

    long currSystemTime = System.nanoTime();

    double cpuLoadVal = ((double)(total - prevThreadsTime) / ((double)(cpuCount * (currSystemTime - prevSystemTime))));

    setCpuLoad( cpuLoadVal );

    prevThreadsTime = total;
    prevSystemTime = currSystemTime;

  }

  // ------------------------------------------------------------------------------------------------------------
  // методы и классы диагностики потоков
  //

  private static long prevCheckThreadsTime = 0;

  void checkThreads() {
    if( System.currentTimeMillis() - prevCheckThreadsTime > CHECK_CPU_LOAD_PERIOD ) {
      double cpuLoad = getCpuLoad();
      if( cpuLoad > CPU_LOAD__DEBUG_INFO_LEVEL ) {
        L2AppUtils.threadInfoToLogs();
      }
      prevCheckThreadsTime = System.currentTimeMillis();
    }

  }

}

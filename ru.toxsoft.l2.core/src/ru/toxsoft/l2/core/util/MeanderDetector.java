package ru.toxsoft.l2.core.util;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.utils.logs.*;

/**
 * Определитель меандра
 *
 * @author Max
 */
public class MeanderDetector {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( getClass().getName() );

  /**
   * Минимальное количество точек на период (если период не выдерживается)
   */
  private int MIN_COUNT_FOR_CHECK;// = 6;

  private ValuesMemory vMemory;

  private Trigger trigger;

  private Checker checker;

  private int prevValue = -1;

  private boolean withLater = false;

  public MeanderDetector( long aPeriod, int aMinCountForPeriod ) {
    this( true, aPeriod, aMinCountForPeriod );

  }

  public MeanderDetector( boolean aWithLater, long aPeriod, int aMinCountForPeriod ) {
    withLater = aWithLater;
    long MIN_PERIOD_FOR_CHECK = aPeriod;
    MIN_COUNT_FOR_CHECK = aMinCountForPeriod;
    vMemory = new ValuesMemory( MIN_COUNT_FOR_CHECK * 2 );
    checker = new Checker( MIN_PERIOD_FOR_CHECK, MIN_COUNT_FOR_CHECK );
    trigger = new Trigger();
  }

  public int getValue( long aTime, boolean aValue ) {
    int result = -1;
    vMemory.put( aValue );
    if( vMemory.isValid() ) {

      result = checker.isCheck( aTime, vMemory );
      if( result < 0 ) {
        result = prevValue;
      }

      if( trigger.isTriggered( vMemory ) || prevValue < 0 ) {
        checker.startCheck( aTime );
      }
    }

    prevValue = result;
    return result;
  }

  static class Trigger {

    boolean isTriggered( ValuesMemory aValuesMemory ) {
      return aValuesMemory.get( 0 ) != aValuesMemory.get( 1 );
    }
  }

  class Checker {

    private long minPeriodForCheck = 500L;

    private int minCountForCheck = 6;

    long startTime = 0;

    int checkCount = 1000;

    boolean isRun = false;

    boolean isLater = false;

    public Checker( long aMIN_PERIOD_FOR_CHECK, int aMIN_COUNT_FOR_CHECK ) {
      super();
      minPeriodForCheck = aMIN_PERIOD_FOR_CHECK;
      minCountForCheck = aMIN_COUNT_FOR_CHECK;

    }

    void startCheck( long aStartTime ) {
      if( aStartTime - startTime > minPeriodForCheck && checkCount > minCountForCheck ) {
        startTime = aStartTime;
        checkCount = 0;
        isRun = true;
      }
      else {
        isLater = true;
      }

    }

    int isCheck( long aTime, ValuesMemory aValuesMemory ) {
      if( isRun ) {
        checkCount++;
        if( aTime - startTime > minPeriodForCheck && checkCount > minCountForCheck ) {
          isRun = false;
          // проверить сумму
          double middle = aValuesMemory.calcMiddle( checkCount );

          if( isLater ) {
            isLater = false;
            if( withLater ) {
              startTime = aTime;
              checkCount = 0;
              isRun = true;
            }
          }

          if( middle > 0.33 && middle < 0.67 ) {
            return 2;
          }

          if( middle <= 0.33 ) {
            return 0;
          }
          return 1;
        }
      }
      return -1;
    }
  }

  static class ValuesMemory {

    private boolean buffer[];
    private int     currIndex = -1;
    boolean         isValid   = false;

    ValuesMemory( int aSize ) {
      buffer = new boolean[aSize];
    }

    boolean isValid() {
      return isValid;
    }

    boolean get( int aIndex ) {
      int index = currIndex - aIndex;
      if( index < 0 ) {
        index = buffer.length + index;
      }
      return buffer[index];
    }

    void put( boolean aValue ) {
      currIndex++;
      if( currIndex == buffer.length ) {
        currIndex = 0;
        // достаточно одного раза
        isValid = true;
      }
      buffer[currIndex] = aValue;
    }

    int size() {
      return buffer.length;
    }

    double calcMiddle( int aCount ) {
      double result = 0;
      for( int i = 0; i < aCount; i++ ) {
        result += (get( i ) ? 1.0 : 0.0);
      }
      return result / aCount;
    }
  }

  static class TestMeanderGenerator {

    static long MIN_HALF_PERIOD = 1500L;

    static int MIN_COUNT_FOR_HALF_PERIOD = 3;

    long halfPeriodStart = 0;

    boolean prevVal = true;
    int     count   = 0;

    boolean getValue( long aTime ) {
      count++;
      if( aTime - halfPeriodStart > MIN_HALF_PERIOD && count > MIN_COUNT_FOR_HALF_PERIOD ) {
        prevVal = !prevVal;
        count = 1;
        halfPeriodStart = aTime;
      }

      return prevVal;
    }
  }

  public static void main( String[] arg ) {
    long period = 500L;
    TestMeanderGenerator generator = new TestMeanderGenerator();
    MeanderDetector detectorWrong = new MeanderDetector( false, TestMeanderGenerator.MIN_HALF_PERIOD * 2,
        TestMeanderGenerator.MIN_COUNT_FOR_HALF_PERIOD * 2 );
    MeanderDetector detectorRight = new MeanderDetector( TestMeanderGenerator.MIN_HALF_PERIOD * 2,
        TestMeanderGenerator.MIN_COUNT_FOR_HALF_PERIOD * 2 );

    int vT = 0;
    int prevVt = 0;
    long startT = System.currentTimeMillis();
    while( true ) {
      long t = System.currentTimeMillis();
      if( t - startT > 9300 ) {
        vT = (int)(3.0 * Math.random());
        if( vT == prevVt ) {
          vT = (int)(3.0 * Math.random());
        }
        if( vT != prevVt ) {
          System.out.println( "---------------" );
        }
        prevVt = vT;
        startT = t;
      }
      boolean v = false;
      if( vT == 2 ) {
        v = generator.getValue( t );
      }
      if( vT == 1 ) {
        v = true;
      }
      System.out.println( v + "   |  " + detectorRight.getValue( t, v ) + "   |  " + detectorWrong.getValue( t, v ) );
      try {
        Thread.sleep( period );
      }
      catch( InterruptedException ex ) {
        ex.printStackTrace();
      }
    }

  }
}

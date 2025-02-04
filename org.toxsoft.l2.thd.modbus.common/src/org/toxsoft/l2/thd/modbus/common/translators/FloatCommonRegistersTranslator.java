package org.toxsoft.l2.thd.modbus.common.translators;

import java.util.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Common translator of registers to float value
 *
 * @author max
 */
public class FloatCommonRegistersTranslator
    implements IAnalogTranslator {

  private boolean isCDAB = false;

  /**
   * Constructor.
   */
  public FloatCommonRegistersTranslator() {
    // nop
  }

  /**
   * Constructor.
   *
   * @param aByteOrder - byte order string
   */
  public FloatCommonRegistersTranslator( String aByteOrder ) {
    isCDAB = (aByteOrder.compareTo( "CDAB" ) == 0); //$NON-NLS-1$
  }

  @Override
  public IAtomicValue translate( int[] aWords ) {
    if( isCDAB ) {
      return translateCDAB( aWords );
    }
    return translateABCD( aWords );
  }

  private static IAtomicValue translateABCD( int[] aWords ) {
    if( aWords.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += aWords[i] << (16 * (aWords.length - 1 - i));
      }
      return AvUtils.avFloat( Float.intBitsToFloat( value ) );
    }

    if( aWords.length == 4 ) {
      long value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += ((long)aWords[i]) << (16 * (aWords.length - 1 - i));
      }
      return AvUtils.avFloat( Double.longBitsToDouble( value ) );
    }
    return AvUtils.avFloat( aWords[0] );
  }

  private static IAtomicValue translateCDAB( int[] aWords ) {
    if( aWords.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += aWords[i] << (16 * i);
      }
      return AvUtils.avFloat( Float.intBitsToFloat( value ) );
    }

    if( aWords.length == 4 ) {
      long value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += ((long)aWords[i]) << (16 * i);
      }
      return AvUtils.avFloat( Double.longBitsToDouble( value ) );
    }
    return AvUtils.avFloat( aWords[0] );
  }

  /**
   * @param aArgs program args
   */
  @SuppressWarnings( "nls" )
  public static void main( String[] aArgs ) {
    long lVal = Double.doubleToLongBits( 0.01d );
    String lValStr = Long.toBinaryString( lVal );
    System.out.println( lValStr );
    System.out.println( lValStr.length() );
    System.out.println( Integer.valueOf( "110", 2 ) );

    List<String> subs = new ArrayList<>();
    int startSub = lValStr.length() - 16;
    do {
      subs.add( lValStr.substring( Math.max( 0, startSub ), startSub + 16 ) );
      startSub -= 16;
    } while( startSub > -16 );

    List<Integer> vals = new ArrayList<>();
    for( String sub : subs ) {
      System.out.println( sub );
      vals.add( Integer.valueOf( sub, 2 ) );
    }

    long value = 0;
    for( int i = 0; i < vals.size(); i++ ) {
      value += ((long)vals.get( i )) << (16 * i);
    }
    System.out.println( Double.longBitsToDouble( value ) );
  }
}

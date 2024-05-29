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

  @Override
  public IAtomicValue translate( int[] aBytes ) {
    if( aBytes.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aBytes.length; i++ ) {
        value += aBytes[i] << (16 * i);
      }
      return AvUtils.avFloat( Float.intBitsToFloat( value ) );
    }

    if( aBytes.length == 4 ) {
      long value = 0;
      for( int i = 0; i < aBytes.length; i++ ) {
        value += ((long)aBytes[i]) << (16 * i);
      }

      return AvUtils.avFloat( Double.longBitsToDouble( value ) );
    }
    return AvUtils.avFloat( aBytes[0] );
  }

  public static void main( String[] a ) {
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

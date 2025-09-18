package org.toxsoft.l2.thd.modbus.common.translators;

import java.util.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

import gnu.io.*;

/**
 * Translator of registers to integer value for Vordaq <br>
 * just simple ABCD bytes order conversion <br>
 *
 * @deprecated use IntegerCommonRegistersTranslator with argument ABCD
 * @author dima
 */
@Deprecated
public class VordaqIntegerRegistersTranslator
    implements IAnalogTranslator {

  @Override
  public IAtomicValue translate( int[] aWords ) {
    // dima 15.09.25
    // this code fragment is strict implementation recommendations from manual
    // https://wiki.teltonika-networks.com/view/Monitoring_via_Modbus, but it doesn't work
    // StringBuilder strValue = new StringBuilder();
    // if( aWords.length == 2 ) {
    // // first step convert to binary string
    // for( int i = 0; i < aWords.length; i++ ) {
    // String regBinaryString = Integer.toBinaryString( aWords[i] );
    // strValue.append( String.format( "%16s", regBinaryString ).replace( ' ', '0' ) );
    // }
    // // Convert string to integer value on radix 2
    // int value = Integer.parseInt( strValue.toString(), 2 );
    // return AvUtils.avInt( value );
    // }
    // just simple ABCD bytes order conversion
    if( aWords.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value = value << 16;
        value += aWords[i];
      }
      return AvUtils.avInt( value );
    }
    return AvUtils.avInt( aWords[0] );
  }

  public static void main( String[] args ) {
    int[] words = new int[2];
    words[0] = 0b1111111111111111;
    words[1] = 0b1111111111000101;
    // words[0] = 1;
    // words[1] = 3067;
    int value = 0;
    for( int i = 0; i < words.length; i++ ) {
      value = value << 16;
      value += words[i];
    }
    System.out.print( value );
  }

}

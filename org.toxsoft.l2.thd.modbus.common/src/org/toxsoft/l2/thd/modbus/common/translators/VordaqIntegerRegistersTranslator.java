package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Translator of registers to integer value for Vordaq <br>
 * see link https://wiki.teltonika-networks.com/view/Monitoring_via_Modbus
 *
 * @author dima
 */
public class VordaqIntegerRegistersTranslator
    implements IAnalogTranslator {

  @Override
  public IAtomicValue translate( int[] aWords ) {
    StringBuilder strValue = new StringBuilder();
    if( aWords.length == 2 ) {
      // first step convert to binary string
      for( int i = 0; i < aWords.length; i++ ) {
        String regBinaryString = Integer.toBinaryString( aWords[i] );
        strValue.append( String.format( "%16s", regBinaryString ).replace( ' ', '0' ) );
      }
      // Convert string to integer value on radix 2
      int value = Integer.parseInt( strValue.toString(), 2 );
      return AvUtils.avInt( value );
    }
    return AvUtils.avInt( aWords[0] );
  }

}

package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Translator of registers to unsigned integer value for Vordaq <br>
 * just simple ABCD bytes order conversion
 *
 * @author dima
 */
public class UnsignedIntegerRegistersTranslator
    implements IAnalogTranslator {

  boolean isUnsigned = false;

  @Override
  public IAtomicValue translate( int[] aWords ) {
    // just simple ABCD bytes order conversion
    if( aWords.length == 2 ) {
      int fourBytesJustRead = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        fourBytesJustRead = fourBytesJustRead << 16;
        fourBytesJustRead += aWords[i];
      }
      long unsignedValue = fourBytesJustRead & 0xffffffffL;
      return AvUtils.avInt( unsignedValue );
    }
    return AvUtils.avInt( aWords[0] );
  }

}

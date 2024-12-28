package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Common translator of registers to integer value
 *
 * @author max
 */
public class IntegerCommonRegistersTranslator
    implements IAnalogTranslator {

  @Override
  // TODO add ABCD bytes order conversion
  public IAtomicValue translate( int[] aWords ) {
    if( aWords.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += aWords[i] << (16 * i);
      }
      return AvUtils.avInt( value );
    }

    return AvUtils.avInt( aWords[0] );
  }

}

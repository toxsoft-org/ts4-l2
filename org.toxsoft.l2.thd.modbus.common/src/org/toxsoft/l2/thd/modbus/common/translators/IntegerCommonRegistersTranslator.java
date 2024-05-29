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
  public IAtomicValue translate( int[] aBytes ) {
    if( aBytes.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aBytes.length; i++ ) {
        value += aBytes[i] << (16 * i);
      }
      return AvUtils.avInt( value );
    }

    return AvUtils.avInt( aBytes[0] );
  }

}

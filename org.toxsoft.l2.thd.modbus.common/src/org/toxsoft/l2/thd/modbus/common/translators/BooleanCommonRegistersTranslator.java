package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Common translator of registers to boolean value
 *
 * @author max
 */
public class BooleanCommonRegistersTranslator
    implements IAnalogTranslator {

  int bitNumber;

  /**
   * Конструктор по номеру бита
   *
   * @param aBitNumber - номер бита в строковой форме.
   */
  public BooleanCommonRegistersTranslator( String aBitNumber ) {
    bitNumber = Integer.parseInt( aBitNumber );
    TsIllegalArgumentRtException.checkFalse( 0 <= bitNumber && bitNumber < 16,
        "Bit number must be >=0 && <16, but current value = %s", aBitNumber );
  }

  @Override
  public IAtomicValue translate( int[] aBytes ) {
    return AvUtils.avBool( ((aBytes[0] >> bitNumber) & 1) == 1 );
  }

  public static void main( String[] a ) {
    int d = 10;
    for( int i = 0; i < 16; i++ ) {
      System.out.println( (d & (1 << i)) == (1 << i) );
    }
  }
}

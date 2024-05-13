package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Транслятор аналогового сигнала из двух байт в float
 *
 * @author max
 */
public class AnalogTowBytesTranslator
    implements IAnalogTranslator {

  @Override
  public IAtomicValue translate( int[] aBytes ) {
    int result = aBytes[0];
    // (((aBytes[0] & 0xff) << 8) | (aBytes[1] & 0xff));
    // System.out.println("Analog result = "+result);
    return AvUtils.avInt( result );
  }

}

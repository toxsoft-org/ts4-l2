package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Транслятор аналогового сигнала из бладшего??? байта входного интового значения
 *
 * @author Max
 */
public class AnalogLowByteTranslator
    implements IAnalogTranslator {

  @Override
  public IAtomicValue translate( int[] aBytes ) {
    int low = (0xff & (aBytes[0] >> 8));

    // System.out.println("Low byte = "+low);
    return AvUtils.avFloat( low );
  }
}

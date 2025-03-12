package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;

/**
 * Translator of registers to float value and recalculate Mega Pascal 2 kg/cm2
 *
 * @author dima
 */
public class MPas2Kg_Cm2FloatRegistersTranslator
    extends FloatCommonRegistersTranslator {

  private final float coeff = 10.197162f;

  /**
   * Constructor.
   */
  public MPas2Kg_Cm2FloatRegistersTranslator() {
    super( FloatCommonRegistersTranslator.byteOrderCDAB );
  }

  @Override
  public IAtomicValue translate( int[] aWords ) {
    IAtomicValue rawValue = super.translate( aWords );
    return AvUtils.avFloat( rawValue.asFloat() * coeff );
  }

}

package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;

/**
 * Translator of registers to float value and recalculate from second to hour
 *
 * @author dima
 */
public class Sec2HourFloatRegistersTranslator
    extends FloatCommonRegistersTranslator {

  /**
   * Constructor.
   */
  public Sec2HourFloatRegistersTranslator() {
  }

  @Override
  public IAtomicValue translate( int[] aWords ) {
    IAtomicValue rawValue = super.translate( aWords );
    return AvUtils.avFloat( rawValue.asFloat() * 3600 );
  }

}

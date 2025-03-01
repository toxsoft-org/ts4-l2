package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

/**
 * Translator of registers to float value and then calculate bit value
 *
 * @author dima
 */
public class Float2BitRegistersTranslator
    extends FloatCommonRegistersTranslator {

  private int bitPosition = 0;

  /**
   * Constructor.
   *
   * @param aBitPosition - position of bit to mask
   */
  public Float2BitRegistersTranslator( String aBitPosition ) {
    try {
      bitPosition = Integer.parseInt( aBitPosition );
    }
    catch( NumberFormatException ex ) {
      LoggerUtils.errorLogger().error( ex );
      bitPosition = -1;
    }
  }

  @Override
  public IAtomicValue translate( int[] aWords ) {
    IAtomicValue rawValue = super.translate( aWords );
    int intRepresentation = Math.round( rawValue.asFloat() );
    boolean bitValue = extractValueAtPosition( intRepresentation, bitPosition );
    return AvUtils.avBool( bitValue );
  }

  private boolean extractValueAtPosition( int aIntRepresentation, int aPosition ) {
    if( bitPosition < 0 ) {
      return false;
    }
    return ((aIntRepresentation) & (1 << (aPosition - 1))) != 0;
  }
}

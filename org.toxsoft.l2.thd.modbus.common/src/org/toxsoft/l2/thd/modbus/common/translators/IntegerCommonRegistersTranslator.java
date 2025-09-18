package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Common translator of registers to integer value
 *
 * @author max
 * @author dima // added ABCD part
 */
public class IntegerCommonRegistersTranslator
    implements IAnalogTranslator {

  private boolean      isABCD        = false;
  /**
   * Byte order CDAB
   */
  public static String byteOrderABCD = "ABCD"; //$NON-NLS-1$

  /**
   * Constructor.
   */
  public IntegerCommonRegistersTranslator() {
    // nop
  }

  /**
   * Constructor.
   *
   * @param aByteOrder - byte order string
   */
  public IntegerCommonRegistersTranslator( String aByteOrder ) {
    isABCD = (aByteOrder.compareTo( byteOrderABCD ) == 0);
  }

  @Override
  public IAtomicValue translate( int[] aWords ) {
    if( isABCD ) {
      return translateABCD( aWords );
    }
    return translateCDAB( aWords );
  }

  private static IAtomicValue translateABCD( int[] aWords ) {
    if( aWords.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value = value << 16;
        value += aWords[i];
      }
      return AvUtils.avInt( value );
    }
    return AvUtils.avInt( aWords[0] );
  }

  private static IAtomicValue translateCDAB( int[] aWords ) {
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

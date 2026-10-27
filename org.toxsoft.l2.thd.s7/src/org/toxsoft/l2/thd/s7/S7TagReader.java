/**
 *
 */
package org.toxsoft.l2.thd.s7;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import Moka7.*;

/**
 * Read one tag value from data block and conver it to IAtomicValue
 *
 * @author dima
 */
public class S7TagReader
    extends S7TagBase {

  /**
   * Constructor.
   */
  S7TagReader( String aTagId, int aDbNumber, EAtomicType aTagType, int aBufferOffset ) {
    super( aTagId, aDbNumber, aTagType, aBufferOffset );
  }

  S7TagReader( String aTagId, int aDbNumber, int aBufferOffset, int aBitNumber ) {
    super( aTagId, aDbNumber, aBufferOffset, aBitNumber );
  }

  @SuppressWarnings( { "nls", "boxing" } )
  IAtomicValue readTagValue( byte[] aDataBuffer ) {
    tagValue = IAtomicValue.NULL;
    switch( tagType ) {
      case BOOLEAN:
        boolean boolVal = S7.GetBitAt( aDataBuffer, bufferOffset, bitNumber );
        tagValue = AvUtils.avBool( boolVal );
        LoggerUtils.debug( "read from DB%d.DBX%d.%d boolVal = %b \n", dbNumber, bufferOffset, bitNumber, boolVal );
        break;
      case FLOATING:
        float fVal = S7.GetFloatAt( aDataBuffer, bufferOffset );
        tagValue = AvUtils.avFloat( fVal );
        LoggerUtils.debug( "read from DB%d.DBD%d  fVal = %.2f \n", dbNumber, bufferOffset, fVal );
        break;
      case INTEGER:
        int iVal = S7.GetWordAt( aDataBuffer, bufferOffset );
        tagValue = AvUtils.avInt( iVal );
        LoggerUtils.debug( "read from DB%d.DBW%d  iVal = %d \n", dbNumber, bufferOffset, iVal );
        break;
      case NONE:
        break;
      case STRING:
        break;
      case TIMESTAMP:
        break;
      case VALOBJ:
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    return tagValue;
  }
}

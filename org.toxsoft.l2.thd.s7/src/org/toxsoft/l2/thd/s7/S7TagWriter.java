/**
 *
 */
package org.toxsoft.l2.thd.s7;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.thd.opc.*;

import Moka7.*;

/**
 * Write one tag value from USkat {@link ITag} to S7 data block
 *
 * @author dima
 */
public class S7TagWriter
    extends S7TagBase {

  /**
   * Constructor.
   */
  S7TagWriter( String aTagId, int aDbNumber, EAtomicType aTagType, int aBufferOffset ) {
    super( aTagId, aDbNumber, aTagType, aBufferOffset );
  }

  S7TagWriter( String aTagId, int aDbNumber, int aBufferOffset, int aBitNumber ) {
    super( aTagId, aDbNumber, aBufferOffset, aBitNumber );
  }

  @SuppressWarnings( { "nls", "boxing" } )
  void writeValue2Buffer( byte[] aDataBuffer, IAtomicValue aValue ) {
    switch( tagType ) {
      case BOOLEAN:
        boolean boolVal = aValue.asBool();
        S7.SetBitAt( aDataBuffer, bufferOffset, bitNumber, boolVal );
        LoggerUtils.debug( "write to DB5%d.DBX%d.%d   boolVal = %b \n", dbNumber, bufferOffset, bitNumber, boolVal );
        break;
      case FLOATING:
        float fVal = aValue.asFloat();
        S7.SetFloatAt( aDataBuffer, bufferOffset, fVal );
        LoggerUtils.debug( "write to DB%d.DBD%d  fVal = %.2f\n", dbNumber, bufferOffset, fVal );
        break;
      case INTEGER:
        int iVal = aValue.asInt();
        S7.SetWordAt( aDataBuffer, bufferOffset, iVal );
        LoggerUtils.debug( "write to DB%d.DBW%d  iVal = %d \n", dbNumber, bufferOffset, iVal );
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
  }

}

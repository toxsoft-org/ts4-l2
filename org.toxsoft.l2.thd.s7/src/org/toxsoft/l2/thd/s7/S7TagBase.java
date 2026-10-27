/**
 *
 */
package org.toxsoft.l2.thd.s7;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Read one tag value from data block and conver it to IAtomicValue
 *
 * @author dima
 */
public class S7TagBase {

  protected final int dbNumber;

  private final String tagId;

  protected EAtomicType tagType = EAtomicType.BOOLEAN;

  protected final int bufferOffset;

  protected int bitNumber = -1;

  protected IAtomicValue tagValue = IAtomicValue.NULL;

  /**
   * Constructor.
   */
  S7TagBase( String aTagId, int aDbNumber, EAtomicType aTagType, int aBufferOffset ) {
    tagId = aTagId;
    dbNumber = aDbNumber;
    tagType = aTagType;
    bufferOffset = aBufferOffset;
  }

  S7TagBase( String aTagId, int aDbNumber, int aBufferOffset, int aBitNumber ) {
    tagId = aTagId;
    dbNumber = aDbNumber;
    bufferOffset = aBufferOffset;
    bitNumber = aBitNumber;
  }

  int tagValBufferSize() {
    int retVal = 2;
    switch( tagType ) {
      case BOOLEAN:
        break;
      case FLOATING:
        retVal = 4;
        break;
      case INTEGER:
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
    return retVal;
  }

  String tagId() {
    return tagId;
  }

}

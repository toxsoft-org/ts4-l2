/**
 *
 */
package org.toxsoft.l2.thd.s7;

import static org.toxsoft.l2.thd.s7.IS7ThdConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.thd.opc.*;

import Moka7.*;

/**
 * Читатель одного блока данных S7. <br>
 * Использует уже созданное снаружи соединение {@link S7Client} для получения одного блока данных с S7 server. В его
 * составе находится список читателей конкретных тегов которые, используя буфер и зная о расположении своего данного в
 * нем, извлекают значения и передают их в тег.
 *
 * @author dima
 */
public class DataBlockReader
    extends DataBlockBase {

  /**
   * Реализация тега на чтение предоставляемого в виде интерфейса службе DLM.
   *
   * @author dima
   */
  static class ReadTagImpl
      implements ITag {

    private IAtomicValue value = IAtomicValue.NULL;

    private String id;

    public ReadTagImpl( String aId ) {
      this.id = aId;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public String description() {
      return TsLibUtils.EMPTY_STRING;
    }

    @Override
    public String tagId() {
      return id;
    }

    @Override
    public String name() {
      return TsLibUtils.EMPTY_STRING;
    }

    @Override
    public EKind kind() {
      return EKind.R;
    }

    @Override
    public EAtomicType valueType() {
      return value.atomicType();
    }

    @Override
    public IAtomicValue get() {
      return value;
    }

    @Override
    public void set( IAtomicValue aVal ) {
      // без реализации - снаружи не менять
    }

    @Override
    public String nmName() {
      return id;
    }

    @Override
    public boolean isDirty() {
      return false;
    }
  }

  private IListEdit<S7TagReader> readers = new ElemArrayList<>();

  /**
   * Constructor.
   *
   * @param aS7Server S7 server API
   * @param aBlockNumber number of block in S7
   */
  public DataBlockReader( S7Client aS7Server, int aBlockNumber ) {
    super( aS7Server, aBlockNumber );
  }

  /**
   * Здесь только чтение целиком буфера DB с S7 сервера
   *
   * @return code of error
   */
  public int readDB() {
    return s7Server.ReadArea( S7.S7AreaDB, dbNumber, 0, bufferSize, dataBuffer );
  }

  /**
   * Read description of tag in that block. Create tag reader and tune paramters to read whole data block.
   *
   * @param aTagDefinition - {@link IAvTree} container with metainfo about one tag of that data block
   */
  @Override
  void addTagDefinition( IAvTree aTagDefinition ) {
    // read tag type
    EKind tagKind = EKind.findByName( aTagDefinition.fields().getStr( TAG_KIND_ID ) );
    if( !tagKind.equals( EKind.W ) ) {
      // read tagId
      String tagId = aTagDefinition.fields().getStr( TAG_ID );
      // add suffix ".input"
      tagId = tagId.concat( ".input" ); //$NON-NLS-1$
      // read offset in data block
      int tagBufferOffset = aTagDefinition.fields().getInt( TAG_BUFFER_OFFSET );
      // read tag value type
      EAtomicType tagValueType = EAtomicType.getById( aTagDefinition.fields().getStr( TAG_VALUE_TYPE_ID ) );
      S7TagReader reader = null;
      switch( tagValueType ) {
        case BOOLEAN:
          int bitNumber = aTagDefinition.fields().getInt( BIT_NUMBER );
          reader = new S7TagReader( tagId, dbNumber, tagBufferOffset, bitNumber );
          break;
        case FLOATING:
          reader = new S7TagReader( tagId, dbNumber, tagValueType, tagBufferOffset );
          break;
        case INTEGER:
          reader = new S7TagReader( tagId, dbNumber, tagValueType, tagBufferOffset );
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
      TsIllegalStateRtException.checkNull( reader );
      readers.add( reader );
      dbTags.put( tagId, new ReadTagImpl( tagId ) );
      // tune buffer size
      @SuppressWarnings( "null" )
      int dataBufferRightBorder = tagBufferOffset + reader.tagValBufferSize();
      if( bufferSize < dataBufferRightBorder ) {
        bufferSize = dataBufferRightBorder;
      }
    }
  }

  void fromBuffer2Tags() {
    // update tag values from actual buffer
    for( S7TagReader tagReader : readers ) {
      IAtomicValue tagValue = tagReader.readTagValue( dataBuffer );
      // transfer actual value to DLM level
      ITag tag = dbTags.getByKey( tagReader.tagId() );
      tag.set( tagValue );
    }
  }
}

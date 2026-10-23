/**
 *
 */
package org.toxsoft.l2.thd.s7;

import static org.toxsoft.l2.thd.s7.IS7ThdConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.thd.opc.*;

import Moka7.*;

/**
 * Писатель в один блок данных S7. <br>
 * Использует уже созданное снаружи соединение {@link S7Client} для записи в один блок данных. В его составе находится
 * список писателей конкретных тегов которые, используя буфер и зная о расположении своего данного в нем, извлекают
 * целевое значение из тега USkat {@link ITag } и пишут его в тег S7.
 *
 * @author dima
 */
public class DataBlockWriter
    extends DataBlockBase {

  /**
   * Реализация тега на запись, предоставляемого в виде интерфейса службе DLM.
   *
   * @author dima
   */
  static class WriteTagImpl
      implements ITag {

    private IAtomicValue value = IAtomicValue.NULL;

    private String id;

    boolean dirty = false;

    public WriteTagImpl( String aId ) {
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
      return EKind.W;
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
      if( value != aVal ) {
        value = aVal;
        setDirty( true );
      }
    }

    @Override
    public String nmName() {
      return id;
    }

    @Override
    public boolean isDirty() {
      return dirty;
    }

    public void setDirty( boolean isDirty ) {
      dirty = isDirty;
    }
  }

  private IStringMapEdit<S7TagWriter> writers = new StringMap<>();

  private boolean bufferDirty = false;

  /**
   * Constructor.
   *
   * @param aS7Server S7 server API
   * @param aBlockNumber number of block in S7
   */
  public DataBlockWriter( S7Client aS7Server, int aBlockNumber ) {
    super( aS7Server, aBlockNumber );
  }

  /**
   * Здесь только запись целиком блока на S7 сервер
   */
  public void writeDB() {
    if( bufferDirty ) {
      int errCode = s7Server.WriteArea( S7.S7AreaDB, dbNumber, 0, bufferSize, dataBuffer );
      if( errCode == 0 ) {
        bufferDirty = false;
        // clear all tags dirty marks
        for( ITag tag : dbTags ) {
          WriteTagImpl writeTag = (WriteTagImpl)tag;
          writeTag.setDirty( false );
        }
      }
    }
  }

  /**
   * Read description of tag in that block. Create tag writer and tune paramters to write whole data block.
   *
   * @param aTagDefinition - {@link IAvTree} container with metainfo about one tag of that data block
   */
  @Override
  public void addTagDefinition( IAvTree aTagDefinition ) {
    // read tagId
    String tagId = aTagDefinition.fields().getStr( TAG_ID );
    // read offset in data block
    int tagBufferOffset = aTagDefinition.fields().getInt( TAG_BUFFER_OFFSET );
    // read tag type
    EAtomicType tagType = EAtomicType.getById( aTagDefinition.fields().getStr( TAG_VALUE_TYPE_ID ) );
    S7TagWriter writer = null;
    switch( tagType ) {
      case BOOLEAN:
        int bitNumber = aTagDefinition.fields().getInt( BIT_NUMBER );
        writer = new S7TagWriter( tagId, dbNumber, tagBufferOffset, bitNumber );
        break;
      case FLOATING:
        writer = new S7TagWriter( tagId, dbNumber, tagType, tagBufferOffset );
        break;
      case INTEGER:
        writer = new S7TagWriter( tagId, dbNumber, tagType, tagBufferOffset );
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
    TsIllegalStateRtException.checkNull( writer );
    writers.put( tagId, writer );
    dbTags.put( tagId, new WriteTagImpl( tagId ) );
    // tune buffer size
    @SuppressWarnings( "null" )
    int dataBufferRightBorder = tagBufferOffset + writer.tagValBufferSize();
    if( bufferSize < dataBufferRightBorder ) {
      bufferSize = dataBufferRightBorder;
    }
  }

  void fromTags2OutputBuffer() {
    // update db buffer from actual tag values
    for( ITag tag : dbTags ) {
      WriteTagImpl writeTag = (WriteTagImpl)tag;
      if( writeTag.isDirty() ) {
        S7TagWriter writer = writers.getByKey( writeTag.tagId() );
        writer.writeValue2Buffer( dataBuffer, writeTag.get() );
        bufferDirty = true;
      }
    }
  }

}

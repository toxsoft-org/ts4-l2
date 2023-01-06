package org.toxsoft.l2.thd.opc.ua.milo;

import java.util.*;
import java.util.concurrent.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ge.toxsoft.gwp.opcuabridge.*;

/**
 * Писатель тегов OPC UA
 *
 * @author max
 */
public class NodesWriter {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Opc клиент
   */
  private OpcUaClient client;

  /**
   * Список всех тегов на запись
   */
  private IMapEdit<String, IWriteTag> tags = new ElemMap<>();

  /**
   * Промежуточный буфер, состоящий из спец. тегов, агрегирующих теги записи
   */
  private IListEdit<BufferedUaTag> bufferedTags = new ElemArrayList<>();

  /**
   * Возвращает тег по иду
   *
   * @param aId
   * @return
   */
  public IWriteTag getTag( String aId ) {
    return tags.getByKey( aId );
  }

  List<NodeId>        currWriteIds    = new ArrayList<>();
  List<DataValue>     currWriteValues = new ArrayList<>();
  List<BufferedUaTag> currWriteTags   = new ArrayList<>();

  public void writeValuesToNodes()
      throws InterruptedException,
      ExecutionException {

    for( BufferedUaTag tag : bufferedTags ) {
      if( tag.isChanged() ) {
        currWriteIds.add( tag.getNodeId() );
        currWriteValues.add( new DataValue( tag.getValue(), null, null ) );
        currWriteTags.add( tag );
      }
    }

    if( currWriteIds.size() == 0 ) {
      return;
    }

    CompletableFuture<List<StatusCode>> f = client.writeValues( currWriteIds, currWriteValues );

    currWriteIds.clear();
    currWriteValues.clear();
    currWriteTags.clear();

    List<StatusCode> statusCodes = f.get();

    for( int i = 0; i < statusCodes.size(); i++ ) {
      StatusCode code = statusCodes.get( i );

      if( code.isGood() ) {
        // TODO
        currWriteTags.get( i ).clear();
      }
    }

    // List<NodeId> nodeIds =
    // com.google.common.collect.ImmutableList.of( new NodeId( 2, "HelloWorld/ScalarTypes/Int32" ) );
    //
    // for( int i = 0; i < 10; i++ ) {
    // Variant v = new Variant( i );
    //
    // // don't write status or timestamps
    // DataValue dv = new DataValue( v, null, null );
    //
    // // write asynchronously....
    // CompletableFuture<List<StatusCode>> f =
    // client.writeValues( nodeIds, com.google.common.collect.ImmutableList.of( dv ) );
    //
    // // ...but block for the results so we write in order
    // List<StatusCode> statusCodes = f.get();
    // StatusCode status = statusCodes.get( 0 );
    //
    // if( status.isGood() ) {
    // logger.info( "Wrote '{}' to nodeId={}", v, nodeIds.get( 0 ) );
    // }
    // }
  }

  public void writeValuesToBuffer() {
    for( BufferedUaTag tag : bufferedTags ) {
      tag.setToBuffer();
    }
  }

  /**
   * Класс буфферезированного тега с призаком изменения
   *
   * @author max
   */
  static class BufferedUaTag {

    private WriteOpcUaTag tag;

    private IAtomicValue oldValue = IAtomicValue.NULL;

    private IAtomicValue curValue;

    private NodeId nodeId;

    private boolean changed = false;

    BufferedUaTag( WriteOpcUaTag aTag, NodeId aNodeId, IAtomicValue aCurValue ) {
      super();
      tag = aTag;
      curValue = aCurValue;
      nodeId = aNodeId;
    }

    public void clear() {
      oldValue = curValue;
      changed = false;
    }

    BufferedUaTag( WriteOpcUaTag aTag, NodeId aNodeId ) {
      this( aTag, aNodeId, IAtomicValue.NULL );
    }

    void setToBuffer() {
      curValue = tag.getValue();
      changed = !curValue.equals( oldValue );
    }

    boolean isChanged() {
      return changed;
    }

    Variant getValue() {
      Variant result = OpcUaUtils.convertToOpc( curValue, tag.type() );
      // oldValue = curValue;
      return result;
    }

    NodeId getNodeId() {
      return nodeId;
    }

  }

  public void config( IAvTree aCfgInfo ) {
    // TODO Auto-generated method stub

  }
}

package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import java.util.*;
import java.util.concurrent.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.sdk.client.nodes.*;
import org.eclipse.milo.opcua.stack.core.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.thd.opc.*;

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
  private IMapEdit<String, ITag> tags = new ElemMap<>();

  /**
   * Промежуточный буфер, состоящий из спец. тегов, агрегирующих теги записи
   */
  private IListEdit<BufferedUaTag> bufferedTags = new ElemArrayList<>();

  private List<NodeId>        currWriteIds    = new ArrayList<>();
  private List<DataValue>     currWriteValues = new ArrayList<>();
  private List<BufferedUaTag> currWriteTags   = new ArrayList<>();

  /**
   * @param aClient
   */
  public NodesWriter( OpcUaClient aClient ) {
    client = aClient;
  }

  /**
   * Возвращает тег по иду
   *
   * @param aId
   * @return
   */
  public ITag getTag( String aId ) {
    return tags.getByKey( aId );
  }

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

    if( currWriteIds.size() > 0 ) {

      CompletableFuture<List<StatusCode>> f = client.writeValues( currWriteIds, currWriteValues );

      List<StatusCode> statusCodes = f.get();

      for( int i = 0; i < statusCodes.size(); i++ ) {
        StatusCode code = statusCodes.get( i );

        if( code.isGood() ) {
          // TODO
          currWriteTags.get( i ).clear();
        }
        else {
          logger.error( "tag %s error writing", currWriteTags.get( i ).getNodeId().toParseableString() );
        }
      }
    }

    currWriteIds.clear();
    currWriteValues.clear();
    currWriteTags.clear();
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

    private TagImpl tag;

    private IAtomicValue oldValue = IAtomicValue.NULL;

    private IAtomicValue curValue;

    private NodeId nodeId;

    private boolean changed = false;

    BufferedUaTag( TagImpl aTag, NodeId aNodeId, IAtomicValue aCurValue ) {
      super();
      tag = aTag;
      curValue = aCurValue;
      nodeId = aNodeId;
    }

    BufferedUaTag( TagImpl aTag, NodeId aNodeId ) {
      this( aTag, aNodeId, IAtomicValue.NULL );
    }

    public void clear() {
      oldValue = curValue;
      changed = false;
      tag.setDirty( false );
    }

    void setToBuffer() {
      curValue = tag.newValue;
      changed = !curValue.equals( oldValue );
    }

    boolean isChanged() {
      return changed;
    }

    Variant getValue() {
      Variant result = OpcUaUtils.convertToOpc( curValue, tag.valueType() );
      // oldValue = curValue;
      return result;
    }

    NodeId getNodeId() {
      return nodeId;
    }

  }

  public void config( IAvTree aCfgInfo )
      throws UaException {
    // output

    IAvTree tagsConfig = aCfgInfo.nodes().findByKey( OUTPUT_TAGS_PARAM_NAME );

    IList<TagCfgItem> outputTagsCfgItems = new ElemArrayList<>();

    for( int i = 0; i < outputTagsCfgItems.size(); i++ ) {
      TagCfgItem item = outputTagsCfgItems.get( i );
      NodeId nodeId = new NodeId( item.namespaceId, item.tagId );

      UaVariableNode dNode = client.getAddressSpace().getVariableNode( nodeId );

      TagImpl tag = new TagImpl( dNode.getNodeId().toParseableString(), EKind.W, item.tagType );
      tags.put( tag.id(), tag );

      BufferedUaTag bTag = new BufferedUaTag( tag, nodeId );
      bufferedTags.add( bTag );

    }

  }
}

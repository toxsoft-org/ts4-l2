package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import java.util.*;
import java.util.concurrent.*;

import org.eclipse.milo.opcua.sdk.client.*;
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
  private IMapEdit<String, TagImpl> tags = new ElemMap<>();

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
        logger.debug( "BufferedUaTag %s changed, new val = %s", tag.getNodeId().toParseableString(),
            tag.curValue.asString() );
        currWriteIds.add( tag.getNodeId() );
        currWriteValues.add( new DataValue( tag.getValue(), null, null ) );
        currWriteTags.add( tag );
      }
    }

    if( currWriteIds.size() > 0 ) {

      try {
        CompletableFuture<List<StatusCode>> f = client.writeValues( currWriteIds, currWriteValues );

        List<StatusCode> statusCodes = new ArrayList<>();
        try {
          statusCodes = f.get( 10, TimeUnit.SECONDS );
        }
        catch( Exception ex ) {
          logger.error( "cant get writing status codes", ex );
        }

        for( int i = 0; i < statusCodes.size(); i++ ) {
          StatusCode code = statusCodes.get( i );

          if( code.isGood() ) {
            currWriteTags.get( i ).clear();
            logger.debug( "tag %s cleared", currWriteTags.get( i ).getNodeId().toParseableString() );
          }
          else {
            logger.error( "tag %s error writing", currWriteTags.get( i ).getNodeId().toParseableString() );
          }
        }
        logger.debug( "exit from writeValues method" );

      }
      catch( Exception wEx ) {
        logger.error( "Exception in write on LL method", wEx );
      }
      finally {
        currWriteIds.clear();
        currWriteValues.clear();
        currWriteTags.clear();
      }
    }

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

    // private IAtomicValue oldValue = IAtomicValue.NULL;

    private IAtomicValue curValue = IAtomicValue.NULL;

    private NodeId nodeId;

    private boolean changed = false;

    private boolean toClear = false;

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
      toClear = true;
      // changed = false;
      // tag.setDirty( false );
    }

    void setToBuffer() {
      if( tag.isDirty() ) {
        changed = !curValue.equals( tag.newValue );
        curValue = tag.newValue;

        if( !changed ) {
          if( toClear ) {
            tag.setDirty( false );
          }
          else {
            changed = true;
          }
        }
        toClear = false;
      }
      else {
        changed = false;
        toClear = false;
      }
    }

    boolean isChanged() {
      return changed;
    }

    Variant getValue() {
      Variant result = OpcUaUtils.convertToOpc( curValue, tag.valueType(), tag.valueTypeExtra() );
      // oldValue = curValue;
      return result;
    }

    NodeId getNodeId() {
      return nodeId;
    }

  }

  public void config( IAvTree aCfgInfo )
      throws UaException {

    for( int i = 0; i < aCfgInfo.arrayLength(); i++ ) {
      IAvTree groupConfig = aCfgInfo.arrayElement( i );
      if( groupConfig.structId().endsWith( ".output.group.def" ) ) {

        // output
        IAvTree tagsConfig = groupConfig.nodes().findByKey( OUTPUT_TAGS_PARAM_NAME );
        IList<TagCfgItem> outputTagsCfgItems = OpcUaUtils.createTagsCfgItems( tagsConfig );

        for( int j = 0; j < outputTagsCfgItems.size(); j++ ) {
          TagCfgItem item = outputTagsCfgItems.get( j );
          NodeId nodeId = OpcUaUtils.createNodeFromCfg( item );

          try {
            client.getAddressSpace().getVariableNode( nodeId );
          }
          catch( UaException uaEx ) {
            logger.error( "Write tag '%s' creation faild '%s'", nodeId.toParseableString(), uaEx.getMessage() );
            continue;
          }

          TagImpl tag = new TagImpl( nodeId.toParseableString(), EKind.W, item.getTagType(), item.getTagTypeExtra(),
              item.isControlWord() );
          tags.put( tag.id(), tag );

          BufferedUaTag bTag = new BufferedUaTag( tag, nodeId );
          bufferedTags.add( bTag );
        }
      }
    }
  }

  IMap<String, TagImpl> getTags() {
    return tags;
  }
}

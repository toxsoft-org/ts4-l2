package org.toxsoft.l2.thd.opcua;

import java.util.*;
import java.util.concurrent.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.impl.*;

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
  private IStridablesList<L2AbstractTag> tags = new StridablesList<>();

  /**
   * Промежуточный буфер, состоящий из спец. тегов, агрегирующих теги записи
   */
  private IListEdit<BufferedUaTag> bufferedTags = new ElemArrayList<>();

  private List<NodeId>        currWriteIds    = new ArrayList<>();
  private List<DataValue>     currWriteValues = new ArrayList<>();
  private List<BufferedUaTag> currWriteTags   = new ArrayList<>();
  // карта неудачных попыток записать значение в тег
  private IStringMapEdit<Integer> nodeId2ErrorCnt = new StringMap<>();

  /**
   * @param aClient
   */
  public NodesWriter( OpcUaClient aClient, IStridablesList<L2AbstractTag> aTags ) {
    client = aClient;
    tags = aTags;
  }

  /**
   * Возвращает тег по иду
   *
   * @param aId
   * @return
   */
  public IL2Tag getTag( String aId ) {
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
          String key = currWriteTags.get( i ).getNodeId().toParseableString();

          if( code.isGood() ) {
            // currWriteTags.get( i ).clear(); //TODO FIX
            logger.debug( "tag %s cleared", currWriteTags.get( i ).getNodeId().toParseableString() );
            if( nodeId2ErrorCnt.hasKey( key ) ) {
              nodeId2ErrorCnt.removeByKey( key );
            }
          }
          else {
            logger.error( "tag %s error writing", currWriteTags.get( i ).getNodeId().toParseableString() );
            // dima 08.10.25 при ошибке делаем три попытки и отменяем выполнение этой команды
            if( !nodeId2ErrorCnt.hasKey( key ) ) {
              nodeId2ErrorCnt.put( key, Integer.valueOf( 1 ) );
            }
            else {
              int errCount = nodeId2ErrorCnt.getByKey( key ).intValue();
              errCount++;
              if( errCount >= 3 ) {
                logger.error( "tag %s error writing failed after %d attempts",
                    currWriteTags.get( i ).getNodeId().toParseableString(), errCount );
                // currWriteTags.get( i ).clear(); //TODO FIX
                nodeId2ErrorCnt.removeByKey( key );
              }
              else {
                nodeId2ErrorCnt.put( key, Integer.valueOf( errCount ) );
              }
            }
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
   * Класс буфферезированного тега с признаком изменения
   *
   * @author max
   */
  static class BufferedUaTag {

    private IL2Tag tag;

    // private IAtomicValue oldValue = IAtomicValue.NULL;

    private IAtomicValue curValue = IAtomicValue.NULL;

    private NodeId nodeId;

    private boolean changed = false;

    BufferedUaTag( IL2Tag aTag, NodeId aNodeId, IAtomicValue aCurValue ) {
      tag = aTag;
      curValue = aCurValue;
      nodeId = aNodeId;
    }

    BufferedUaTag( IL2Tag aTag, NodeId aNodeId ) {
      this( aTag, aNodeId, IAtomicValue.NULL );
    }

    void setToBuffer() {

      changed = !curValue.equals( tag.get() );
      curValue = tag.get();

    }

    boolean isChanged() {
      return changed;
    }

    Variant getValue() {
      Variant result = OpcUaUtils.convertToOpc( curValue, tag.atomicType(), "tag.valueTypeExtra()" );
      // oldValue = curValue;
      return result;
    }

    NodeId getNodeId() {
      return nodeId;
    }

  }

  public void config( IAvTree aCfgInfo ) {

    for( IL2Tag tag : tags ) {

      BufferedUaTag bTag = new BufferedUaTag( tag, NodeId.parse( tag.id() ) );
      bufferedTags.add( bTag );
    }
    logger.info( "Output group: successfully added %s nodes from %s", String.valueOf( tags.size() ),
        String.valueOf( tags.size() ) );

  }

  IStridablesList<L2AbstractTag> getTags() {
    return tags;
  }

}

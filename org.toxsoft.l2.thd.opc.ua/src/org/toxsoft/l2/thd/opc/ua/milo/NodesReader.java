package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import java.util.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.sdk.client.nodes.*;
import org.eclipse.milo.opcua.sdk.client.subscriptions.*;
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
 * Читатель тегов OPC UA
 *
 * @author max
 */
public class NodesReader {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Opc клиент
   */
  private OpcUaClient client;

  /**
   * Группа синхронных данных
   */
  private IListEdit<UaVariableNode> syncGroup;

  /**
   * Промежуточный буфер для хранения значений считанных с OPC синхронных данных
   */
  private IMapEdit<String, Variant> bufferSynchVal = new ElemMap<>();

  /**
   * Промежуточный буфер для хранения значений считанных с OPC асинхронных данных
   */
  private IMapEdit<String, Variant> bufferAsynchVal = new ElemMap<>();

  /**
   * Теги
   */
  private IMapEdit<String, TagImpl> tags = new ElemMap<>();

  /**
   * @param aClient
   */
  public NodesReader( OpcUaClient aClient ) {
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

  public void readValuesFromNodes() {
    try {
      for( UaVariableNode dNode : syncGroup ) {
        // .getVariableNode( new NodeId( 2, "TEST SERVER.PLC TAGS FOR OPC SERVER.COMPRESSOR ON" ) );

        // UaVariableNode dNode = client.getAddressSpace().getVariableNode(new NodeId(2,"COMPRESSOR ON"));

        DataValue dValue = dNode.readValue();
        Variant value = dValue.getValue();

        bufferSynchVal.put( dNode.getNodeId().toParseableString(), value );
      }

    }
    catch( UaException ex ) {
      ex.printStackTrace();
    }

  }

  public void readValuesFromBuffer() {
    readValuesFromBuffer( bufferSynchVal );
    readValuesFromAsyncBuffer();
  }

  /**
   * Метод вызывается при поступлении асинхронных данных
   *
   * @param aItems
   * @param aValues
   */
  public void onDataChanged( List<ManagedDataItem> aItems, List<DataValue> aValues ) {
    for( int i = 0; i < aItems.size(); i++ ) {
      ManagedDataItem dataItem = aItems.get( i );
      DataValue dataValue = aValues.get( i );

      String tagId = dataItem.getNodeId().toParseableString();

      TagImpl tag = tags.getByKey( tagId );

      EAtomicType tagType = tag.valueType();
      Variant vValue = dataValue.getValue();

      IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
      tag.updateVal( atomicVal );
    }
  }

  public void config( IAvTree aCfgInfo )
      throws UaException {

    // synch
    IAvTree tagsConfig = aCfgInfo.nodes().findByKey( SYNC_TAGS_PARAM_NAME );
    IList<TagCfgItem> synchTagsCfgItems = OpcUaUtils.createTagsCfgItems( tagsConfig );

    for( int i = 0; i < synchTagsCfgItems.size(); i++ ) {
      TagCfgItem item = synchTagsCfgItems.get( i );
      NodeId nodeId = new NodeId( item.getNamespaceId(), item.getTagId() );
      UaVariableNode dNode = client.getAddressSpace().getVariableNode( nodeId );
      syncGroup.add( dNode );

      TagImpl tag = new TagImpl( dNode.getNodeId().toParseableString(), EKind.R, item.getTagType() );
      tags.put( tag.tagId(), tag );
    }

    // async
    tagsConfig = aCfgInfo.nodes().findByKey( ASYNC_TAGS_PARAM_NAME );
    IList<TagCfgItem> asynchTagsCfgItems = OpcUaUtils.createTagsCfgItems( tagsConfig );

    ManagedSubscription subscription = ManagedSubscription.create( client );

    subscription.addDataChangeListener( ( items, values ) -> {
      onDataChanged( items, values );
    } );

    for( int i = 0; i < asynchTagsCfgItems.size(); i++ ) {
      TagCfgItem item = asynchTagsCfgItems.get( i );
      NodeId nodeId = new NodeId( item.getNamespaceId(), item.getTagId() );
      ManagedDataItem dataItem = subscription.createDataItem( nodeId );
      if( dataItem.getStatusCode().isGood() ) {
        logger.debug( "item created for nodeId={}", dataItem.getNodeId() );
        TagImpl tag = new TagImpl( dataItem.getNodeId().toParseableString(), EKind.R, item.getTagType() );
        tags.put( tag.tagId(), tag );
      }
      else {
        logger.error( "failed to create item for nodeId={} (status={})", dataItem.getNodeId(),
            dataItem.getStatusCode() );
      }

    }

  }

  private synchronized void readValuesFromAsyncBuffer() {
    readValuesFromBuffer( bufferAsynchVal );
  }

  private synchronized void setValuesToAsyncBuffer( List<ManagedDataItem> aItems, List<DataValue> aValues ) {
    for( int i = 0; i < aItems.size(); i++ ) {
      DataValue asyncDataValue = aValues.get( i );
      Variant asyncValue = asyncDataValue.getValue();

      bufferAsynchVal.put( aItems.get( i ).getNodeId().toParseableString(), asyncValue );
    }
  }

  private void readValuesFromBuffer( IMapEdit<String, Variant> aBuffer ) {
    IList<String> synchGroupKeys = aBuffer.keys();
    for( String key : synchGroupKeys ) {
      TagImpl tag = tags.getByKey( key );

      EAtomicType tagType = tag.valueType();
      Variant vValue = aBuffer.removeByKey( key );

      IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
      tag.updateVal( atomicVal );
    }
  }

}

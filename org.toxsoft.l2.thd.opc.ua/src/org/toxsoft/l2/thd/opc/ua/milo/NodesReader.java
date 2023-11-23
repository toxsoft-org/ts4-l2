package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import java.util.*;
import java.util.concurrent.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.*;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscriptionManager.*;
import org.eclipse.milo.opcua.sdk.client.subscriptions.*;
import org.eclipse.milo.opcua.stack.core.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Читатель тегов OPC UA
 *
 * @author max
 */
public class NodesReader {

  /**
   * Настройка подписки опроса асинхронных данных
   */
  private static final double ASYNCH_SUBSCRIPTION_SAMPLING_INTERVAL = 200.0;

  /**
   * Настройка подписки выдачи асинхронных данных
   */
  private static final double ASYNCH_SUBSCRIPTION_PUBLISHING_INTERVAL = 200.0;

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
  // private IListEdit<UaVariableNode> syncGroup = new ElemArrayList<>();
  private List<NodeId> syncGroup = new ArrayList<>();

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
   * Текущая подписка на асинхронные данные
   */
  private ManagedSubscription currSubscription;

  private int health = 100;

  private int healthBuffer = 100;

  /**
   * @param aClient
   */
  public NodesReader( OpcUaClient aClient ) {
    client = aClient;

    client.getSubscriptionManager().addSubscriptionListener( new SubscriptionListener() {

      @Override
      public void onSubscriptionTransferFailed( UaSubscription subscription, StatusCode statusCode ) {
        logger.info( "* onSubscriptionTransferFailed" );

        // reinitSubscription();
      }

    } );
  }

  private void reinitSubscription() {
    if( currSubscription == null ) {
      return;
    }

    logger.info( "Try to reinit Subscription" );

    // повторная регистрация асинхронных тегов

    List<ManagedDataItem> dataItems = currSubscription.getDataItems();
    IListEdit<NodeId> newNodeIds = new ElemArrayList<>();
    for( ManagedDataItem mDataItem : dataItems ) {
      newNodeIds.add( mDataItem.getNodeId() );
    }

    try {
      currSubscription.delete();
    }
    catch( UaException e1 ) {
      logger.error( e1 );
    }

    try {
      currSubscription = createSubscriptionAndRegAsynchNodes( newNodeIds );
    }
    catch( UaException e ) {
      logger.error( e.getMessage() );
    }
  }

  int getHealth() {
    return health;
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
    // logger.debug( "Start readValuesFromNodes" );
    // if( syncGroup.size() == 0 ) {
    // return;
    // }
    try {
      CompletableFuture<List<DataValue>> dValuesF = client.readValues( 0, TimestampsToReturn.Source, syncGroup );

      List<DataValue> dValues = dValuesF.get();

      for( int i = 0; i < syncGroup.size(); i++ ) {
        bufferSynchVal.put( syncGroup.get( i ).toParseableString(), dValues.get( i ).getValue() );

        // if( syncGroup.get( i ).toParseableString().equals( "ns=3;s=\"TP1\".\"CV\"" ) ) {
        // logger.debug( "Value of SyncTag=%s is %s", syncGroup.get( i ).toParseableString(),
        // dValues.get( i ).getValue().toString() );
        // }

      }

      if( healthBuffer == 0 ) {
        reinitSubscription();
      }

      healthBuffer = 100;

      // for( UaVariableNode dNode : syncGroup ) {
      //
      // // .getVariableNode( new NodeId( 2, "TEST SERVER.PLC TAGS FOR OPC SERVER.COMPRESSOR ON" ) );
      //
      // // UaVariableNode dNode = client.getAddressSpace().getVariableNode(new NodeId(2,"COMPRESSOR ON"));
      //
      // DataValue dValue = dNode.readValue();
      // Variant value = dValue.getValue();
      //
      // if( dNode.getNodeId().toParseableString().equals( "ns=3;s=\"BHB\".\"X0\"" ) ) {
      // logger.debug( "Value of SyncTag=%s is %s", dNode.getNodeId().toParseableString(), value.toString() );
      // }
      //
      // if( value == null ) {
      // logger.debug( "Value of SyncTag=%s is NULL", dNode.getNodeId().toParseableString() );
      // }
      //
      // bufferSynchVal.put( dNode.getNodeId().toParseableString(), value );
      // }
    }
    catch( Exception ex ) {
      healthBuffer = 0;
      logger.error( ex );
    }
    // logger.debug( "End readValuesFromNodes" );

  }

  public void readValuesFromBuffer() {
    health = healthBuffer;
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
    logger.debug( "Async Tags changed count %s", String.valueOf( aItems.size() ) );
    setValuesToAsyncBuffer( aItems, aValues );
    // for( int i = 0; i < aItems.size(); i++ ) {
    // ManagedDataItem dataItem = aItems.get( i );
    // DataValue dataValue = aValues.get( i );
    //
    // String tagId = dataItem.getNodeId().toParseableString();
    //
    // TagImpl tag = tags.getByKey( tagId );
    //
    // EAtomicType tagType = tag.valueType();
    // Variant vValue = dataValue.getValue();
    //
    // if( vValue == null ) {
    // logger.debug( "Value of AsyncTag=%s is NULL", tagId );
    // }
    // else {
    // // logger.debug( "Value of AsyncTag=%s is %s", tagId, vValue.toString() );
    // }
    //
    // IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
    // tag.updateVal( atomicVal );
    // }
  }

  public void config( IAvTree aCfgInfo )
      throws UaException {

    for( int i = 0; i < aCfgInfo.arrayLength(); i++ ) {
      IAvTree groupConfig = aCfgInfo.arrayElement( i );
      if( groupConfig.structId().endsWith( SYNC_GROUP_DEF_POSTFIX ) ) {
        // synch
        IAvTree tagsConfig = groupConfig.nodes().findByKey( SYNC_TAGS_PARAM_NAME );
        IList<TagCfgItem> synchTagsCfgItems = OpcUaUtils.createTagsCfgItems( tagsConfig );

        for( int j = 0; j < synchTagsCfgItems.size(); j++ ) {
          TagCfgItem item = synchTagsCfgItems.get( j );
          NodeId nodeId = OpcUaUtils.createNodeFromCfg( item );

          try {
            client.getAddressSpace().getVariableNode( nodeId );
          }
          catch( UaException uaEx ) {
            logger.error( "Synch read tag '%s' creation faild '%s'", nodeId.toParseableString(), uaEx.getMessage() );
            continue;
          }

          syncGroup.add( nodeId );
          // UaVariableNode dNode = client.getAddressSpace().getVariableNode( nodeId );
          // syncGroup.add( dNode );

          TagImpl tag = new TagImpl( nodeId.toParseableString(), EKind.R, item.getTagType(), item.getTagTypeExtra(),
              item.isControlWord() );
          tags.put( tag.tagId(), tag );
        }
        logger.info( "Sync group: successfully formed %s tags", String.valueOf( syncGroup.size() ) );
      }
      else
        if( groupConfig.structId().endsWith( ASYNC_GROUP_DEF_POSTFIX ) ) {
          // async
          IAvTree tagsConfig = groupConfig.nodes().findByKey( ASYNC_TAGS_PARAM_NAME );
          IList<TagCfgItem> asynchTagsCfgItems = OpcUaUtils.createTagsCfgItems( tagsConfig );

          IListEdit<NodeId> asynchNodeIds = new ElemArrayList<>();

          // список идов потенциальных тегов
          IStringListEdit pretends = new StringArrayList();

          for( int j = 0; j < asynchTagsCfgItems.size(); j++ ) {
            TagCfgItem item = asynchTagsCfgItems.get( j );
            NodeId nodeId = OpcUaUtils.createNodeFromCfg( item );

            TagImpl tag = new TagImpl( nodeId.toParseableString(), EKind.R, item.getTagType(), item.getTagTypeExtra(),
                item.isControlWord() );

            // заочно добавляем тегов
            tags.put( tag.tagId(), tag );
            // добавляем иды
            pretends.add( tag.tagId() );

            asynchNodeIds.add( nodeId );
          }

          currSubscription = createSubscriptionAndRegAsynchNodes( asynchNodeIds );

          // проверяем зарегистрированные теги и исключаем их из претендентов
          for( ManagedDataItem di : currSubscription.getDataItems() ) {
            pretends.remove( di.getNodeId().toParseableString() );
          }

          // если в списке претендентов остались элементы - значит они не были зарегистрированы - их надо удалить из
          // списка тегов
          for( String nonRegId : pretends ) {
            tags.removeByKey( nonRegId );
          }
        }
    }
  }

  private ManagedSubscription createSubscriptionAndRegAsynchNodes( IList<NodeId> aAsynchNodeIds )
      throws UaException {
    ManagedSubscription subscription = ManagedSubscription.create( client, ASYNCH_SUBSCRIPTION_PUBLISHING_INTERVAL );
    subscription.setDefaultSamplingInterval( ASYNCH_SUBSCRIPTION_SAMPLING_INTERVAL );

    // subscription.addDataChangeListener( ( items, values ) -> {
    // onDataChanged( items, values );
    // } );

    int successAdded = 0;

    int STEP_SIZE = 100;

    for( int step = 0; step < aAsynchNodeIds.size(); ) {

      List<NodeId> asynchNodeIds = new ArrayList<>();
      for( int j = step; j < step + STEP_SIZE && j < aAsynchNodeIds.size(); j++ ) {
        asynchNodeIds.add( aAsynchNodeIds.get( j ) );
      }

      step = step + STEP_SIZE;

      try {
        List<ManagedDataItem> dataItems = subscription.createDataItems( asynchNodeIds );

        for( ManagedDataItem dataItem : dataItems ) {
          if( dataItem.getStatusCode().isGood() ) {
            // logger.debug( "item created for nodeId=%s", dataItem.getNodeId().toParseableString() );
            successAdded++;
          }
          else {
            logger.error( "failed to create item for nodeId=%s (status=%s)", dataItem.getNodeId().toParseableString(),
                dataItem.getStatusCode().toString() );
            subscription.deleteDataItem( dataItem );
            asynchNodeIds.remove( dataItem.getNodeId() );
          }
        }

        // первичное синхронное чтение зарегистрированных элементов (без буфера)
        try {
          CompletableFuture<List<DataValue>> dValuesF =
              client.readValues( 0, TimestampsToReturn.Source, asynchNodeIds );
          List<DataValue> dValues = dValuesF.get();

          for( int i = 0; i < asynchNodeIds.size(); i++ ) {
            String key = asynchNodeIds.get( i ).toParseableString();
            TagImpl tag = tags.getByKey( key );

            EAtomicType tagType = tag.valueType();
            Variant vValue = dValues.get( i ).getValue();

            IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
            // logger.debug( "Initial async read tag '%s' val= %s", key,
            // atomicVal.asString() + (vValue.isNull() ? ", null" : "") + (atomicVal.isAssigned() ? "" : "NULL") );
            tag.updateVal( atomicVal );
          }
          logger.debug( "Initial async read of %s tags", String.valueOf( asynchNodeIds.size() ) );
        }
        catch( Exception ex ) {
          logger.error( ex );
        }
      }
      catch( Exception uaEx ) {
        logger.error( "SubscriptionEx '%s'", uaEx.getMessage() );
      }
    }

    logger.info( "Async group: successfully added %s nodes from %s", String.valueOf( successAdded ),
        String.valueOf( aAsynchNodeIds.size() ) );

    subscription.addDataChangeListener( ( items, values ) -> {
      onDataChanged( items, values );
    } );

    return subscription;
  }

  private synchronized void readValuesFromAsyncBuffer() {
    // if( bufferAsynchVal.size() > 0 ) {
    // logger.debug( "readValuesFromAsyncBuffer (size): %s", String.valueOf( bufferAsynchVal ) );
    // }
    readValuesFromBuffer( bufferAsynchVal );
    bufferAsynchVal.clear();
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
      Variant vValue = aBuffer.getByKey( key );

      IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
      tag.updateVal( atomicVal );
    }
  }

  IMapEdit<String, TagImpl> getTags() {
    return tags;
  }

}

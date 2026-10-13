package org.toxsoft.l2.thd.opcua;

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
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.impl.*;

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

  private IStridablesListEdit<L2AbstractTag> syncTags;

  private IStridablesListEdit<L2AbstractTag> asyncTags;

  private IStridablesListEdit<L2AbstractTag> tags = new StridablesList<>();

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
   * Текущая подписка на асинхронные данные
   */
  private ManagedSubscription currSubscription;

  private int health = 100;

  private int healthBuffer = 100;

  /**
   * @param aOpcUaMiloDevice
   * @param aClient
   * @param aIStridablesList
   */
  public NodesReader( OpcUaClient aClient, IStridablesList<L2AbstractTag> aSyncTags,
      IStridablesList<L2AbstractTag> aAsyncTags ) {
    syncTags = new StridablesList<>( aSyncTags );
    asyncTags = new StridablesList<>( aAsyncTags );
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
  public IL2Tag getTag( String aId ) {

    return tags.getByKey( aId );
  }

  public void readValuesFromNodes() {
    // logger.debug( "Start readValuesFromNodes" );
    // if( syncGroup.size() == 0 ) {
    // return;
    // }
    // dima 12.03.24 пустая группа синхронных данных не должна "мешать жить"
    if( syncGroup.isEmpty() ) {
      return;
    }
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

    for( L2AbstractTag tag : syncTags ) {
      syncGroup.add( NodeId.parse( tag.id() ) );
      tags.add( tag );
    }
    logger.info( "Sync group: successfully formed %s tags", String.valueOf( syncGroup.size() ) );

    IListEdit<NodeId> asynchNodeIds = new ElemArrayList<>();

    for( IL2Tag tag : asyncTags ) {
      // добавляем иды

      asynchNodeIds.add( NodeId.parse( tag.id() ) );
    }

    currSubscription = createSubscriptionAndRegAsynchNodes( asynchNodeIds );

    // проверяем зарегистрированные теги и исключаем их из претендентов
    for( ManagedDataItem di : currSubscription.getDataItems() ) {
      tags.add( asyncTags.getByKey( di.getNodeId().toParseableString() ) );
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
            IL2Tag tag = tags.getByKey( key );

            EAtomicType tagType = tag.atomicType();
            Variant vValue = dValues.get( i ).getValue();

            IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
            // logger.debug( "Initial async read tag '%s' val= %s", key,
            // atomicVal.asString() + (vValue.isNull() ? ", null" : "") + (atomicVal.isAssigned() ? "" : "NULL") );
            tag.set( atomicVal );
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
      IL2Tag tag = tags.getByKey( key );

      EAtomicType tagType = tag.atomicType();
      Variant vValue = aBuffer.getByKey( key );

      IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
      tag.set( atomicVal );
    }
  }

  IStridablesList<L2AbstractTag> getTags() {
    return tags;
  }

}

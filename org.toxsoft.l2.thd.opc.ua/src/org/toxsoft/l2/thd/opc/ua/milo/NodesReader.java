package org.toxsoft.l2.thd.opc.ua.milo;

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

import ge.toxsoft.gwp.opcuabridge.*;

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
  private IList<UaVariableNode> syncGroup;
  /**
   * Группы асинхронных данных
   */
  // private OpcGroup asyncGroup;
  // private Map<String, OpcGroup> asyncGroupMap = new HashMap<>();
  /**
   * Группа выходных каналов
   */
  // private OpcGroup outputGroup;

  /**
   * Промежуточный буфер для хранения значений считанных с OPC синхронных данных
   */
  private IMapEdit<String, Variant> bufferSynchVal = new ElemMap<>();

  /**
   * Промежуточный буфер для хранения значений считанных с OPC асинхронных данных
   */
  private IMapEdit<String, Variant> bufferAsynchVal = new ElemMap<>();

  /**
   * Тип данных тега
   */
  // private IMapEdit<String, EAtomicType> types = new ElemMap<>();

  /**
   * Теги
   */
  private IMapEdit<String, ReadOpcUaTag> tags = new ElemMap<>();

  public NodesReader() {

  }

  /**
   * Возвращает тег по иду
   *
   * @param aId
   * @return
   */
  public IReadTag getTag( String aId ) {
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

      ReadOpcUaTag tag = tags.getByKey( tagId );

      EAtomicType tagType = tag.type();
      Variant vValue = dataValue.getValue();

      IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
      tag.setValue( atomicVal );
    }
  }

  public void config( IAvTree aCfgInfo ) {
    // TODO Auto-generated method stub

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
      ReadOpcUaTag tag = tags.getByKey( key );

      EAtomicType tagType = tag.type();
      Variant vValue = aBuffer.removeByKey( key );

      IAtomicValue atomicVal = OpcUaUtils.convertFromOpc( vValue, tagType );
      tag.setValue( atomicVal );
    }
  }
}

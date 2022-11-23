package ru.toxsoft.l2.thd.opc.da;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;
import static ru.toxsoft.l2.thd.opc.da.IL2Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;

/**
 * Создает коллекцию мостов opc2S5
 *
 * @author dima
 */
public class Opc2S5CollectionProducer
    implements IDevicesProducer {

  /**
   * Обработчик ошибок нижнего уровня.
   */
  private IHalErrorProcessor errorProcessor;
  /**
   * Класс - совокупность мостов opc2S5
   */
  private Opc2S5Collection   opc2S5Collection;

  /**
   * Пустой конструктор.
   */
  public Opc2S5CollectionProducer() {
  }

  @Override
  public void setHalErrorProcessor( IHalErrorProcessor aErrorProcessor ) {
    errorProcessor = aErrorProcessor;

  }

  @Override
  public void configYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {
    opc2S5Collection = createOpc2S5Collection( aConfig );
  }

  /**
   * По описанию из конфигурации создает коллекцию мостов opc2S5
   *
   * @param aConfig конфигурация мостов
   * @return коллекция коллекцию мостов opc2S5
   */
  private Opc2S5Collection createOpc2S5Collection( IUnitConfig aConfig ) {
    IList<IOpc2S5Bridge> bridges = createBridges( aConfig );
    opc2S5Collection = new Opc2S5Collection( aConfig.id(), OPC2S5_BRIDGE_COLLECTION_DESCR, errorProcessor, bridges );
    // new Opc2S5Collection( OPC2S5_BRIDGE_COLLECTION_ID, OPC2S5_BRIDGE_COLLECTION_DESCR, errorProcessor, bridges );

    return opc2S5Collection;
  }

  /**
   * По описанию из конфигурации создает мосты opc2S5
   *
   * @param aConfig конфигурация мостов
   * @return список экземпляров мостов
   */
  private IList<IOpc2S5Bridge> createBridges( IUnitConfig aConfig ) {
    IListEdit<IOpc2S5Bridge> retVal = new ElemArrayList<>();
    // Получаем список описаний мостов и создаем по одному
    IAvTree params = aConfig.params();
    // Массив мостов
    IAvTree bridges = params.nodes().findByKey( BRIDGES_PARAM_NAME );
    for( int i = 0; i < bridges.arrayLength(); i++ ) {
      IAvTree bridgeConfig = bridges.arrayElement( i );
      // Считываем тип моста
      String bridgeType = bridgeConfig.fields().getStr( BRIDGE_TYPE_PARAM_NAME );
      if( bridgeType.compareTo( OPENSCADA_BRIDGE ) == 0 ) {
        IOpc2S5Bridge bridge = createOpenSCADABridge( bridgeConfig );
        retVal.add( bridge );
      }
      else
        if( bridgeType.compareTo( JEASYOPC_BRIDGE ) == 0 ) {
          IOpc2S5Bridge bridge = createJEasyOpcBridge( bridgeConfig );
          retVal.add( bridge );
        }
        else {
          throw new TsIllegalStateRtException( UNKNOWN_BRIDGE_TYPE, bridgeType );
        }
    }
    return retVal;
  }

  /**
   * По описанию из конфигурации создает один мост на движке JEasyOPC
   *
   * @param aBridgeConfig конфигурация моста
   * @return мост
   */
  private JEasyOpc2S5Bridge createJEasyOpcBridge( IAvTree aBridgeConfig ) {
    // По описанию создаем мост
    ConnectionInformation ci = new ConnectionInformation();
    String host = aBridgeConfig.fields().getStr( HOST_PARAM_NAME );
    ci.setHost( host );
    String progId = aBridgeConfig.fields().getStr( PROGID_PARAM_NAME );
    ci.setProgId( progId );
    IAvTree groupsConfig = aBridgeConfig.nodes().findByKey( GROUPS_PARAM_NAME );
    int syncPeriod = 500;
    IList<OpcTagPinDefinition> synсPins = null, asynсPins = null, outputPins = null;
    for( int i = 0; i < groupsConfig.arrayLength(); i++ ) {
      IAvTree groupConfig = groupsConfig.arrayElement( i );
      if( groupConfig.structId().endsWith( SYNC_GROUP_DEF_POSTFIX ) ) {
        syncPeriod = groupConfig.fields().getInt( PERIOD_PARAM_NAME );
        synсPins = createPins( groupConfig, SYNC_TAGS_PARAM_NAME );
      }
      else
        if( groupConfig.structId().endsWith( ASYNC_GROUP_DEF_POSTFIX ) ) {
          asynсPins = createPins( groupConfig, ASYNC_TAGS_PARAM_NAME );
        }
        else {
          outputPins = createPins( groupConfig, OUTPUT_TAGS_PARAM_NAME );
        }
    }
    String bridgeId = progId;

    JEasyOpc2S5Bridge retVal =
        new JEasyOpc2S5Bridge( bridgeId, bridgeId, errorProcessor, ci, synсPins, syncPeriod, asynсPins, outputPins );
    return retVal;
  }

  /**
   * По описанию из конфигурации создает один мост на движке openSCADA
   *
   * @param aBridgeConfig конфигурация моста
   * @return мост
   */
  private OpenSCADA2S5Bridge createOpenSCADABridge( IAvTree aBridgeConfig ) {
    // По описанию создаем мост
    ConnectionInformation ci = new ConnectionInformation();
    String host = aBridgeConfig.fields().getStr( HOST_PARAM_NAME );
    ci.setHost( host );
    String user = aBridgeConfig.fields().getStr( USER_PARAM_NAME );
    ci.setUser( user );
    String pswd = aBridgeConfig.fields().getStr( PASSWORD_PARAM_NAME );
    ci.setPassword( pswd );
    String progId = aBridgeConfig.fields().getStr( PROGID_PARAM_NAME );
    ci.setProgId( progId );
    String clsId = aBridgeConfig.fields().getStr( CLSID_PARAM_NAME );
    ci.setClsId( clsId );
    int syncPeriod = aBridgeConfig.fields().getInt( PERIOD_PARAM_NAME );
    IAvTree groupsConfig = aBridgeConfig.nodes().findByKey( GROUPS_PARAM_NAME );
    IList<OpcTagPinDefinition> synсPins = null, asynсPins = null, outputPins = null;
    for( int i = 0; i < groupsConfig.arrayLength(); i++ ) {
      IAvTree groupConfig = groupsConfig.arrayElement( i );
      if( groupConfig.structId().endsWith( SYNC_GROUP_DEF_POSTFIX ) ) {
        syncPeriod = groupConfig.fields().getInt( PERIOD_PARAM_NAME );
        synсPins = createPins( groupConfig, SYNC_TAGS_PARAM_NAME );
      }
      else
        if( groupConfig.structId().endsWith( ASYNC_GROUP_DEF_POSTFIX ) ) {
          asynсPins = createPins( groupConfig, ASYNC_TAGS_PARAM_NAME );
        }
        else {
          outputPins = createPins( groupConfig, OUTPUT_TAGS_PARAM_NAME );
        }
    }

    OpenSCADA2S5Bridge retVal =
        new OpenSCADA2S5Bridge( progId, progId, errorProcessor, ci, synсPins, syncPeriod, asynсPins, outputPins );
    return retVal;
  }

  /**
   * По дереву конфигурации группы создает список пинов
   *
   * @param aGroupConfig дерево конфигурации
   * @param aTagGroupName название тегов
   * @return список описания пинов
   */
  private static IList<OpcTagPinDefinition> createPins( IAvTree aGroupConfig, String aTagGroupName ) {
    IAvTree tagsConfig = aGroupConfig.nodes().findByKey( aTagGroupName );
    IListEdit<OpcTagPinDefinition> pinDefs = new ElemArrayList<>();
    for( int i = 0; i < tagsConfig.arrayLength(); i++ ) {
      IAvTree pinConfig = tagsConfig.arrayElement( i );
      OpcTagPinDefinition pinDef = createOpcTagPinDefinition( pinConfig );
      pinDefs.add( pinDef );
    }
    return pinDefs;
  }

  /**
   * По описанию из конфигурации создает пин OPC
   *
   * @param aPinConfig дерево конфигурация пина
   * @return пин
   */
  private static OpcTagPinDefinition createOpcTagPinDefinition( IAvTree aPinConfig ) {
    String pinTypeId = aPinConfig.fields().getStr( PIN_TYPE_PARAM_NAME );
    EAtomicType pinType = EAtomicType.findById( pinTypeId );
    String id = aPinConfig.fields().getStr( PIN_ID_PARAM_NAME );
    String tagId = aPinConfig.fields().getStr( OPC_TAG_PARAM_NAME );

    return new OpcTagPinDefinition( id, tagId, pinType );
  }

  @Override
  public AbstractPinsDevice createPinsDevice()
      throws Exception {
    return null;
  }

  @Override
  public IStridablesList<AbstractSpecificDevice> createSpecificDevices()
      throws Exception {
    StridablesList<AbstractSpecificDevice> devices = new StridablesList<>();
    devices.add( opc2S5Collection );
    return devices;
  }
}

package ru.toxsoft.l2.thd.opc.da;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

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
 * Создатель тестового драйвера OPC
 *
 * @author Max
 */
public class TestOpcDeviceProducer
    implements IDevicesProducer {

  private static final String TEST_OPC_DRIVER_DESCRIPTION_STR = "DRIVER FOR OPC DLM TESTING"; //$NON-NLS-1$

  private static final String TEST_OPC_DEVICE_ID = "opc2s5.bridge.collection.id"; //$NON-NLS-1$
  /**
   * Обработчик ошибок нижнего уровня.
   */
  private IHalErrorProcessor  errorProcessor;

  private TestOpcDevice bridge;

  @Override
  public void configYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {
    // Получаем список описаний мостов и создаем по одному
    IAvTree params = aConfig.params();
    // Массив мостов
    IAvTree bridges = params.nodes().findByKey( BRIDGES_PARAM_NAME );

    IAvTree bridgeConfig = bridges.arrayElement( 0 );

    bridge = createOpenSCADABridge( bridgeConfig );

  }

  /**
   * По описанию из конфигурации создает один мост на движке openSCADA
   *
   * @param aBridgeConfig конфигурация моста
   * @return мост
   */
  private TestOpcDevice createOpenSCADABridge( IAvTree aBridgeConfig ) {
    // По описанию создаем мост

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

    TestOpcDevice retVal = new TestOpcDevice( TEST_OPC_DEVICE_ID, TEST_OPC_DRIVER_DESCRIPTION_STR, errorProcessor,
        synсPins, syncPeriod, asynсPins, outputPins );
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
  public void setHalErrorProcessor( IHalErrorProcessor aErrorProcessor ) {
    errorProcessor = aErrorProcessor;
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
    // devices.add( new TestOpcDevice( TEST_OPC_DEVICE_ID, TEST_OPC_DRIVER_DESCRIPTION_STR, errorProcessor ) );
    devices.add( bridge );
    return devices;

  }

}

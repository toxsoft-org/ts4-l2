/**
 *
 */
package ru.toxsoft.l2.thd.opc.da;

import static ru.toxsoft.l2.core.hal.IHalHardConstants.*;
import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;
import static ru.toxsoft.l2.thd.opc.da.IL2Resources.*;

import java.io.*;
import java.util.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.cfg.impl.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.thd.opc.*;

import javafish.clients.opc.*;
import javafish.clients.opc.asynch.*;
import javafish.clients.opc.component.*;
import javafish.clients.opc.exception.*;
import javafish.clients.opc.variant.*;

/**
 * тест движка JEasyOpc
 *
 * @author dima
 */
public class JEasyOpcTest
    implements OpcAsynchGroupListener {

  /**
   * Для корректной сериализации.
   */
  private static final long     serialVersionUID    = 1L;
  @SuppressWarnings( "nls" )
  private static final String   SYNC_GROUP          = "ops2s5_sync_data_group";
  @SuppressWarnings( "nls" )
  private static final String   ASYNC_GROUP         = "ops2s5_async_data_group";
  @SuppressWarnings( "nls" )
  private static final String   OUTPUT_GROUP        = "ops2s5_output_tags_group";
  /**
   * OPC client
   */
  private final JEasyOpc        opcClient;
  /**
   * Группа синхронных данных
   */
  private OpcGroup              syncGroup;
  /**
   * Группы асинхронных данных
   */
  // private OpcGroup asyncGroup;
  private Map<String, OpcGroup> asyncGroupMap       = new HashMap<>();
  /**
   * Группа выходных каналов
   */
  private OpcGroup              outputGroup;
  /**
   * Журнал работы
   */
  private ILogger               logger              = LoggerWrapper.getLogger( this.getClass().getName() );
  /**
   * Промежуточный буфер для хранения значений считанных с OPC
   */
  private Map<String, Variant>  bufferVal           = new HashMap<>();
  /**
   * Список элементов синхронного обновления
   */
  private ArrayList<OpcItem>    syncItems           = new ArrayList<>();
  /**
   * Список элементов aсинхронного обновления
   */
  private ArrayList<OpcItem>    asyncItems          = new ArrayList<>();
  /**
   * Список групп aсинхронного обновления
   */
  private ArrayList<OpcGroup>   asyncGroups         = new ArrayList<>();
  /**
   * Список элементов для записи
   */
  private ArrayList<OpcItem>    outputItems         = new ArrayList<>();
  /**
   * Карта tag id -> opc item
   */
  private Map<String, OpcItem>  tagId2OutputIem     = new HashMap<>();
  /**
   * Период обновления синхронных данных мс
   */
  private int                   updatePeriod;
  /**
   * Метка последнего обновления
   */
  private long                  lastUpdateTimestamp = 0;

  private StringMap<ITag> tags          = new StringMap<>();
  private StringMap<ITag> readableTags  = new StringMap<>();
  private StringMap<ITag> writeableTags = new StringMap<>();

  /**
   * @param aCi - информация о соединении
   * @param aSyncTags - описание синхронных тегов
   * @param aUpdatePeriod - период обновления
   * @param aAsyncTags - описание асинхронных тегов
   * @param aOutputTags - описание тегов на запись
   */
  public JEasyOpcTest( IConnectionInformation aCi, IList<OpcTagPinDefinition> aSyncTags, int aUpdatePeriod,
      IList<OpcTagPinDefinition> aAsyncTags, IList<OpcTagPinDefinition> aOutputTags ) {
    try {
      JCustomOpc.coInitialize();
    }
    catch( CoInitializeException ex ) {
      logger.error( ex );
    }
    updatePeriod = aUpdatePeriod;
    // Создаем соединение с сервером
    opcClient = new JEasyOpc( aCi.host(), aCi.progId(), OPC2S5_BRIDGE_CLIENT_HANDLE );
    try {
      // connect to server
      opcClient.connect();
      // Группа синхронных данных
      syncGroup = new OpcGroup( SYNC_GROUP, true, 500, 0.0f );
      // Группа асинхронных данных
      // asyncGroup = new OpcGroup( ASYNC_GROUP, true, aUpdatePeriod, 0.0f );
      // Группа выходных пинов
      outputGroup = new OpcGroup( OUTPUT_GROUP, true, aUpdatePeriod, 0.0f );
    }
    catch( Exception e ) {
      logger.error( CANT_CONNECT_OPC, e.getMessage() );
    }
    for( OpcTagPinDefinition tagDef : aSyncTags ) {
      // if( tagDef.tagId().compareTo( "S71500ET200MP station_1.PLC_40.TW7.FV" ) != 0 ) { //$NON-NLS-1$
      // continue;
      // }
      OpcItem item = new OpcItem( tagDef.tagId(), true, EMPTY_STRING );
      syncGroup.addItem( item );
      syncItems.add( item );
      // Создаем S5 OPC tag
      ITag syncTag = new Tag( tagDef.tagId(), EKind.R, tagDef.valueType() );
      readableTags.put( tagDef.tagId(), syncTag );
      tags.put( tagDef.tagId(), syncTag );
    }
    try {
      opcClient.addGroup( syncGroup );
      opcClient.registerGroup( syncGroup );
    }
    catch( Exception e ) {
      logger.error( ERR_ADD_SYNC_GROUP );
    }
    // Добавляем теги и проверяем их правильность
    for( OpcItem syncItem : syncItems ) {
      try {
        opcClient.registerItem( syncGroup, syncItem );
      }
      catch( Exception e ) {
        logger.error( CANT_ADD_SYNC_TAG, syncItem.getItemName() );
        syncGroup.removeItem( syncItem );
      }
    }
    for( OpcTagPinDefinition tagDef : aAsyncTags ) {
      // if( tagDef.tagId().compareTo( "S71500ET200MP station_1.PLC_40.TW7.FV" ) != 0 ) { //$NON-NLS-1$
      // continue;
      // }
      // Защищаемся от дублирования
      if( readableTags.hasKey( tagDef.tagId() ) ) {
        continue;
      }
      OpcItem item = new OpcItem( tagDef.tagId(), true, EMPTY_STRING );
      // Группа асинхронных данных
      OpcGroup asyncGroup = getOpcGroup( aUpdatePeriod );
      asyncGroup.addItem( item );
      asyncGroupMap.put( item.getItemName(), asyncGroup );
      asyncItems.add( item );
      // Создаем S5 OPC tag
      ITag asyncTag = new Tag( tagDef.tagId(), EKind.R, tagDef.valueType() );
      readableTags.put( tagDef.tagId(), asyncTag );
      tags.put( tagDef.tagId(), asyncTag );
    }
    // Добавляем теги и проверяем их правильность
    try {
      for( OpcGroup asyncGroup : asyncGroups ) {
        opcClient.addGroup( asyncGroup );
        opcClient.registerGroup( asyncGroup );
        asyncGroup.addAsynchListener( this );
      }
    }
    catch( Exception e ) {
      logger.error( ERR_ADD_ASYNC_GROUP );
    }
    for( OpcItem asyncItem : asyncItems ) {
      OpcGroup asyncGroup = asyncGroupMap.get( asyncItem.getItemName() );
      try {
        opcClient.registerItem( asyncGroup, asyncItem );
      }
      catch( Exception e ) {
        logger.error( CANT_ADD_ASYNC_TAG, asyncItem.getItemName() );
        asyncGroup.removeItem( asyncItem );
      }
    }

    for( OpcTagPinDefinition tagDef : aOutputTags ) {
      OpcItem item = new OpcItem( tagDef.tagId(), true, EMPTY_STRING );
      outputGroup.addItem( item );
      outputItems.add( item );
      tagId2OutputIem.put( tagDef.tagId(), item );
      // Проверяем на существование уже такого тега (это если он типа RW)
      if( tags.hasKey( tagDef.tagId() ) ) {
        ITag rwTag = tags.getByKey( tagDef.tagId() );
        TsIllegalStateRtException.checkFalse( rwTag.kind() == EKind.R, ERR_WRITE_TAG_DUPLICATE, tagDef.tagId() );
        ((Tag)rwTag).setKind( EKind.RW );
        writeableTags.put( tagDef.tagId(), rwTag );
      }
      else {
        ITag outputTag = new Tag( tagDef.tagId(), EKind.W, tagDef.valueType() );
        writeableTags.put( tagDef.tagId(), outputTag );
        tags.put( tagDef.tagId(), outputTag );
      }
    }
    try {
      opcClient.addGroup( outputGroup );
      opcClient.registerGroup( outputGroup );
    }
    catch( Exception e ) {
      logger.error( ERR_ADD_SYNC_GROUP );
    }
    // Добавляем теги и проверяем их правильность
    for( OpcItem outputItem : outputItems ) {
      try {
        opcClient.registerItem( outputGroup, outputItem );
      }
      catch( Exception e ) {
        logger.error( CANT_ADD_OUTPUT_TAG, outputItem.getItemName() );
        outputGroup.removeItem( outputItem );
      }
    }
    // Начинаем работу
    opcClient.start();
  }

  int      tag_counter   = 0;
  int      group_counter = 1;
  OpcGroup currAsyncGroup;

  private OpcGroup getOpcGroup( int aUpdatePeriod ) {
    OpcGroup retVal = currAsyncGroup;
    if( tag_counter++ > 10 ) {
      tag_counter = 0;
      currAsyncGroup = new OpcGroup( ASYNC_GROUP + "_" + group_counter++, true, 500, 0.0f ); //$NON-NLS-1$
      asyncGroups.add( currAsyncGroup );
      retVal = currAsyncGroup;
    }
    if( currAsyncGroup == null ) {
      currAsyncGroup = new OpcGroup( ASYNC_GROUP + "_" + group_counter++, true, 500, 0.0f ); //$NON-NLS-1$
      asyncGroups.add( currAsyncGroup );
      retVal = currAsyncGroup;
    }
    return retVal;
  }

  @Override
  public void getAsynchEvent( AsynchEvent aEvent ) {
    OpcGroup asyncDataGroup = aEvent.getOPCGroup();
    System.out.println( asyncDataGroup.getGroupName() );
    for( OpcItem item : asyncDataGroup.getItems() ) {
      bufferVal.put( item.getItemName(), item.getValue() );
      // if( item.getItemName().compareTo( "S71500ET200MP station_1.PLC_40.TW7.FV" ) == 0 ) { //$NON-NLS-1$
      System.out.println( "-------------------------------------------" ); //$NON-NLS-1$
      System.out.println( "async : " + item.getItemName() + ": " + item.getValue() ); //$NON-NLS-1$
      System.out.println( "-------------------------------------------" ); //$NON-NLS-1$
      // }
    }
  }

  protected void readValuesFromLL()
      throws TsMultipleApparatRtException {
    long timestamp = System.currentTimeMillis();
    if( timestamp - lastUpdateTimestamp >= updatePeriod ) {
      lastUpdateTimestamp = timestamp;
      // Считываем значения с синхронных тегов
      try {
        OpcGroup responseGroup = opcClient.synchReadGroup( syncGroup );
        // рассовываем в промежуточный буфер
        System.out.println( "+++++++++++++++++++++++++++++++++++++++++++" ); //$NON-NLS-1$
        int i = 0;
        for( OpcItem item : responseGroup.getItems() ) {
          bufferVal.put( item.getItemName(), item.getValue() );
          System.out.println( "sync : " + item.getItemName() + " : " + item.getValue() ); //$NON-NLS-1$
        }
        System.out.println( "+++++++++++++++++++++++++++++++++++++++++++" ); //$NON-NLS-1$
      }
      catch( Exception e ) {
        logger.error( e );
      }
    }
  }

  /**
   * Тест
   *
   * @param a
   */
  public static void main( String[] a ) {

    // Считываем конфигурационный файл
    DefaultUnitConfigLoader loader = new DefaultUnitConfigLoader( DEVCFG, DEVICE_CONFIG );
    // загрузка конфигураций
    try {
      // получение конфигураций устройств по данным из конфигурационной
      // директории
      File dir = new File( "C:\\works\\git-repos\\tsmain\\ru.toxsoft.l2.core\\run\\cfg\\hal\\thds\\" );
      loader.loadConfig( dir );
    }
    catch( TsIoRtException e ) {
      return;
    }

    // получение конфигураций
    IStridablesList<IUnitConfig> cfgs = loader.listThDriverConfigs();

    // по каждой конфигурации инициализируется создатель пинов и спец.
    // устройств
    IUnitConfig aBridgeConfig = cfgs.get( 0 );
    // Получаем список описаний мостов и создаем по одному
    IAvTree params = aBridgeConfig.params();
    IAvTree bridges = params.nodes().findByKey( BRIDGES_PARAM_NAME );
    IAvTree bridgeConfig = bridges.arrayElement( 0 );
    // По описанию создаем мост
    ConnectionInformation ci = new ConnectionInformation();
    String host = bridgeConfig.fields().getStr( HOST_PARAM_NAME );
    ci.setHost( host );
    String progId = bridgeConfig.fields().getStr( PROGID_PARAM_NAME );
    ci.setProgId( progId );
    IAvTree groupsConfig = bridgeConfig.nodes().findByKey( GROUPS_PARAM_NAME );
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

    JEasyOpcTest test = new JEasyOpcTest( ci, synсPins, syncPeriod, asynсPins, outputPins );
    // Читаем значения с OPC
    for( int i = 0; i < 1000; i++ ) {
      test.readValuesFromLL();
      try {
        Thread.sleep( 1000 );
      }
      catch( InterruptedException ex ) {
        // TODO Auto-generated catch block
        ex.printStackTrace();
      }
    }
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

}

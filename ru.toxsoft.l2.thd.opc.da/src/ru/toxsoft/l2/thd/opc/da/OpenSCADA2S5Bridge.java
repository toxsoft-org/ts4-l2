/**
 *
 */
package ru.toxsoft.l2.thd.opc.da;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.thd.opc.da.IL2Resources.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.jinterop.dcom.common.*;
import org.jinterop.dcom.core.*;
//import org.openscada.opc.lib.da.AccessBase;
import org.openscada.opc.lib.da.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация моста opc 2 S5 на движке openSCADA
 *
 * @author dima
 */
public class OpenSCADA2S5Bridge
    extends AbstractSpecificDevice
    implements IOpc2S5Bridge, DataCallback {

  @SuppressWarnings( "nls" )
  private static final String SYNC_GROUP   = "ops2s5_sync_data_group";
  @SuppressWarnings( "nls" )
  private static final String ASYNC_GROUP  = "ops2s5_async_data_group";
  @SuppressWarnings( "nls" )
  private static final String OUTPUT_GROUP = "ops2s5_output_tags_group";
  /**
   * OPC server
   */
  private final Server        server;
  /**
   * Группа синхронных данных
   */
  private Group               syncGroup;
  /**
   * Группы асинхронных данных
   */

  private AccessBase          asyncGroup;
  // переходим полностью на синхронное чтение
  // private Group asyncGroup;
  /**
   * Группа выходных каналов
   */
  private Group                  outputGroup;
  /**
   * Журнал работы
   */
  private ILogger                logger    = LoggerWrapper.getLogger( this.getClass().getName() );
  /**
   * Промежуточный буфер для хранения значений считанных с OPC
   */
  private Map<String, JIVariant> bufferVal = new HashMap<>();
  /**
   * Список элементов синхронного обновления
   */
  private ArrayList<Item>        syncItems = new ArrayList<>();
  /**
   * Список элементов Асинхронного обновления
   */
  // private ArrayList<Item> asyncItems = new ArrayList<>();

  /**
   * Период обновления синхронных данных мс
   */
  private int               updatePeriod;
  /**
   * Карта tag id -> opc item
   */
  private Map<String, Item> tagId2OutputItem    = new HashMap<>();
  /**
   * Метка последнего обновления
   */
  private long              lastUpdateTimestamp = 0;

  private StringMap<ITag> tags = new StringMap<>();

  /**
   * Список тегов на чтение (и синхронные и асинхронные)
   */
  private StringMap<ITag> readableTags = new StringMap<>();

  /**
   * Список тегов на запись (пересекаются со множеством на чтение)
   */
  private StringMap<ITag>         writeableTags = new StringMap<>();
  /**
   * Контроллер переподключения к ОРС
   */
  private AutoReconnectController autoReconnectController;
  private AtomicBoolean           connected     = new AtomicBoolean( false );
  /**
   * Флаг - проверка тегов при инициализации
   */
  // TODO вынести в настройки *.devcfg
  private boolean                 initTagsCheck = false;                     // проверка убрана, чтобы не засирать лог
  /**
   * Флаг инициализации
   */
  private boolean                 initialized   = true;

  /**
   * @param aId String - строковый идентификатор.
   * @param aDescription String - описание.
   * @param aErrorProcessor {@link IHalErrorProcessor} - обработчик ошибок, возникающих при работе с НУ.
   * @param aCi - информация о соединении
   * @param aSyncTags - описание синхронных тегов
   * @param aUpdatePeriod - период обновления
   * @param aAsyncTags - описание асинхронных тегов
   * @param aOutputTags - описание тегов на запись
   */
  public OpenSCADA2S5Bridge( String aId, String aDescription, IHalErrorProcessor aErrorProcessor,
      IConnectionInformation aCi, IList<OpcTagPinDefinition> aSyncTags, int aUpdatePeriod,
      IList<OpcTagPinDefinition> aAsyncTags, IList<OpcTagPinDefinition> aOutputTags ) {
    super( aId, aDescription, aErrorProcessor );
    // create connection information
    final org.openscada.opc.lib.common.ConnectionInformation ci =
        new org.openscada.opc.lib.common.ConnectionInformation();
    ci.setHost( aCi.host() );
    ci.setUser( aCi.user() );
    ci.setPassword( aCi.password() );
    ci.setClsid( aCi.clsId() );

    updatePeriod = aUpdatePeriod;
    // Создаем соединение с сервером
    server = new Server( ci, Executors.newSingleThreadScheduledExecutor() );
    // disable GC for COM objects to prevent the socket from being closed
    JISystem.setJavaCoClassAutoCollection( false );
    // Turn off logging
    java.util.logging.Logger.getLogger( "org.jinterop" ).setLevel( java.util.logging.Level.OFF );
    autoReconnectController = new AutoReconnectController( server );
    autoReconnectController.addListener( new AutoReconnectListener() {

      @SuppressWarnings( "synthetic-access" )
      @Override
      public void stateChanged( AutoReconnectState state ) {
        logger.info( AUTO_RECONNECT_STATE, state.name() );
        if( state == AutoReconnectState.CONNECTED ) {
          if( initTagsCheck ) {
            checkOPCTags( aSyncTags, aAsyncTags, aOutputTags );
            initTagsCheck = false;
          }
          if( initialized ) {
            afterOPCConnection( aSyncTags, aUpdatePeriod, aAsyncTags, aOutputTags );
          }
          connected.set( true );
        }
        else {
          connected.set( false );
        }
      }
    } );
    // do some actions before connect
    beforeOPCConnection( aSyncTags, aAsyncTags, aOutputTags );
    autoReconnectController.connect();
  }

  /**
   * Подготовительная работа перед соединением с OPC
   *
   * @param aSyncTags описание синхронных тегов
   * @param aAsyncTags описание асинхронных тегов
   * @param aOutputTags описание выходных тегов
   */
  private void beforeOPCConnection( IList<OpcTagPinDefinition> aSyncTags, IList<OpcTagPinDefinition> aAsyncTags,
      IList<OpcTagPinDefinition> aOutputTags ) {
    readableTags.clear();
    tags.clear();
    writeableTags.clear();
    for( OpcTagPinDefinition tagDef : aSyncTags ) {
      // Создаем S5 OPC tag
      ITag syncTag = new Tag( tagDef.tagId(), EKind.R, tagDef.valueType() );
      readableTags.put( tagDef.tagId(), syncTag );
      tags.put( tagDef.tagId(), syncTag );
    }
    for( OpcTagPinDefinition tagDef : aAsyncTags ) {
      // Защищаемся от дублирования
      if( readableTags.hasKey( tagDef.tagId() ) ) {
        continue;
      }
      // Группа асинхронных данных
      // Создаем S5 OPC tag
      ITag asyncTag = new Tag( tagDef.tagId(), EKind.R, tagDef.valueType() );
      readableTags.put( tagDef.tagId(), asyncTag );
      tags.put( tagDef.tagId(), asyncTag );
    }

    for( OpcTagPinDefinition tagDef : aOutputTags ) {
      // Группа тегов на запись
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
  }

  /**
   * Проверка тегов ОРС сервера
   *
   * @param aSyncTags описание синхронных тегов
   * @param aAsyncTags описание асинхронных тегов
   * @param aOutputTags описание выходных тегов
   */
  @SuppressWarnings( "nls" )
  private void checkOPCTags( IList<OpcTagPinDefinition> aSyncTags, IList<OpcTagPinDefinition> aAsyncTags,
      IList<OpcTagPinDefinition> aOutputTags ) {
    Group testSyncGroup, testAsyncGroup, testOutputGroup;
    try {
      // Группа синхронных данных
      testSyncGroup = server.addGroup( "test_" + SYNC_GROUP );

      // Группа асинхронных данных
      testAsyncGroup = server.addGroup( "test_" + ASYNC_GROUP );

      // Группа выходных пинов
      testOutputGroup = server.addGroup( "test_" + OUTPUT_GROUP );
    }
    catch( Exception e ) {
      initialized = false;
      logger.error( CANT_CONNECT_OPC, e.getMessage() );
      return;
    }
    try {
      for( OpcTagPinDefinition tagDef : aSyncTags ) {
        try {
          testSyncGroup.addItem( tagDef.tagId() );
        }
        catch( final JIException e ) {
          initialized = false;
          logger.error( ERR_ADD_SYNC_GROUP );
        }
        catch( AddFailedException ex ) {
          initialized = false;
          logger.error( CANT_ADD_SYNC_TAG, tagDef.tagId() );
          testSyncGroup.removeItem( tagDef.tagId() );
        }
      }
      for( OpcTagPinDefinition tagDef : aAsyncTags ) {
        try {
          // Группа асинхронных данных
          testAsyncGroup.addItem( tagDef.tagId() );
        }
        catch( final JIException e ) {
          initialized = false;
          logger.error( ERR_ADD_ASYNC_GROUP );
        }
        catch( AddFailedException ex ) {
          initialized = false;
          logger.error( CANT_ADD_ASYNC_TAG, tagDef.tagId() );
          testAsyncGroup.removeItem( tagDef.tagId() );
        }
      }

      for( OpcTagPinDefinition tagDef : aOutputTags ) {
        try {
          // Группа тегов на запись
          testOutputGroup.addItem( tagDef.tagId() );
        }
        catch( final JIException e ) {
          initialized = false;
          logger.error( ERR_ADD_OUTPUT_GROUP );
        }
        catch( AddFailedException ex ) {
          initialized = false;
          logger.error( CANT_ADD_OUTPUT_TAG, tagDef.tagId() );
          testOutputGroup.removeItem( tagDef.tagId() );
        }
      }
    }
    catch( final JIException | UnknownHostException e ) {
      initialized = false;
      logger.error( ERROR_OPENSCADA_INIT, e.getMessage() );
    }
  }

  /**
   * Действия после подключения к ОРС серверу
   *
   * @param aSyncTags описание синхронных тегов
   * @param aUpdatePeriod период обновления
   * @param aAsyncTags описание асинхронных тегов
   * @param aOutputTags описание выходных тегов
   */
  private void afterOPCConnection( IList<OpcTagPinDefinition> aSyncTags, int aUpdatePeriod,
      IList<OpcTagPinDefinition> aAsyncTags, IList<OpcTagPinDefinition> aOutputTags ) {
    try {
      syncItems.clear();
      tagId2OutputItem.clear();
      // Группа синхронных данных
      syncGroup = server.addGroup( SYNC_GROUP );

      // Группа асинхронных данных
      asyncGroup = new Async20Access( server, aUpdatePeriod, true );
      // asyncGroup = server.addGroup( ASYNC_GROUP );

      // Группа выходных пинов
      outputGroup = server.addGroup( OUTPUT_GROUP );
    }
    catch( Exception e ) {
      logger.error( CANT_CONNECT_OPC, e.getMessage() );
      return;
    }
    try {
      logger.debug( "Synch tags adding to the group: %s", String.valueOf( aSyncTags.size() ) );
      for( OpcTagPinDefinition tagDef : aSyncTags ) {
        try {
          Item syncItem = syncGroup.addItem( tagDef.tagId() );
          syncItems.add( syncItem );
        }
        catch( final JIException e ) {
          logger.error( ERR_ADD_SYNC_GROUP );
        }
        catch( AddFailedException ex ) {
          logger.error( CANT_ADD_SYNC_TAG, tagDef.tagId() );
          syncGroup.removeItem( tagDef.tagId() );
        }
      }
      logger.debug( "Asynch tags adding to the group: %s", String.valueOf( aAsyncTags.size() ) );
      for( OpcTagPinDefinition tagDef : aAsyncTags ) {
        try {
          // Группа асинхронных данных
          asyncGroup.addItem( tagDef.tagId(), this );
          logger.debug( "Tag %s added to group: %s", tagDef.tagId(), "Async20Access" );

          // Item asyncItem = asyncGroup.addItem( tagDef.tagId() );
          // asyncItems.add( asyncItem );
        }
        catch( final JIException e ) {
          logger.error( ERR_ADD_ASYNC_GROUP );
        }
        catch( AddFailedException ex ) {
          logger.error( CANT_ADD_ASYNC_TAG, tagDef.tagId() );
          asyncGroup.removeItem( tagDef.tagId() );
        }
      }
      logger.debug( "Output tags adding to the group: %s", String.valueOf( aOutputTags.size() ) );
      for( OpcTagPinDefinition tagDef : aOutputTags ) {
        try {
          // Группа тегов на запись
          Item item = outputGroup.addItem( tagDef.tagId() );
          tagId2OutputItem.put( tagDef.tagId(), item );
        }
        catch( final JIException e ) {
          logger.error( ERR_ADD_OUTPUT_GROUP );
        }
        catch( AddFailedException ex ) {
          logger.error( CANT_ADD_OUTPUT_TAG, tagDef.tagId() );
          outputGroup.removeItem( tagDef.tagId() );
        }
      }
      // Начинаем работу c того что запрашиваем значения всех тегов
      asyncGroup.bind(); // binding removed for test
      logger.debug( "init OPC items OK" );

      readValuesFromLL();
      getFromBufferInputValues();
    }
    catch( final JIException | UnknownHostException e ) {
      logger.error( ERROR_OPENSCADA_INIT, e.getMessage() );
    }
  }

  @Override
  public void readValuesFromLL() {
    if( !connected.get() || !initialized ) {
      return;
    }
    long timestamp = System.currentTimeMillis();
    if( timestamp - lastUpdateTimestamp >= updatePeriod ) {
      lastUpdateTimestamp = timestamp;
      try {
        // Считываем значения с синхронных тегов
        // Переделываем на считывание всех тегов разом
        // for( Item syncItem : syncItems ) {
        // ItemState val = syncItem.read( false );
        // // рассовываем в промежуточный буфер
        // bufferVal.put( syncItem.getId(), val.getValue() );
        // if( syncItem.getId().compareTo( "S71500ET200MP station_1.PLC_40.TP1.CV" ) == 0 ) {
        // logger.debug( "1) PLC_40.TP1.CV = " + val.getValue() );
        // }
        // }
        Item[] itemsArray = new Item[syncItems.size()];
        itemsArray = syncItems.toArray( itemsArray );
        // logger.debug( "Synch items size: %s, array size: %s", String.valueOf( syncItems.size() ), String.valueOf(
        // itemsArray.length ) );
        Map<Item, ItemState> syncTags = syncGroup.read( false, itemsArray );

        // logger.debug( "Synch tags size: %s", String.valueOf( syncTags.size() ) );

        for( Item item : syncTags.keySet() ) {
          // рассовываем в промежуточный буфер
          JIVariant tagValue = syncTags.get( item ).getValue();
          bufferVal.put( item.getId(), tagValue );
          // if( item.getId().contains( "SIMATIC 300(1).CPU 314C-2 PtP.TP1.CV" ) ) {
          // logger.debug( "Tag: TP1.CV, Value: %s", tagValue.toString() );
          // }
        }
        // Считываем значения с Асинхронных тегов
        // TODO отключить и проверить асинхронную работу
        // itemsArray = new Item[asyncItems.size()];
        // itemsArray = asyncItems.toArray( itemsArray );
        // Map<Item, ItemState> asyncTags = asyncGroup.read( false, itemsArray );
        // for( Item item : asyncTags.keySet() ) {
        // // рассовываем в промежуточный буфер
        // JIVariant tagValue = asyncTags.get( item ).getValue();
        // bufferVal.put( item.getId(), tagValue );
        // }
      }
      catch( Exception e ) {
        logger.error( e );
        reconnect();
      }
    }
  }

  /**
   * Переподключаемся к ОРС серверу
   */
  private void reconnect() {
    // Обработка потери связи с OPC
    logger.debug( ERR_OPC_TRY_DISCONNECT );
    autoReconnectController.disconnect();
    logger.debug( ERR_OPC_TRY_RECONNECT );
    autoReconnectController.connect();
  }

  @Override
  public void writeValuesOnLL()
      throws TsMultipleApparatRtException {
    if( !connected.get() || !initialized ) {
      return;
    }
    try {
      for( ITag tag : writeableTags ) {
        TsIllegalStateRtException.checkFalse( tag instanceof Tag, ERROR_EXPECTED_TAG_TYPE );
        Tag tagImpl = (Tag)tag;
        if( tagImpl.isDirty() ) {
          Item outputItem = tagId2OutputItem.get( tagImpl.tagId() );
          JIVariant tagValue;
          switch( tag.valueType() ) {
            case FLOATING:
              //outputItem.write( new JIVariant( tagImpl.newValue.asFloat() ) );
              tagValue = new JIVariant( tagImpl.newValue.asFloat() );
              break;
            case INTEGER:
              // Short shortVal = Short.valueOf( (short)tagImpl.newValue.asInt() );
              // IJIUnsigned val = JIUnsignedFactory.getUnsigned( shortVal, JIFlags.FLAG_REPRESENTATION_UNSIGNED_SHORT
              // );
              // outputItem.write( new JIVariant( val ) );

              //outputItem.write( new JIVariant( tagImpl.newValue.asInt() ) );
              tagValue = new JIVariant( tagImpl.newValue.asInt() );
              break;
            case BOOLEAN:
              //outputItem.write( new JIVariant( tagImpl.newValue.asBool() ) );
              tagValue = new JIVariant( tagImpl.newValue.asBool() );
              break;
            case STRING:
              //outputItem.write( new JIVariant( tagImpl.newValue.asString() ) );
              tagValue = new JIVariant( tagImpl.newValue.asString() );
              break;
            // $CASES-OMITTED$
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          long startTagWriteTime = System.currentTimeMillis();
          outputGroup.write( new WriteRequest(outputItem,tagValue) );
          long tagWriteTime = System.currentTimeMillis() - startTagWriteTime;
          logger.debug( "Wrote tag %s, value=%s, by time=%d", tag.id(),  tagImpl.newValue.asString(), Long.valueOf( tagWriteTime )  );
          tagImpl.setDirty( false );
        }
      }
    }
    catch( Exception e ) {
      logger.error( e );
    }
  }

  @Override
  public void putInBufferOutputValues() {
    // nop
  }

  @SuppressWarnings( "incomplete-switch" )
  private boolean checkValueType( EAtomicType aDeclaredType, int aFactType ) {
    try {
      switch( aDeclaredType ) {
        case FLOATING:
          return (aFactType == JIVariant.VT_R4) || (aFactType == JIVariant.VT_R8);
        case INTEGER:
          switch( aFactType ) {
            case JIVariant.VT_I1:
            case JIVariant.VT_UI1:
            case JIVariant.VT_I2:
            case JIVariant.VT_UI2:
            case JIVariant.VT_I4:
            case JIVariant.VT_I8:
              return true;
            default:
              return false;
          }
        case BOOLEAN:
          return (aFactType == JIVariant.VT_BOOL);
        case STRING:
          return (aFactType == JIVariant.VT_BSTR);
      }
    }
    catch( Exception e ) {
      logger.error( e );
    }
    return false;
  }

  @SuppressWarnings( "boxing" )
  @Override
  public void getFromBufferInputValues() {
    // перенос входных значений из буфера в S5 OPC теги
    int total = 0;
    int empty = 0;
    int invalid = 0;
    for( ITag tag : readableTags ) {

      JIVariant rawVal = getBufferValue( tag.tagId() );

      // if( tag.id().compareTo( "S71500ET200MP station_1.PLC_40.MV_PP.TKVF" ) == 0 ) {
      // // TODO Обработка неработающих тегов
      // System.out.println( tag.id() );
      // }
      if( rawVal == null ) {
        // TODO
        // logger.error( TAG_READ_FAIL, tag.id() );
        // System.out.println( tag.id() );
        empty++;
        continue;
      }
      try {
        TsIllegalStateRtException.checkFalse( tag instanceof Tag, ERROR_EXPECTED_TAG_TYPE );
        // if( tag.tagId().contains( "SIMATIC 300(1).CPU 314C-2 PtP.TP1.CV" ) ) {
        // logger.debug( "tag Enter: %s, raw type: %s", tag.valueType().name(), String.valueOf( rawVal.getType() ) );
        // }
        if( !checkValueType( tag.valueType(), rawVal.getType() ) ) {
          // TODO
          // logger.error( TAG_VAL_INCORRECT_TYPE, tag.id(), tag.valueType().toString(), rawVal.getType() );
          invalid++;
          continue;
        }
        // // Проверяем значение на валидность
        // try {
        // switch( rawVal.getType() ) {
        // case JIVariant.VT_EMPTY:
        // logger.error( TAG_EMPTY_VAL, tag.id() );
        // continue;
        // case JIVariant.VT_NULL:
        // logger.error( TAG_NULL_VAL, tag.id() );
        // continue;
        // default:
        // break;
        // }
        // }
        // catch( JIException ex ) {
        // logger.error( ex );
        // }
        Tag tagImpl = (Tag)tag;
        // if( tag.tagId().contains( "SIMATIC 300(1).CPU 314C-2 PtP.TP1.CV" ) ) {
        // logger.debug( "tag type: %s, isDirty: %s", tag.valueType().name(), String.valueOf( tagImpl.isDirty() ) );
        // }
        switch( tag.valueType() ) {
          case FLOATING:
            tagImpl.updateVal( avFloat( rawVal.getObjectAsFloat() ) );
            // if( tag.tagId().contains( "SIMATIC 300(1).CPU 314C-2 PtP.TP1.CV" ) ) {
            // logger.debug( "tag updateVal: %s", DvUtils.avFloat( rawVal.getObjectAsFloat() ).asString() );
            // }
            break;
          case INTEGER:
            switch( rawVal.getType() ) {
              case JIVariant.VT_I1:
                tagImpl.updateVal( avInt( rawVal.getObjectAsChar() ) );
                break;
              case JIVariant.VT_UI1:
                tagImpl.updateVal( avInt( rawVal.getObjectAsUnsigned().getValue().byteValue() ) );
                break;
              case JIVariant.VT_I2:
                tagImpl.updateVal( avInt( rawVal.getObjectAsShort() ) );
                break;
              case JIVariant.VT_UI2:
                short shortVal = rawVal.getObjectAsUnsigned().getValue().shortValue();
                tagImpl.updateVal( avInt( Short.toUnsignedInt( shortVal ) ) );
                logger.debug( "tag: %s, read Val: %s", tag.tagId(), Short.valueOf( shortVal ) );
                break;
              case JIVariant.VT_I4:
                tagImpl.updateVal( avInt( rawVal.getObjectAsInt() ) );
                break;
              case JIVariant.VT_I8:
                tagImpl.updateVal( avInt( rawVal.getObjectAsLong() ) );
                break;
              default:
                throw new TsNotAllEnumsUsedRtException( "Unknown integer type: %d", rawVal.getType() ); //$NON-NLS-1$
            }
            break;
          case BOOLEAN:
            tagImpl.updateVal( avBool( rawVal.getObjectAsBoolean() ) );
            break;
          case STRING:
            tagImpl.updateVal( avStr( rawVal.getObjectAsString2() ) );
            break;
          // $CASES-OMITTED$
          default:
            throw new TsNotAllEnumsUsedRtException( UNKNOWN_TAG_VALUE_TYPE, Integer.valueOf( rawVal.getType() ) );
        }
        total++;
      }
      catch( Exception e ) {
        logger.error( e );
      }
    }
    // System.out.println( total + ":" + empty + " : " + invalid ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void closeApparatResources() {
    try {
      // stop async reading
      asyncGroup.unbind();
      server.disconnect();
    }
    catch( Exception ex ) {
      logger.error( ex );
    }
  }

  /**
   * По идентификатору тэга получить его значение считанное с OPC
   *
   * @param aTagId идентификатор тэга
   * @return значение считанное с OPC
   */
  public JIVariant getBufferValue( String aTagId ) {
    return bufferVal.get( aTagId );
  }

  @Override
  public ITag tag( String aTagId ) {
    return tags.findByKey( aTagId );
  }

  @Override
  public IStringMap<ITag> tags() {
    return tags;
  }

  @Override
  public void changed( Item aItem, ItemState aItemState ) {
    bufferVal.put( aItem.getId(), aItemState.getValue() );
    // for debug
    logger.debug( "Asinc tag: %s, changed = %s", aItem.getId(), aItemState.getValue().toString() );
  }
}

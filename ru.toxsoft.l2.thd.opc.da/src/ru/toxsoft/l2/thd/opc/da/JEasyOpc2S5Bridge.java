/**
 *
 */
package ru.toxsoft.l2.thd.opc.da;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;
import static ru.toxsoft.l2.thd.opc.da.IL2Resources.*;

import java.util.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.thd.opc.*;

import javafish.clients.opc.*;
import javafish.clients.opc.asynch.*;
import javafish.clients.opc.component.*;
import javafish.clients.opc.exception.*;
import javafish.clients.opc.variant.*;

/**
 * Реализация моста opc 2 S5 на движке JEasyOpc
 *
 * @author dima
 */
public class JEasyOpc2S5Bridge
    extends AbstractSpecificDevice
    implements OpcAsynchGroupListener, IOpc2S5Bridge {

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
   * @param aId String - строковый идентификатор.
   * @param aDescription String - описание.
   * @param aErrorProcessor {@link IHalErrorProcessor} - обработчик ошибок, возникающих при работе с НУ.
   * @param aCi - информация о соединении
   * @param aSyncTags - описание синхронных тегов
   * @param aUpdatePeriod - период обновления
   * @param aAsyncTags - описание асинхронных тегов
   * @param aOutputTags - описание тегов на запись
   */
  public JEasyOpc2S5Bridge( String aId, String aDescription, IHalErrorProcessor aErrorProcessor,
      IConnectionInformation aCi, IList<OpcTagPinDefinition> aSyncTags, int aUpdatePeriod,
      IList<OpcTagPinDefinition> aAsyncTags, IList<OpcTagPinDefinition> aOutputTags ) {
    super( aId, aDescription, aErrorProcessor );
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
      syncGroup = new OpcGroup( SYNC_GROUP, true, aUpdatePeriod, 0.0f );
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

  @Override
  public void readValuesFromLL()
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

  @Override
  public void writeValuesOnLL()
      throws TsMultipleApparatRtException {
    try {
      for( ITag tag : writeableTags ) {
        TsIllegalStateRtException.checkFalse( tag instanceof Tag, ERROR_EXPECTED_TAG_TYPE );
        Tag tagImpl = (Tag)tag;
        if( tagImpl.isDirty() ) {
          OpcItem outputItem = tagId2OutputIem.get( tagImpl.tagId() );
          switch( tag.valueType() ) {
            case FLOATING:
              outputItem.setValue( new Variant( tagImpl.value.asFloat() ) );
              break;
            case INTEGER:
              outputItem.setValue( new Variant( tagImpl.value.asInt() ) );
              break;
            case BOOLEAN:
              outputItem.setValue( new Variant( tagImpl.value.asBool() ) );
              break;
            case STRING:
              outputItem.setValue( new Variant( tagImpl.value.asString() ) );
              break;
            // $CASES-OMITTED$
            default:
              throw new TsNotAllEnumsUsedRtException();
          }
          // write to opc-server
          opcClient.synchWriteItem( outputGroup, outputItem );
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

  @Override
  public void getFromBufferInputValues() {
    // перенос входных значений из буфера в S5 OPC теги
    for( ITag tag : readableTags ) {
      Variant rawVal = getBufferValue( tag.tagId() );
      try {
        TsIllegalStateRtException.checkFalse( tag instanceof Tag, ERROR_EXPECTED_TAG_TYPE );
        Tag tagImpl = (Tag)tag;
        // if( tagImpl.tagId().compareTo( "S71500ET200MP station_1.PLC_40.Zn.TO" ) == 0 ) { //$NON-NLS-1$
        // System.out.println( "-------------------------------------------" ); //$NON-NLS-1$
        // System.out.println( tagImpl.tagId() + ": " + tagImpl.get() ); //$NON-NLS-1$
        // System.out.println( "-------------------------------------------" ); //$NON-NLS-1$
        // }

        switch( tag.valueType() ) {
          case FLOATING:
            tagImpl.updateVal( avFloat( rawVal.getFloat() ) );
            break;
          case INTEGER:
            switch( rawVal.getVariantType() ) {
              case VariantTypes.VT_UI1:
                tagImpl.updateVal( avInt( rawVal.getByte() ) );
                break;
              case VariantTypes.VT_I2:
                tagImpl.updateVal( avInt( rawVal.getWord() ) );
                break;
              case VariantTypes.VT_INT:
                tagImpl.updateVal( avInt( rawVal.getInteger() ) );
                break;
              default:
                throw new TsNotAllEnumsUsedRtException();
            }
            break;
          case BOOLEAN:
            tagImpl.updateVal( avBool( rawVal.getBoolean() ) );
            break;
          case STRING:
            tagImpl.updateVal( avStr( rawVal.getString() ) );
            break;
          // $CASES-OMITTED$
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
      catch( Exception e ) {
        logger.error( e );
      }
    }
  }

  @Override
  public void closeApparatResources() {
    try {
      for( OpcGroup asyncGroup : asyncGroupMap.values() ) {
        asyncGroup.removeAsynchListener( this );
      }
      opcClient.terminate();
      JCustomOpc.coUninitialize();
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
  public Variant getBufferValue( String aTagId ) {
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

}

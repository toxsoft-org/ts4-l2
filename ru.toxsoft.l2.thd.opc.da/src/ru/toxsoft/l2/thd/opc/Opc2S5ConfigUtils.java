/**
 *
 */
package ru.toxsoft.l2.thd.opc;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import java.io.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;

/**
 * Набор утилит для генерации файлов конфигурации моста Opc2S5
 *
 * @author dima
 */
public class Opc2S5ConfigUtils {

  /**
   * Приватный конструктор, чтобы избежать создания экземпляров
   */
  private Opc2S5ConfigUtils() {
    // nop
  }

  /**
   * Кол-во описаний OPC в конфигурационном файле
   */
  private static final int    OPC_QTTY = 1;
  /**
   * хост OPC сервера
   */
  private static final String HOST     = "localhost";
  /**
   * домен OPC сервера
   */
  public static final String  DOMAIN   = "localhost";                            //$NON-NLS-1$
  /**
   * пользователь
   */
  public static final String  USER     = "opc";                                  //$NON-NLS-1$
  /**
   * пароль
   */
  public static final String  PASSWORD = "1";                                    //$NON-NLS-1$
  /**
   * progId
   */
  public static final String  PROG_ID  = "Graybox.Simulator";                    //$NON-NLS-1$
  /**
   * clsId
   */
  public static final String  CLS_ID   = "2C2E36B7-FE45-4A29-BF89-9BFBA6A40857"; //$NON-NLS-1$

  /**
   * @param a
   */
  public static void main( String[] a ) {
    // Создаем конфигурацию для станции
    try {
      createConfig( "vj" );
      System.out.println( "Template OPC config file generation complete." ); //$NON-NLS-1$
    }
    catch( IOException e ) {
      e.printStackTrace();
    }
  }

  // Набор функция для создания тестовой конфигурации
  /**
   * Создает описание одного пина
   *
   * @param aTag тэг OPC
   * @param aPinId id пина
   * @param aPinType тип значения пина
   * @return
   */
  private static IAvTree createPin( String aTag, String aPinId, String aPinType ) {

    IOptionSetEdit pinOpSet = new OptionSet();
    pinOpSet.setStr( OPC_TAG_PARAM_NAME, aTag );
    pinOpSet.setStr( PIN_ID_PARAM_NAME, aPinId );
    pinOpSet.setStr( PIN_TYPE_PARAM_NAME, aPinType );

    @SuppressWarnings( "nls" )
    IAvTree pinTree = AvTree.createSingleAvTree( "def." + aPinId, pinOpSet, IStringMap.EMPTY );
    return pinTree;
  }

  /**
   * Функция создания параметров одного моста OPC
   *
   * @return конфигурация одного моста OPC
   */
  private static IAvTree createBridge( String aOpcId ) {

    IListEdit<IAvTree> groups = new ElemArrayList<>();
    // Создаем описание для группы входных синхронных тегов
    IAvTree synDataTags = createSyncDataTags( aOpcId );
    groups.add( synDataTags );
    // Создаем описание для группы входных aсинхронных тегов
    IAvTree asynDataTags = createAsyncDataTags( aOpcId );
    groups.add( asynDataTags );
    // Создаем описание для группы вЫходных тегов
    IAvTree outputTags = createOutputTags( aOpcId );
    groups.add( outputTags );

    // массив груп
    AvTree groupsArrayTree = AvTree.createArrayAvTree();

    for( IAvTree group : groups ) {
      groupsArrayTree.addElement( group );
    }
    IOptionSetEdit opcOpSet = new OptionSet();

    opcOpSet.setStr( HOST_PARAM_NAME, DEFAULT_HOST_VAL );
    opcOpSet.setStr( DOMAIN_PARAM_NAME, DEFAULT_DOMAIN_VAL );
    opcOpSet.setStr( USER_PARAM_NAME, DEFAULT_USER_VAL );
    opcOpSet.setStr( PASSWORD_PARAM_NAME, DEFAULT_PASSWORD_VAL );
    opcOpSet.setStr( PROGID_PARAM_NAME, DEFAULT_PROGID_VAL );
    opcOpSet.setStr( CLSID_PARAM_NAME, DEFAULT_CLSID_VAL );

    StringMap<IAvTree> groupNodes = new StringMap<>();
    groupNodes.put( GROUPS_PARAM_NAME, groupsArrayTree );

    @SuppressWarnings( "nls" )
    IAvTree busTree = AvTree.createSingleAvTree( aOpcId + ".opc" + ".def", opcOpSet, groupNodes );
    return busTree;
  }

  /**
   * Создает описание всех выходных пинов OPC сервера
   *
   * @param aOpcId id opc сервера
   * @return дерево выходных пинов
   */
  private static IAvTree createOutputTags( String aOpcId ) {
    IListEdit<IAvTree> pins = createOutputPins( aOpcId );
    // массив пинов
    AvTree pinsArrayTree = AvTree.createArrayAvTree();

    for( IAvTree pin : pins ) {
      pinsArrayTree.addElement( pin );
    }
    IOptionSetEdit outputGroupOpSet = new OptionSet();

    StringMap<IAvTree> pinsNodes = new StringMap<>();
    pinsNodes.put( OUTPUT_TAGS_PARAM_NAME, pinsArrayTree );

    @SuppressWarnings( "nls" )
    IAvTree syncGroupTree = AvTree.createSingleAvTree( aOpcId + ".output.group.def", outputGroupOpSet, pinsNodes );
    return syncGroupTree;
  }

  /**
   * Создает описание всех выходных пинов OPC сервера
   *
   * @param aOpcId id opc сервера
   * @return дерево выходных пинов
   */
  private static IListEdit<IAvTree> createOutputPins( String aOpcId ) {
    ElemArrayList<IAvTree> result = new ElemArrayList<>();
    // pin типа Boolean
    IAvTree pinDef = createPin( "opc.bool.tag", aOpcId + ".opc.output.group.pin.id.xxx", EAtomicType.BOOLEAN.id() );
    result.add( pinDef );
    // pin типа int
    pinDef = createPin( "opc.int.tag", aOpcId + ".opc.output.group.pin.id.xxx", EAtomicType.INTEGER.id() );
    result.add( pinDef );
    // pin типа float
    pinDef = createPin( "opc.float.tag", aOpcId + ".opc.output.group.pin.id.xxx", EAtomicType.FLOATING.id() );
    result.add( pinDef );
    return result;
  }

  /**
   * Создает описание всех асинхронных пинов OPC сервера
   *
   * @param aOpcId id opc сервера
   * @return дерево асинхронных пинов
   */
  private static IListEdit<IAvTree> createAsyncPins( String aOpcId ) {
    ElemArrayList<IAvTree> result = new ElemArrayList<>();
    // pin типа Boolean
    IAvTree pinDef = createPin( "opc.bool.tag", aOpcId + ".opc.async.group.pin.id.xxx", EAtomicType.BOOLEAN.id() );
    result.add( pinDef );
    // pin типа int
    pinDef = createPin( "opc.int.tag", aOpcId + ".opc.async.group.pin.id.xxx", EAtomicType.INTEGER.id() );
    result.add( pinDef );
    // pin типа float
    pinDef = createPin( "opc.float.tag", aOpcId + ".opc.async.group.pin.id.xxx", EAtomicType.FLOATING.id() );
    result.add( pinDef );
    return result;
  }

  /**
   * Создает описание всех синхронных пинов OPC сервера
   *
   * @param aOpcId id opc сервера
   * @return дерево синхронных пинов
   */
  private static IListEdit<IAvTree> createSyncPins( String aOpcId ) {
    ElemArrayList<IAvTree> result = new ElemArrayList<>();
    // pin типа Boolean
    IAvTree pinDef = createPin( "opc.bool.tag", aOpcId + ".opc.sync.group.pin.id.xxx", EAtomicType.BOOLEAN.id() );
    result.add( pinDef );
    // pin типа int
    pinDef = createPin( "opc.int.tag", aOpcId + ".opc.sync.group.pin.id.xxx", EAtomicType.INTEGER.id() );
    result.add( pinDef );
    // pin типа float
    pinDef = createPin( "opc.float.tag", aOpcId + ".opc.sync.group.pin.id.xxx", EAtomicType.FLOATING.id() );
    result.add( pinDef );
    return result;
  }

  /**
   * Создает подраздел описывающий все синхронные теги
   *
   * @return дерево конфигурации синхронных тегов
   */
  private static IAvTree createSyncDataTags( String aOpcId ) {
    IListEdit<IAvTree> pins = createSyncPins( aOpcId );
    // массив пинов
    AvTree pinsArrayTree = AvTree.createArrayAvTree();

    for( IAvTree pin : pins ) {
      pinsArrayTree.addElement( pin );
    }
    IOptionSetEdit syncGroupOpSet = new OptionSet();

    syncGroupOpSet.setInt( PERIOD_PARAM_NAME, 500 );

    StringMap<IAvTree> pinsNodes = new StringMap<>();
    pinsNodes.put( SYNC_TAGS_PARAM_NAME, pinsArrayTree );

    @SuppressWarnings( "nls" )
    IAvTree syncGroupTree = AvTree.createSingleAvTree( aOpcId + ".sync.group.def", syncGroupOpSet, pinsNodes );
    return syncGroupTree;
  }

  /**
   * Создает подраздел описывающий все асинхронные теги
   *
   * @return дерево конфигурации асинхронных тегов
   */
  private static IAvTree createAsyncDataTags( String aOpcId ) {
    IListEdit<IAvTree> pins = createAsyncPins( aOpcId );
    // массив пинов
    AvTree pinsArrayTree = AvTree.createArrayAvTree();

    for( IAvTree pin : pins ) {
      pinsArrayTree.addElement( pin );
    }
    IOptionSetEdit asyncGroupOpSet = new OptionSet();

    StringMap<IAvTree> pinsNodes = new StringMap<>();
    pinsNodes.put( ASYNC_TAGS_PARAM_NAME, pinsArrayTree );

    @SuppressWarnings( "nls" )
    IAvTree syncGroupTree = AvTree.createSingleAvTree( aOpcId + ".async.group.def", asyncGroupOpSet, pinsNodes );
    return syncGroupTree;
  }

  /**
   * Функция создания массива мостов OPC
   *
   * @return массив описаний мостов OPC
   */
  static IListEdit<IAvTree> createBridges() {

    ElemArrayList<IAvTree> result = new ElemArrayList<>();
    for( int i = 1; i < OPC_QTTY + 1; i++ ) {
      IAvTree bridgeDef = createBridge( "siemens" );
      result.add( bridgeDef );
    }
    return result;
  }

  /**
   * Функция создания целиком конфигурационного файла
   *
   * @param aProjectShortName короткое название проекта, например tm
   * @throws IOException
   */
  private static void createConfig( String aProjectShortName )
      throws IOException {
    String destConfigFile = aProjectShortName + CONFIG_FILE_POSTFIX;
    String tmpDestFile = "destFile.tmp";

    File f = new File( tmpDestFile );
    // FileWriter fw = new FileWriter( f );
    // IStrioWriter writer = new StrioWriter( new CharOutputStreamAppendable( fw ) );

    // IDvWriter dr = new DvWriter( writer );

    IListEdit<IAvTree> bridges = createBridges();
    // массив мостов opc2S5
    AvTree bridgesArrayTree = AvTree.createArrayAvTree();

    for( IAvTree bridge : bridges ) {
      bridgesArrayTree.addElement( bridge );
    }
    IOptionSetEdit configOpSet = new OptionSet();

    configOpSet.setStr( JAVA_CLASS_NAME, OPC_2_S5_COLLECTION_PRODUCER );
    configOpSet.setStr( ID, ID_VAL_PREFIX + aProjectShortName );
    configOpSet.setStr( DESCRIPTION, DESCRIPTION_VAL );

    StringMap<IAvTree> opcNodes = new StringMap<>();
    opcNodes.put( BRIDGES_PARAM_NAME, bridgesArrayTree );

    @SuppressWarnings( "nls" )
    IAvTree congigTree = AvTree.createSingleAvTree( "opc2s5.cfg", configOpSet, opcNodes );
    AvTreeKeeper.KEEPER.write( f, congigTree );

    // fw.close();

    // PinsConfigFileFormatter.format( tmpDestFile, destConfigFile, "DeviceConfig = " );

  }

}

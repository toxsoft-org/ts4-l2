package ru.toxsoft.l2.utils.opc.cfg.exe.gwp;

import static ru.toxsoft.l2.utils.opc.cfg.exe.ISkResources.*;
import static ru.toxsoft.l2.utils.opc.cfg.exe.ISysClassRowDef.*;

import java.io.*;
import java.util.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.*;
import ru.toxsoft.l2.utils.opc.cfg.exe.OdsFileReader.*;

public class GwpOpcUaOdsToOpcThdConvertor {

  private static final ILogger logger = LoggerUtils.errorLogger();

  private static final String OPC_TAG_DEVICE_UA = "opc2s5.bridge.collection.id";

  private static final String DESCRIPTION_PARAM_NAME         = "description";
  private static final String DESCRIPTION_PARAM_VAL_TEMPLATE = "opc 2 s5 pins UA apparat producer";

  private static final String ID_PARAM_NAME = "id";

  private static final String JAVA_CLASS_PARAM_NAME         = "javaClassName";
  private static final String JAVA_CLASS_PARAM_VAL_TEMPLATE = "org.toxsoft.l2.thd.opc.ua.milo.OpcUaMiloDriverProducer";

  private static final String HOST_PARAM_NAME         = "host";
  private static final String HOST_PARAM_VAL_TEMPLATE = "opc.tcp://192.168.0.???:4840";

  private static final String USER_PARAM_NAME         = "user";
  private static final String USER_PARAM_VAL_TEMPLATE = "";

  private static final String PASSWORD_PARAM_NAME         = "password";
  private static final String PASSWORD_PARAM_VAL_TEMPLATE = "";

  // public static final String OPC_TAG_DEVICE = "opc2s5.vj";
  public static String OPC_TAG_DEVICE = OPC_TAG_DEVICE_UA;

  private static final String ID_PARAM_VAL_TEMPLATE = OPC_TAG_DEVICE;

  private static final String SIEMENS_BRIDGE_NODE_ID = "device1.opc.def";

  private static final String OPC2S5_CFG_NODE_ID = "opc2s5.cfg";

  private static final String BRIDGE_TYPE_PARAM_NAME         = "bridge.type";
  private static final String BRIDGE_TYPE_PARAM_VAL_TEMPLATE = "OPENSCADA";

  private static final String PERIOD_PARAM_NAME         = "period";
  private static final int    PERIOD_PARAM_VAL_TEMPLATE = 500;

  private static final String DOMAIN_PARAM_NAME         = "domain";
  private static final String DOMAIN_PARAM_VAL_TEMPLATE = "localhost";

  private static final String PROG_ID_PARAM_NAME         = "progId";
  private static final String PROG_ID_PARAM_VAL_TEMPLATE = "OPC.SimaticNET";

  private static final String CLS_ID_PARAM_NAME         = "clsId";
  private static final String CLS_ID_PARAM_VAL_TEMPLATE = "B6EACB30-42D5-11d0-9517-0020AFAA4B3C";

  private static final String GROUPS_ARRAY_NAME           = "groups";
  private static final String BRIDGES_ARRAY_NAME          = "bridges";
  private static final String SYNCH_PERIOD_PARAM_NAME     = "period";
  private static final String OUTPUT_TAGS_ARRAY_ID        = "output.tags";
  private static final String ASYNC_TAGS_ARRAY_ID         = "async.tags";
  private static final String SYNC_TAGS_ARRAY_ID          = "sync.tags";
  private static final String OUTPUT_GROUP_NODE_ID        = "siemens.output.group.def";
  private static final String ASYNC_GROUP_NODE_ID         = "siemens.async.group.def";
  private static final String SYNC_GROUP_NODE_ID          = "siemens.sync.group.def";
  private static final String TMP_DEST_FILE               = "thdDestFile.tmp";
  private static final String DEVICE_CONFIG_STR           = "DeviceConfig = ";
  private static final String OPC_TAG_PARAM_NAME          = "opc.tag";
  private static final String PIN_ID_PARAM_NAME           = "pin.id";
  private static final String PIN_TYPE_PARAM_NAME         = "pin.type";
  private static final String PIN_TYPE_EXTRA_PARAM_NAME   = "pin.type.extra";
  private static final String PIN_CONTROL_WORD_PARAM_NAME = "is.pin.control.word";
  private static final String PIN_TAG_NODE_ID_FORMAT      = "pin.tag.%s.def";

  /**
   * Запускает программу.
   *
   * @param args - 0 параметр - исходный ODS-файл, 1 параметр - целевой devcfg-файл.
   */
  public static void main( String[] args ) {
    String srcFileName = args[0];

    String dstFileName = args[1];

    generate( srcFileName, dstFileName );
  }

  /**
   * Генерация файла конфигурации из файла описания сигналов
   *
   * @param aSourceOdsFile - исходный Ods файл описания
   * @param aTargetThdFile - целевой файл *.devcfg
   * @return результат выполнения операции { @link ValidationResult }
   */
  public static ValidationResult generate( String aSourceOdsFile, String aTargetThdFile ) {

    String classSheetName = "Классы";
    GwpTagsOdsReader reader = new GwpTagsOdsReader( new File( aSourceOdsFile ), classSheetName );

    List<IOptionSet> result;
    try {
      reader.read();

      result = reader.getAllRows();
    }
    catch( IOException e ) {
      logger.error( e.getMessage() );
      return ValidationResult.error( FMT_ERR_READ_SHEET, aSourceOdsFile );
    }

    IOptionSetEdit opSet = new OptionSet();

    opSet.setStr( JAVA_CLASS_PARAM_NAME, JAVA_CLASS_PARAM_VAL_TEMPLATE );
    opSet.setStr( ID_PARAM_NAME, OPC_TAG_DEVICE_UA );
    opSet.setStr( DESCRIPTION_PARAM_NAME, DESCRIPTION_PARAM_VAL_TEMPLATE );

    IOptionSetEdit bridgeOps = new OptionSet();

    bridgeOps.setStr( ID_PARAM_NAME, OPC_TAG_DEVICE_UA );
    bridgeOps.setStr( DESCRIPTION_PARAM_NAME, DESCRIPTION_PARAM_VAL_TEMPLATE );
    bridgeOps.setStr( HOST_PARAM_NAME, HOST_PARAM_VAL_TEMPLATE );
    bridgeOps.setStr( USER_PARAM_NAME, USER_PARAM_VAL_TEMPLATE );
    bridgeOps.setStr( PASSWORD_PARAM_NAME, PASSWORD_PARAM_VAL_TEMPLATE );

    try {
      formThdFile( aTargetThdFile, result, opSet, bridgeOps );
    }
    catch( IOException e ) {
      logger.error( e.getMessage() );
      return ValidationResult.error( FMT_ERR_FORM_THD_FILE, aTargetThdFile );
    }
    return ValidationResult.SUCCESS;
  }

  public static void formThdFile( String aDestFile, List<IOptionSet> aResult, IOptionSet aCommonOptions,
      IOptionSet aBridgeOptions )
      throws IOException {
    AvTree synchGroup = createGroup( aResult, SYNC_TAGS_ARRAY_ID, SYNC_GROUP_NODE_ID );

    synchGroup.fieldsEdit().setInt( SYNCH_PERIOD_PARAM_NAME, 500 );

    IAvTree asynchGroup = createGroup( new ArrayList<IOptionSet>(), ASYNC_TAGS_ARRAY_ID, ASYNC_GROUP_NODE_ID );

    IAvTree outputGroup = createGroup( new ArrayList<IOptionSet>(), OUTPUT_TAGS_ARRAY_ID, OUTPUT_GROUP_NODE_ID );

    // массив групп
    AvTree groupsMassivTree = AvTree.createArrayAvTree();

    groupsMassivTree.addElement( synchGroup );
    groupsMassivTree.addElement( asynchGroup );
    groupsMassivTree.addElement( outputGroup );

    // массив групп
    AvTree bridgesMassivTree = AvTree.createArrayAvTree();

    StringMap<IAvTree> groupsNodes = new StringMap<>();
    groupsNodes.put( GROUPS_ARRAY_NAME, groupsMassivTree );

    IAvTree siemensBridge = AvTree.createSingleAvTree( SIEMENS_BRIDGE_NODE_ID, aBridgeOptions, groupsNodes );
    bridgesMassivTree.addElement( siemensBridge );

    StringMap<IAvTree> nodes = new StringMap<>();
    nodes.put( BRIDGES_ARRAY_NAME, bridgesMassivTree );

    IAvTree tree = AvTree.createSingleAvTree( OPC2S5_CFG_NODE_ID, aCommonOptions, nodes );

    String tmpDestFile = TMP_DEST_FILE;

    saveDstTree( tmpDestFile, tree );

    PinsConfigFileFormatter.format( tmpDestFile, aDestFile, DEVICE_CONFIG_STR );
    // Dima, 06.07.16
    // Подчистим за собой
    try {
      File file = new File( tmpDestFile );
      if( file.delete() ) {
        System.out.println( file.getName() + " is deleted!" );
      }

      else {
        System.out.println( "Delete operation is failed." );
      }
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
    System.out.println( "Operation completed." );
  }

  static void saveDstTree( String aDstFile, IAvTree aTree )
      throws IOException {

    File f = new File( aDstFile );
    // FileWriter fw = new FileWriter( f );
    // IStridWriter writer = new StridWriter( new CharOutputStreamAppendable( fw ) );

    // IDvWriter dr = new DvWriter( writer );

    AvTreeKeeper.KEEPER.write( f, aTree );

    // fw.close();
  }

  private static AvTree createGroup( List<IOptionSet> aStringDatas, String aArrayId, String aGroupNodeId ) {

    // массив тегов группы
    AvTree tagsMassivTree = AvTree.createArrayAvTree();

    for( IOptionSet tagData : aStringDatas ) {

      IAvTree tag = createTag( tagData );

      tagsMassivTree.addElement( tag );
    }

    StringMap<IAvTree> nodes = new StringMap<>();
    nodes.put( aArrayId, tagsMassivTree );

    IOptionSetEdit pinOpSet1 = new OptionSet();

    AvTree groupTree = AvTree.createSingleAvTree( aGroupNodeId, pinOpSet1, nodes );
    return groupTree;
  }

  @SuppressWarnings( "unchecked" )
  private static IAvTree createTag( IOptionSet aData ) {

    IOptionSetEdit pinOpSet1 = new OptionSet();
    // Dima, 29.12.15
    // Здесь д.б. полное имя тега
    pinOpSet1.setStr( OPC_TAG_PARAM_NAME, TAG_PATH_PARAM.getValue( aData ).asString() );
    pinOpSet1.setStr( PIN_ID_PARAM_NAME, getPinId( TAG_PATH_PARAM.getValue( aData ).asString() ) );
    pinOpSet1.setStr( PIN_TYPE_PARAM_NAME, VAL_TYPE_PARAM.getValue( aData ).asString() );
    pinOpSet1.setStr( PIN_TYPE_EXTRA_PARAM_NAME, VAL_RAW_TYPE_PARAM.getValue( aData ).asString() );

    IAvTree pinTree1 = null;
    try {
      pinTree1 = AvTree.createSingleAvTree(
          String.format( PIN_TAG_NODE_ID_FORMAT, getPinId( TAG_PATH_PARAM.getValue( aData ).asString() ) ), pinOpSet1,
          IStringMap.EMPTY );
    }
    catch( TsValidationFailedRtException e ) {
      System.out.println( TAG_NAME_PARAM.getValue( aData ).asString() );
      throw e;
    }
    return pinTree1;
  }

  interface IGroupFilter {

    boolean isValid( StringData aStringData );
  }

  private static int searchByFiledValue( IAvTree aParentMassiv, String aKey, String aValue ) {
    for( int i = 0; i < aParentMassiv.arrayLength(); i++ ) {
      IAvTree currElem = aParentMassiv.arrayElement( i );

      if( currElem.fields().findValue( aKey ).equals( AvUtils.avStr( aValue ) ) ) {
        return i;
      }
    }

    return -1;
  }

  protected static String getPinId( String aTagName ) {
    String result = aTagName;
    result = result.replace( " ", "_" );
    result = result.replace( "-", "_" );
    result = result.replace( ".", "_" );
    result = result.replace( "(", "_" );
    result = result.replace( ")", "_" );
    result = result.replace( "=", "_" );
    result = result.replace( ";", "_" );
    result = result.replace( "\"", "" );
    return result;
  }
}

package ru.toxsoft.l2.utils.opc.cfg.exe;

import java.io.*;
import java.util.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.OdsFileReader.*;

/**
 * Конвертор ODS-файла с описанием OPC в файл конфигурации драйвера OPC-моста.
 *
 * @author max
 */
public class OpcOdsToOpcThdConvertor {

  // public static final String OPC_TAG_DEVICE = "opc2s5.vj";
  public static final String OPC_TAG_DEVICE = "opc2s5.bridge.collection.id";

  private static final String DESCRIPTION_PARAM_NAME         = "description";
  private static final String DESCRIPTION_PARAM_VAL_TEMPLATE = "opc 2 s5 pins apparat producer";

  private static final String ID_PARAM_NAME         = "id";
  private static final String ID_PARAM_VAL_TEMPLATE = OPC_TAG_DEVICE;

  private static final String JAVA_CLASS_PARAM_NAME         = "javaClassName";
  private static final String JAVA_CLASS_PARAM_VAL_TEMPLATE = "ru.toxsoft.l2.thd.opc.da.Opc2S5CollectionProducer";

  private static final String SIEMENS_BRIDGE_NODE_ID = "siemens.opc.def";

  private static final String OPC2S5_CFG_NODE_ID = "opc2s5.cfg";

  private static final String BRIDGE_TYPE_PARAM_NAME         = "bridge.type";
  private static final String BRIDGE_TYPE_PARAM_VAL_TEMPLATE = "OPENSCADA";

  private static final String PERIOD_PARAM_NAME         = "period";
  private static final int    PERIOD_PARAM_VAL_TEMPLATE = 500;

  private static final String HOST_PARAM_NAME         = "host";
  private static final String HOST_PARAM_VAL_TEMPLATE = "localhost";

  private static final String DOMAIN_PARAM_NAME         = "domain";
  private static final String DOMAIN_PARAM_VAL_TEMPLATE = "localhost";

  private static final String USER_PARAM_NAME         = "user";
  private static final String USER_PARAM_VAL_TEMPLATE = "opc";

  private static final String PASSWORD_PARAM_NAME         = "password";
  private static final String PASSWORD_PARAM_VAL_TEMPLATE = "010101";

  private static final String PROG_ID_PARAM_NAME         = "progId";
  private static final String PROG_ID_PARAM_VAL_TEMPLATE = "OPC.SimaticNET";

  private static final String CLS_ID_PARAM_NAME         = "clsId";
  private static final String CLS_ID_PARAM_VAL_TEMPLATE = "B6EACB30-42D5-11d0-9517-0020AFAA4B3C";

  private static final String GROUPS_ARRAY_NAME       = "groups";
  private static final String BRIDGES_ARRAY_NAME      = "bridges";
  private static final String SYNCH_PERIOD_PARAM_NAME = "period";
  private static final String OUTPUT_TAGS_ARRAY_ID    = "output.tags";
  private static final String ASYNC_TAGS_ARRAY_ID     = "async.tags";
  private static final String SYNC_TAGS_ARRAY_ID      = "sync.tags";
  private static final String OUTPUT_GROUP_NODE_ID    = "siemens.output.group.def";
  private static final String ASYNC_GROUP_NODE_ID     = "siemens.async.group.def";
  private static final String SYNC_GROUP_NODE_ID      = "siemens.sync.group.def";
  private static final String TMP_DEST_FILE           = "thdDestFile.tmp";
  private static final String DEVICE_CONFIG_STR       = "DeviceConfig = ";
  private static final String OPC_TAG_PARAM_NAME      = "opc.tag";
  private static final String PIN_ID_PARAM_NAME       = "pin.id";
  private static final String PIN_TYPE_PARAM_NAME     = "pin.type";
  private static final String PIN_TAG_NODE_ID_FORMAT  = "pin.tag.%s.def";

  /**
   * Запускает программу.
   *
   * @param args - 0 параметр - исходный ODS-файл, 1 параметр - целевой devcfg-файл.
   */
  public static void main( String[] args ) {
    String srcFileName = args[0];

    String dstFileName = args[1];

    IListEdit<StringData> result;
    try {
      result = TwoTabsOdsFileReader.readSheet( srcFileName );
      System.out.println( result.size() );
    }
    catch( IOException e ) {
      e.printStackTrace();
      return;
    }

    try {
      formThdFile( dstFileName, result );
    }
    catch( IOException e ) {
      e.printStackTrace();
    }
  }

  private static void formThdFile( String aDestFile, IListEdit<StringData> aResult )
      throws IOException {
    AvTree synchGroup = createGroup( aResult, aStringData -> {
      if( aStringData.getTagFullName() == null || aStringData.getTagFullName().trim().length() == 0 ) {
        return false;
      }
      return aStringData.getTagSynchType() == ETagSynchType.SYNCH;
    }, SYNC_TAGS_ARRAY_ID, SYNC_GROUP_NODE_ID );

    synchGroup.fieldsEdit().setInt( SYNCH_PERIOD_PARAM_NAME, 500 );

    IAvTree asynchGroup = createGroup( aResult, aStringData -> {
      if( aStringData.getTagFullName() == null || aStringData.getTagFullName().trim().length() == 0 ) {
        return false;
      }
      return aStringData.getTagSynchType() == ETagSynchType.ASYNCH;
    }, ASYNC_TAGS_ARRAY_ID, ASYNC_GROUP_NODE_ID );

    IAvTree outputGroup = createGroup( aResult, aStringData -> {
      if( aStringData.getTagFullName() == null || aStringData.getTagFullName().trim().length() == 0 ) {
        return false;
      }
      return aStringData.isOutput();
    }, OUTPUT_TAGS_ARRAY_ID, OUTPUT_GROUP_NODE_ID );

    // массив групп
    AvTree groupsMassivTree = AvTree.createArrayAvTree();

    groupsMassivTree.addElement( synchGroup );
    groupsMassivTree.addElement( asynchGroup );
    groupsMassivTree.addElement( outputGroup );

    // массив групп
    AvTree bridgesMassivTree = AvTree.createArrayAvTree();

    IOptionSetEdit siemensBridgeOps = new OptionSet();
    // Dima, 05.07.16
    // Добавляем новые настройки
    siemensBridgeOps.setStr( BRIDGE_TYPE_PARAM_NAME, BRIDGE_TYPE_PARAM_VAL_TEMPLATE );
    siemensBridgeOps.setInt( PERIOD_PARAM_NAME, PERIOD_PARAM_VAL_TEMPLATE );
    siemensBridgeOps.setStr( HOST_PARAM_NAME, HOST_PARAM_VAL_TEMPLATE );
    siemensBridgeOps.setStr( DOMAIN_PARAM_NAME, DOMAIN_PARAM_VAL_TEMPLATE );
    siemensBridgeOps.setStr( USER_PARAM_NAME, USER_PARAM_VAL_TEMPLATE );
    siemensBridgeOps.setStr( PASSWORD_PARAM_NAME, PASSWORD_PARAM_VAL_TEMPLATE );
    siemensBridgeOps.setStr( PROG_ID_PARAM_NAME, PROG_ID_PARAM_VAL_TEMPLATE );
    siemensBridgeOps.setStr( CLS_ID_PARAM_NAME, CLS_ID_PARAM_VAL_TEMPLATE );

    StringMap<IAvTree> groupsNodes = new StringMap<>();
    groupsNodes.put( GROUPS_ARRAY_NAME, groupsMassivTree );

    IAvTree siemensBridge = AvTree.createSingleAvTree( SIEMENS_BRIDGE_NODE_ID, siemensBridgeOps, groupsNodes );
    bridgesMassivTree.addElement( siemensBridge );
    IOptionSetEdit opSet = new OptionSet();

    opSet.setStr( JAVA_CLASS_PARAM_NAME, JAVA_CLASS_PARAM_VAL_TEMPLATE );
    opSet.setStr( ID_PARAM_NAME, ID_PARAM_VAL_TEMPLATE );
    opSet.setStr( DESCRIPTION_PARAM_NAME, DESCRIPTION_PARAM_VAL_TEMPLATE );

    StringMap<IAvTree> nodes = new StringMap<>();
    nodes.put( BRIDGES_ARRAY_NAME, bridgesMassivTree );

    IAvTree tree = AvTree.createSingleAvTree( OPC2S5_CFG_NODE_ID, opSet, nodes );

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

  private static AvTree createGroup( IListEdit<StringData> aStringDatas, IGroupFilter aGroupFilter, String aArrayId,
      String aGroupNodeId ) {

    // массив тегов группы
    AvTree tagsMassivTree = AvTree.createArrayAvTree();

    Set<String> alreadyAddedTags = new HashSet<>();

    for( StringData tagData : aStringDatas ) {
      if( !aGroupFilter.isValid( tagData ) ) {
        continue;
      }
      // for debug
      System.out.print( tagData.getTagFullName() );
      if( alreadyAddedTags.contains( tagData.getTagFullName() ) ) {
        System.out.println();
        continue;
      }
      System.out.println( " - *" );
      alreadyAddedTags.add( tagData.getTagFullName() );

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
  private static IAvTree createTag( StringData aData ) {

    IOptionSetEdit pinOpSet1 = new OptionSet();
    // Dima, 29.12.15
    // Здесь д.б. полное имя тега
    pinOpSet1.setStr( OPC_TAG_PARAM_NAME, aData.getTagFullName() );
    // pinOpSet1.setStr( OPC_TAG_PARAM_NAME, aData.getTagName() );
    pinOpSet1.setStr( PIN_ID_PARAM_NAME, aData.getPinId() );
    pinOpSet1.setStr( PIN_TYPE_PARAM_NAME, aData.getTagValueType().getName() );

    IAvTree pinTree1 = null;
    try {
      pinTree1 = AvTree.createSingleAvTree( String.format( PIN_TAG_NODE_ID_FORMAT, aData.getPinId() ), pinOpSet1,
          IStringMap.EMPTY );
    }
    catch( TsValidationFailedRtException e ) {
      System.out.println( aData.getTagFullName().trim() );
      throw e;
    }
    return pinTree1;
  }

  interface IGroupFilter {

    boolean isValid( StringData aStringData );
  }

}

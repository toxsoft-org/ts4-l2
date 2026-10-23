package org.toxsoft.l2.thd.s7.utils;

import java.io.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.thd.s7.gui.*;
import org.toxsoft.skf.bridge.cfg.opcua.gui.utils.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация генератора содержимого файла s7.devcfg.
 *
 * @author dima
 */
public class S7DevCfgGenerator
    implements IS7DevCfgGenerator {

  /**
   * все S7 устройства
   */
  protected IListEdit<GuiS7Device> s7Devices = new ElemArrayList<>();

  public void addS7Device( GuiS7Device aDevice ) {
    s7Devices.add( aDevice );
  }
  // protected ITsGuiContext context;

  private static IAvTree createS7TagTree( GuiS7Tag aGuiS7Tag ) {
    IOptionSetEdit tagOpSet = new OptionSet();
    tagOpSet.setStr( TAG_ID, aGuiS7Tag.getId() );
    tagOpSet.setInt( TAG_BUFFER_OFFSET, aGuiS7Tag.bufferOffset );
    tagOpSet.setStr( TAG_KIND_ID, aGuiS7Tag.tagKind.getName() );
    tagOpSet.setStr( TAG_VALUE_TYPE_ID, aGuiS7Tag.valueType.id() );
    if( aGuiS7Tag.valueType.equals( EAtomicType.BOOLEAN ) ) {
      tagOpSet.setInt( BIT_NUMBER, aGuiS7Tag.bitNumber );
    }

    IAvTree tagTree =
        AvTree.createSingleAvTree( String.format( TAG_DEF_FORMAT, aGuiS7Tag.getId() ), tagOpSet, IStringMap.EMPTY );
    return tagTree;
  }

  private static IAvTree createS7DataBlockTree( GuiS7DataBlock aGuiS7DataBlock ) {
    IOptionSetEdit dbOpSet = new OptionSet();
    dbOpSet.setStr( DB_ID, aGuiS7DataBlock.getId() );
    dbOpSet.setInt( DB_NUM, aGuiS7DataBlock.getDBNumber() );

    // массив тегов
    AvTree tagArrayTree = AvTree.createArrayAvTree();
    for( GuiS7Tag tag : aGuiS7DataBlock.getTags() ) {
      IAvTree s7TagTree = createS7TagTree( tag );
      tagArrayTree.addElement( s7TagTree );
    }

    StringMap<IAvTree> tags = new StringMap<>();
    tags.put( TAGS_ARRAY_NAME, tagArrayTree );

    IAvTree dbTree =
        AvTree.createSingleAvTree( String.format( DB_DEF_FORMAT, aGuiS7DataBlock.getId() ), dbOpSet, tags );
    return dbTree;
  }

  private static AvTree createS7DeviceTree( GuiS7Device aDevice ) {

    IOptionSetEdit deviceOpSet = new OptionSet();
    deviceOpSet.setStr( DEVICE_ID, aDevice.id() );
    deviceOpSet.setStr( DEVICE_IP_ADDRESS,
        S7DeviceOptionsUtils.OP_TCP_IP_ADDRESS.getValue( aDevice.getDeviceOptValues() ).asString() );
    deviceOpSet.setInt( DEVICE_RACK, S7DeviceOptionsUtils.OP_RACK.getValue( aDevice.getDeviceOptValues() ).asInt() );
    deviceOpSet.setInt( DEVICE_SLOT, S7DeviceOptionsUtils.OP_SLOT.getValue( aDevice.getDeviceOptValues() ).asInt() );

    // массив блоков данных
    AvTree dbArrayTree = AvTree.createArrayAvTree();

    for( GuiS7DataBlock dataBlock : aDevice.getDataBlocks() ) {
      IAvTree db = createS7DataBlockTree( dataBlock );
      dbArrayTree.addElement( db );
    }

    StringMap<IAvTree> dataBlocks = new StringMap<>();
    dataBlocks.put( DATABLOCKS_ARRAY_NAME, dbArrayTree );

    AvTree deviceTree = AvTree.createSingleAvTree( aDevice.id(), deviceOpSet, dataBlocks );
    return deviceTree;
  }

  @Override
  public IAvTree generate() {

    // массив S7 уcтройств
    AvTree devicesArrayTree = AvTree.createArrayAvTree();

    for( GuiS7Device device : s7Devices ) {
      IAvTree db = createS7DeviceTree( device );
      devicesArrayTree.addElement( db );
    }

    // создаем самое верхнее описание, раздел драйвера целиком
    StringMap<IAvTree> devices = new StringMap<>();
    devices.put( S7_DEVICES_ARRAY_NAME, devicesArrayTree );

    IOptionSetEdit opSet = new OptionSet();
    // тут настройки самого драйвера
    opSet.setStr( JAVA_CLASS_PARAM_NAME, JAVA_CLASS_PARAM_VAL_TEMPLATE );
    opSet.setStr( ID_PARAM_NAME, S7_DEVICE_ID );
    opSet.setStr( DESCRIPTION_PARAM_NAME, DESCRIPTION_PARAM_VAL_TEMPLATE );
    opSet.setInt( SYNCH_PERIOD_PARAM_NAME, 1000 );

    IAvTree tree = AvTree.createSingleAvTree( S7_CFG_NODE_ID, opSet, devices );

    // FIXME
    // BaseOpcCommonDlmCfgGenerator.insertProperties( tree, properties, paramValueSource, context );
    return tree;
  }

  /**
   * Just for testing
   *
   * @param aArgs program arguments
   */
  public static void main( String[] aArgs ) {
    S7DevCfgGenerator devCfgGenerator = new S7DevCfgGenerator();
    // создаем два девайса
    IOptionSetEdit deviceOptValues = new OptionSet();
    S7DeviceOptionsUtils.OP_TCP_IP_ADDRESS.setValue( deviceOptValues, AvUtils.avStr( "8.8.8.8" ) );
    S7DeviceOptionsUtils.OP_RACK.setValue( deviceOptValues, AvUtils.avInt( 0 ) );
    S7DeviceOptionsUtils.OP_SLOT.setValue( deviceOptValues, AvUtils.avInt( 2 ) );
    GuiS7Device device1 = new GuiS7Device( "device1.id", "device1.name", deviceOptValues );
    // создаем два блока данных
    GuiS7DataBlock dev1DB50 = new GuiS7DataBlock( device1, 50, false );
    device1.addDataBlock( dev1DB50 );
    // добавляем два тега
    GuiS7Tag s7Tag = new GuiS7Tag( dev1DB50, 6, EAtomicType.BOOLEAN, 7, EKind.R );
    dev1DB50.addTag( s7Tag );
    s7Tag = new GuiS7Tag( dev1DB50, 8, EAtomicType.INTEGER, 0, EKind.R );
    dev1DB50.addTag( s7Tag );
    s7Tag = new GuiS7Tag( dev1DB50, 10, EAtomicType.FLOATING, 0, EKind.R );
    dev1DB50.addTag( s7Tag );

    GuiS7DataBlock dev1DB55 = new GuiS7DataBlock( device1, 55, true );
    device1.addDataBlock( dev1DB55 );
    s7Tag = new GuiS7Tag( dev1DB55, 10, EAtomicType.FLOATING, 0, EKind.R );
    dev1DB55.addTag( s7Tag );
    devCfgGenerator.addS7Device( device1 );

    deviceOptValues = new OptionSet();
    S7DeviceOptionsUtils.OP_TCP_IP_ADDRESS.setValue( deviceOptValues, AvUtils.avStr( "9.8.7.8" ) );
    S7DeviceOptionsUtils.OP_RACK.setValue( deviceOptValues, AvUtils.avInt( 100 ) );
    S7DeviceOptionsUtils.OP_SLOT.setValue( deviceOptValues, AvUtils.avInt( 222 ) );
    GuiS7Device device2 = new GuiS7Device( "device2.id", "device2.name", deviceOptValues );
    devCfgGenerator.addS7Device( device2 );

    IAvTree avTree = devCfgGenerator.generate();
    String TMP_DEST_FILE = "destDevFile.tmp"; //$NON-NLS-1$
    File tmpFile = new File( TMP_DEST_FILE );
    AvTreeKeeper.KEEPER.write( tmpFile, avTree );
    String DLM_CONFIG_STR = "DlmConfig = "; //$NON-NLS-1$
    try {
      PinsConfigFileFormatter.format( TMP_DEST_FILE, "s7test.devcfg", DLM_CONFIG_STR, true );
    }
    catch( IOException ex ) {
      // TODO Auto-generated catch block
      LoggerUtils.error( ex );
    }

  }

}

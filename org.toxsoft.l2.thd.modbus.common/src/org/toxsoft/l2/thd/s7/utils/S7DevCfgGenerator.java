package org.toxsoft.l2.thd.s7.utils;

import static org.toxsoft.l2.thd.s7.IS7ThdConstants.*;

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
import org.toxsoft.l2.thd.s7.*;
import org.toxsoft.l2.thd.s7.gui.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация генератора содержимого файла конфигурации нижней половины драйвера S7 (s7.devcfg)
 *
 * @author dima
 */
public class S7DevCfgGenerator
    implements IS7DevCfgGenerator {

  /**
   * все S7 устройства
   */
  protected IListEdit<GuiS7Device> s7Devices = new ElemArrayList<>();

  /**
   * Added device info to driver configuration
   *
   * @param aDevice S7 device configuration info
   */
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
    tags.put( DB_TAGS_ARRAY_NAME, tagArrayTree );

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
  @SuppressWarnings( "nls" )
  public static void main( String[] aArgs ) {
    S7DevCfgGenerator devCfgGenerator = new S7DevCfgGenerator();
    // создаем два девайса
    // Moscow bench configuration
    IOptionSetEdit deviceOptValues = new OptionSet();
    S7DeviceOptionsUtils.OP_TCP_IP_ADDRESS.setValue( deviceOptValues, AvUtils.avStr( "192.168.12.39" ) );
    S7DeviceOptionsUtils.OP_RACK.setValue( deviceOptValues, AvUtils.avInt( 0 ) );
    S7DeviceOptionsUtils.OP_SLOT.setValue( deviceOptValues, AvUtils.avInt( 1 ) );
    GuiS7Device benchDevice =
        new GuiS7Device( "bench_IP_192_168_12_39_rack_0.slot_1.id", "Siemens bench, rack 0, slot 1", deviceOptValues );
    // создаем блок данных DB6
    GuiS7DataBlock benchDeviceDB6 = new GuiS7DataBlock( benchDevice, 6 );
    benchDevice.addDataBlock( benchDeviceDB6 );
    // тег DB6.DBD20
    GuiS7Tag s7Tag = new GuiS7Tag( benchDeviceDB6, 20, EAtomicType.FLOATING, 7, EKind.RW );
    benchDeviceDB6.addTag( s7Tag );
    // тег DB6.DBD24
    s7Tag = new GuiS7Tag( benchDeviceDB6, 24, EAtomicType.FLOATING, 0, EKind.RW );
    benchDeviceDB6.addTag( s7Tag );

    // создаем блок данных DB50
    GuiS7DataBlock benchDeviceDB50 = new GuiS7DataBlock( benchDevice, 50 );
    benchDevice.addDataBlock( benchDeviceDB50 );
    // тег DB50.DBX0.2
    s7Tag = new GuiS7Tag( benchDeviceDB50, 0, EAtomicType.BOOLEAN, 2, EKind.RW );
    benchDeviceDB50.addTag( s7Tag );
    // тег DB50.DBX4.5
    s7Tag = new GuiS7Tag( benchDeviceDB50, 4, EAtomicType.BOOLEAN, 5, EKind.RW );
    benchDeviceDB50.addTag( s7Tag );
    // тег DB50.DBX4.6
    s7Tag = new GuiS7Tag( benchDeviceDB50, 4, EAtomicType.BOOLEAN, 6, EKind.RW );
    benchDeviceDB50.addTag( s7Tag );

    // создаем блок данных DB58
    GuiS7DataBlock benchDeviceDB58 = new GuiS7DataBlock( benchDevice, 58 );
    benchDevice.addDataBlock( benchDeviceDB58 );
    // тег DB58.DBW12
    s7Tag = new GuiS7Tag( benchDeviceDB58, 12, EAtomicType.INTEGER, 0, EKind.RW );
    benchDeviceDB58.addTag( s7Tag );
    devCfgGenerator.addS7Device( benchDevice );

    deviceOptValues = new OptionSet();
    S7DeviceOptionsUtils.OP_TCP_IP_ADDRESS.setValue( deviceOptValues, AvUtils.avStr( "192.168.12.39" ) );
    S7DeviceOptionsUtils.OP_RACK.setValue( deviceOptValues, AvUtils.avInt( 0 ) );
    S7DeviceOptionsUtils.OP_SLOT.setValue( deviceOptValues, AvUtils.avInt( 1 ) );
    GuiS7Device device2 = new GuiS7Device( "fake_device.id", "fake device just for testing", deviceOptValues );
    devCfgGenerator.addS7Device( device2 );

    IAvTree s7DevicesTree = devCfgGenerator.generate();

    IUnitConfig s7Config = new IUnitConfig() {

      @Override
      public String nmName() {
        return "s7Config.name";
      }

      @Override
      public String id() {
        return "s7Config.id";
      }

      @Override
      public String description() {
        return "s7Config.description";
      }

      @Override
      public IAvTree params() {
        return s7DevicesTree;
      }
    };
    S7DeviceProducer deviceProducer = new S7DeviceProducer();
    deviceProducer.configYourself( s7Config );
    try {
      for( AbstractSpecificDevice abstractDevice : deviceProducer.createSpecificDevices() ) {
        S7Device s7Device = (S7Device)abstractDevice;
        s7Device.readValuesFromLL1();
      }
    }
    catch( Exception ex ) {
      // TODO Auto-generated catch block
      LoggerUtils.error( ex );
    }

    // String TMP_DEST_FILE = "destDevFile.tmp"; //$NON-NLS-1$
    // File tmpFile = new File( TMP_DEST_FILE );
    // AvTreeKeeper.KEEPER.write( tmpFile, s7DevicesTree );
    // try {
    // PinsConfigFileFormatter.format( TMP_DEST_FILE, "s7bench.devcfg", DEV_CONFIG_STR, true );
    // }
    // catch( IOException ex ) {
    // // TODO Auto-generated catch block
    // LoggerUtils.error( ex );
    // }

  }

}

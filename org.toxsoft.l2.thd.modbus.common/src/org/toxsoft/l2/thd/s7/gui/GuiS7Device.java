package org.toxsoft.l2.thd.s7.gui;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * S7 device connection options: ip, rack, slot
 *
 * @author dima
 */
public class GuiS7Device
    extends Stridable {

  /**
   * Values of device connection options.
   */
  private IOptionSetEdit deviceOptValues;

  private IListEdit<GuiS7DataBlock> dataBlocks = new ElemArrayList<>();

  /**
   * Value-object keeper identifier.
   */
  public static final String KEEPER_ID = "GuiS7Device"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public final static IEntityKeeper<GuiS7Device> KEEPER =
      new AbstractEntityKeeper<>( GuiS7Device.class, EEncloseMode.NOT_IN_PARENTHESES, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, GuiS7Device aEntity ) {
          aSw.incNewLine();

          // id, name, description
          aSw.writeAsIs( aEntity.id() );
          aSw.writeSeparatorChar();
          aSw.writeEol();
          aSw.writeQuotedString( aEntity.nmName() );
          aSw.writeSeparatorChar();
          aSw.writeEol();
          // options
          OptionSetKeeper.KEEPER.write( aSw, aEntity.getDeviceOptValues() );
          aSw.writeEol();
        }

        @Override
        protected GuiS7Device doRead( IStrioReader aSr ) {
          // id, name, description
          String id = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          String name = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          IOptionSet deviceOptValues = OptionSetKeeper.KEEPER.read( aSr );
          return new GuiS7Device( id, name, deviceOptValues );
        }
      };

  /**
   * Default modbus device
   */
  public static final GuiS7Device DEFAULT_DEVICE = new GuiS7Device();

  /**
   * Constructor
   *
   * @param aId - id of entity
   * @param aName - human name of address
   * @param aIsTcp - index of tcp device (or rtu)
   * @param aDeviceOptValues - values of connection options
   */
  public GuiS7Device( String aId, String aName, IOptionSet aDeviceOptValues ) {
    super( aId, aName, TsLibUtils.EMPTY_STRING );
    TsNullArgumentRtException.checkNull( aDeviceOptValues );
    deviceOptValues = new OptionSet( aDeviceOptValues );
  }

  @SuppressWarnings( "nls" )
  private GuiS7Device() {
    super( "default.s7.device.id", "default S7 device", TsLibUtils.EMPTY_STRING );
    setDeviceOptValues( S7DeviceOptionsUtils.getParamDefaultValues() );
  }

  /**
   * Sets {@link #nmName()}.
   *
   * @param aName String - short name
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  @Override
  public void setName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    super.setName( aName );
  }

  public IOptionSet getDeviceOptValues() {
    return deviceOptValues;
  }

  public void setDeviceOptValues( IOptionSet aDeviceOptValues ) {
    deviceOptValues = new OptionSet( aDeviceOptValues );
  }

  /**
   * Returns identifier of S7 connection
   *
   * @return String - identifier of S7 connection
   */
  public String getDeviceS7ConnectionId() {
    StringBuilder result = new StringBuilder();
    result.append( "tcp" );

    String ipAddress = S7DeviceOptionsUtils.OP_TCP_IP_ADDRESS.getValue( getDeviceOptValues() ).asString();
    int rack = S7DeviceOptionsUtils.OP_RACK.getValue( getDeviceOptValues() ).asInt();
    int slot = S7DeviceOptionsUtils.OP_SLOT.getValue( getDeviceOptValues() ).asInt();

    result.append( "_" );
    result.append( ipAddress );
    result.append( "_" );
    result.append( rack );
    result.append( "_" );
    result.append( slot );
    return result.toString().replace( ".", "_" );
  }

  /**
   * Returns device identifier forming from its connection options
   *
   * @return String - device identifier forming from its connection options (tcp-ipAddress-port, rtu-portName-address)
   */
  public String getDeviceId() {
    StringBuilder result = new StringBuilder();
    result.append( "tcp" );

    IList<IDataDef> paramDefinitions = S7DeviceOptionsUtils.getParamDefinitions();

    for( IDataDef dDef : paramDefinitions ) {
      result.append( "_" );
      result.append( dDef.id() );
      result.append( "_" );
      result.append( dDef.getValue( deviceOptValues ).toString() );
    }
    return result.toString().replace( ".", "_" );
  }

  @Override
  public String toString() {
    return nmName(); // just to dispalay without id
  }

  public IList<GuiS7DataBlock> getDataBlocks() {
    return dataBlocks;
  }

  public void addDataBlock( GuiS7DataBlock aDataBlock ) {
    dataBlocks.add( aDataBlock );
  }
}

package org.toxsoft.l2.thd.modbus.common.test;

import static org.toxsoft.l2.thd.modbus.common.IModbusThdConstants.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;
import org.toxsoft.l2.thd.modbus.common.connections.*;

import net.wimpi.modbus.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.procimg.*;

public class TestTcpConnect {

  private static ITransactionCreator transactionCreator;

  public static void main( String[] args ) {

    transactionCreator = new TcpTransactionCreator();

    OptionSet optSet = new OptionSet();
    optSet.setStr( IP_PARAM_ID, "192.168.0.3" );
    optSet.setInt( PORT_PARAM_ID, 502 );

    transactionCreator.config( optSet );

    String cmd = args[0];
    int adress = 1;
    // int reg = Integer.parseInt( args[1] );
    int reg = Integer.parseInt( args[1].substring( 2 ), 16 );
    int regVal = Integer.parseInt( args[2] );

    try {
      switch( cmd ) {
        case "do":
          sendDo( adress, reg, regVal > 0 );
          break;
        case "rdo":
          readDo( adress, reg, regVal );
          break;
        case "ao":
          sendRequaest( adress, reg, regVal );
          break;
        case "mao":
          sendMultiRequaest( adress, reg, regVal );
          break;
        case "rao":
          readAOReg( adress, reg, regVal );
          break;
        case "ai":
          readAIReg( adress, reg, regVal );
          break;

        case "di":
          readDi( adress, reg, regVal );
          break;
      }

    }
    catch( Exception e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    transactionCreator.close();
  }

  private static void readDi( int aAdress, int aReg, int aCount ) {
    // формирование запроса
    ReadInputDiscretesRequest cr = new ReadInputDiscretesRequest( aReg, aCount );
    cr.setUnitID( aAdress );
    // cr.setHeadless();

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Read " + aAdress );
      ModbusResponse r = trans.getResponse();
      for( int i = 0; i < aCount; i++ ) {
        System.out.println( "Response val: " + ((ReadInputDiscretesResponse)r).getDiscreteStatus( i ) );
      }
    }
    catch( ModbusException e ) {
      System.out.println( "Havnt read " + aAdress );
      e.printStackTrace();
    }

  }

  /**
   * Чтение выходных аналоговых регистров
   *
   * @param aAdress
   * @param aReg
   */
  public static void readAOReg( int aAdress, int aReg, int aCount ) {
    // формирование запроса
    ReadMultipleRegistersRequest cr = new ReadMultipleRegistersRequest( aReg, aCount );
    cr.setUnitID( aAdress );
    // cr.setHeadless();

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Read " + aAdress );
      ModbusResponse r = trans.getResponse();
      for( int i = 0; i < aCount; i++ ) {
        System.out.println( "Response val: " + ((ReadMultipleRegistersResponse)r).getRegisterValue( i ) );
      }
    }
    catch( ModbusException e ) {
      System.out.println( "Havnt read " + aAdress );
      e.printStackTrace();
    }

  }

  /**
   * Чтение выходных аналоговых регистров
   *
   * @param aAdress
   * @param aReg
   */
  public static void readAIReg( int aAdress, int aReg, int aCount ) {
    for( int ri = 0; ri < 11; ri++ ) {
      int currReg = aReg + 4 * ri;
      // формирование запроса
      ReadInputRegistersRequest cr = new ReadInputRegistersRequest( currReg, aCount );
      // ReadInputRegistersRequest cr = new ReadInputRegistersRequest( 0x0C, 1 );
      cr.setUnitID( aAdress );
      // cr.setHeadless();

      // транзакция
      ModbusTransaction trans = transactionCreator.createModbusTransaction();
      trans.setRequest( cr );

      // испонение транзакции
      try {
        System.out.println( "Read reg = " + Integer.toHexString( currReg ) + " , count = " + aCount );
        trans.execute();

        ModbusResponse r = trans.getResponse();
        // for( int i = 0; i < aCount; i++ ) {
        // int resp = ((ReadInputRegistersResponse)r).getRegisterValue( i );
        // System.out.println( "Response val: " + resp );
        // }
        byte[] hiBytes = ((ReadInputRegistersResponse)r).getRegister( 0 ).toBytes();
        byte[] lowBytes = ((ReadInputRegistersResponse)r).getRegister( 1 ).toBytes();
        byte[] bytes = new byte[4];
        bytes[0] = hiBytes[0];
        bytes[1] = hiBytes[1];
        bytes[2] = lowBytes[0];
        bytes[3] = lowBytes[1];
        float val = registersToFloat( bytes );
        System.out.println( "Response float val: " + val );
      }
      catch( ModbusException e ) {
        System.out.println( "Havnt read " + aAdress );
        e.printStackTrace();
      }
    }
  }

  /**
   * Converts a byte[4] binary float value to a float primitive.
   *
   * @param bytes the byte[4] containing the float value.
   * @return a float value.
   */
  public static final float registersToFloat( byte[] bytes ) {
    return Float.intBitsToFloat(
        (((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff)) );
  }// registersToFloat

  public static void readDo( int aAdress, int aReg, int aCount ) {
    // формирование запроса
    ReadCoilsRequest cr = new ReadCoilsRequest( aReg, aCount );
    cr.setUnitID( aAdress );
    cr.setHeadless();

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Read " + aAdress );
      ModbusResponse r = trans.getResponse();
      for( int i = 0; i < aCount; i++ ) {
        System.out.println( "Response val: " + ((ReadCoilsResponse)r).getCoilStatus( i ) );
      }
    }
    catch( ModbusException e ) {
      System.out.println( "Havnt read " + aAdress );
      e.printStackTrace();
    }

  }

  public static void sendDo( int aAdress, int aReg, boolean aVal ) {
    // формирование запроса
    WriteCoilRequest cr = new WriteCoilRequest( aReg, aVal );
    cr.setUnitID( aAdress );

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Request sent" );
      WriteCoilResponse r = (WriteCoilResponse)trans.getResponse();
      System.out.println( "Response val: " + r.getCoil() );
    }
    catch( ModbusException e ) {
      System.out.println( "Request havnt sent" );
      e.printStackTrace();
    }

  }

  public static void sendRequaest( int aAdress, int aReg, int aVal ) {
    // формирование запроса
    WriteSingleRegisterRequest cr = new WriteSingleRegisterRequest( aReg, new SimpleRegister( aVal ) );
    cr.setUnitID( aAdress );

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Request sent" );
      WriteSingleRegisterResponse r = (WriteSingleRegisterResponse)trans.getResponse();
      System.out.println( "Response val: " + r.getRegisterValue() );
    }
    catch( ModbusException e ) {
      System.out.println( "Request havnt sent" );
      e.printStackTrace();
    }

  }

  public static void sendMultiRequaest( int aAdress, int aReg, int aVal ) {
    // формирование запроса
    WriteMultipleRegistersRequest cr =
        new WriteMultipleRegistersRequest( aReg, new Register[] { new SimpleRegister( aVal ) } );
    cr.setUnitID( aAdress );

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Multi Request sent" );
      WriteMultipleRegistersResponse r = (WriteMultipleRegistersResponse)trans.getResponse();
      // System.out.println("Response val: " + r.getRegisterValue() );
    }
    catch( ModbusException e ) {
      System.out.println( "Request havnt sent" );
      e.printStackTrace();
    }

  }

}

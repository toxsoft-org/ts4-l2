package org.toxsoft.l2.thd.modbus.common.test;

import org.toxsoft.core.tslib.utils.errors.*;

import net.wimpi.modbus.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.procimg.*;
import net.wimpi.modbus.util.*;

public class TestRtuConnect {

  public static void main( String[] args ) {
    portName = args[0];
    String cmd = args[1];
    int adress = Integer.parseInt( args[2] );
    int reg = Integer.parseInt( args[3] );
    int regVal = Integer.parseInt( args[4] );

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
    close();
  }

  private static void readDi( int aAdress, int aReg, int aCount ) {
    // формирование запроса
    ReadInputDiscretesRequest cr = new ReadInputDiscretesRequest( aReg, aCount );
    cr.setUnitID( aAdress );
    cr.setHeadless();

    // транзакция
    ModbusTransaction trans = createModbusTransaction();
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
    cr.setHeadless();

    // транзакция
    ModbusTransaction trans = createModbusTransaction();
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
    // формирование запроса
    ReadInputRegistersRequest cr = new ReadInputRegistersRequest( aReg, aCount );
    cr.setUnitID( aAdress );
    cr.setHeadless();

    // транзакция
    ModbusTransaction trans = createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Read " + aAdress );
      ModbusResponse r = trans.getResponse();
      for( int i = 0; i < aCount; i++ ) {
        System.out.println( "Response val: " + ((ReadInputRegistersResponse)r).getRegisterValue( i ) );
      }
    }
    catch( ModbusException e ) {
      System.out.println( "Havnt read " + aAdress );
      e.printStackTrace();
    }

  }

  public static void readDo( int aAdress, int aReg, int aCount ) {
    // формирование запроса
    ReadCoilsRequest cr = new ReadCoilsRequest( aReg, aCount );
    cr.setUnitID( aAdress );
    cr.setHeadless();

    // транзакция
    ModbusTransaction trans = createModbusTransaction();
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
    ModbusTransaction trans = createModbusTransaction();
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
    ModbusTransaction trans = createModbusTransaction();
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
    ModbusTransaction trans = createModbusTransaction();
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

  /**
   * Контроль четности
   */
  @SuppressWarnings( "nls" )
  private static String       NONE = "None";
  /**
   * Кодирование
   */
  @SuppressWarnings( "nls" )
  private static final String RTU  = "rtu";

  /**
   * Подключение к устройствам на шине.
   */
  private static SerialConnection connection;

  /**
   * Порт
   */
  private static String portName;

  /**
   * Скорость обмена
   */
  private static int baudRate = 9600;

  /**
   * чётность
   */
  private static String parity = NONE;

  public static ModbusTransaction createModbusTransaction() {
    SerialConnection connection = null;
    try {
      connection = getConnection();
    }
    catch( Exception e ) {
      System.out.println( e.getMessage() );
      e.printStackTrace();
    }
    TsIllegalStateRtException.checkFalse( connection != null && connection.isOpen() );
    // Возвращает созданную транзакцию
    return new ModbusSerialTransaction( connection );
  }

  public static void close() {
    closeConnection();
  }

  /**
   * Возвращает соединение.
   *
   * @return SerialConnection - соединение.
   * @throws Exception - ошибка, возникшая при получении соединения.
   */
  static SerialConnection getConnection()
      throws Exception {
    if( connection == null || !connection.isOpen() ) {
      connection = openConnection();
    }
    return connection;
  }

  /**
   * Устанавливает и возвращает соединение.
   *
   * @return SerialConnection - соединение.
   * @throws Exception - ошибка, возникшая при установке соединения.
   */
  private static SerialConnection openConnection()
      throws Exception {
    // System.setProperty("net.wimpi.modbus.debug","true");

    System.out.println( "Port name = " + portName );
    // CommPortIdentifier.getPortIdentifier(portName).open("Modbus Serial Master", 30000);

    // TODO Dima
    // Код соединения взят из примера на сайте jamod, НО!!! потребовались танцы с бубном вокруг ModbusCoupler
    // иногда этот код работает иногда нет, возможно можно вообще выбросить инициализацию ModbusCoupler
    // setup the modbus master
    // ModbusCoupler.createModbusCoupler(null);
    ModbusCoupler.getReference().setUnitID( 1 ); // <-- this is the master id and it doesn't really matter

    // setup serial parameters
    SerialParameters params = new SerialParameters();
    params.setPortName( portName );
    params.setBaudRate( baudRate );
    params.setDatabits( 8 );
    params.setParity( parity );
    params.setStopbits( 1 );
    params.setEncoding( RTU );
    params.setEcho( false );

    // Modbus.debug = true;
    System.out.println( "Modbus.debug = " + Modbus.debug );
    // open the connection
    SerialConnection retVal = new SerialConnection( params );
    retVal.open();
    // Поскольку на выполнении запроса программа повисает, то пробуем это
    retVal.setReceiveTimeout( 3000 );
    return retVal;
  }

  static void closeConnection() {
    if( connection != null && connection.isOpen() ) {
      connection.close();
    }
  }
}

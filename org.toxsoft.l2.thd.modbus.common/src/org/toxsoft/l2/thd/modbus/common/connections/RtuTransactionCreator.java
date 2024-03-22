package org.toxsoft.l2.thd.modbus.common.connections;

import static org.toxsoft.l2.thd.modbus.common.IModbusThdConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.thd.modbus.common.*;

import net.wimpi.modbus.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;
import net.wimpi.modbus.util.*;

/**
 * Создатель транзакций modbus rtu
 *
 * @author max
 */
public class RtuTransactionCreator
    implements ITransactionCreator {

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
  private SerialConnection connection;

  /**
   * Порт
   */
  private String portName;

  /**
   * Скорость обмена
   */
  private int baudRate;

  /**
   * чётность
   */
  private String parity;

  /**
   * Тайм-аут получения ответа от устройства.
   */
  private int modbusReceiveTimeout = 200;

  @Override
  public ModbusTransaction createModbusTransaction() {
    SerialConnection currConnection = null;
    try {
      currConnection = getConnection();
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
    TsIllegalStateRtException.checkFalse( currConnection != null && currConnection.isOpen() );
    // Возвращает созданную транзакцию
    return new ModbusSerialTransaction( currConnection );
  }

  @Override
  public void config( IOptionSet aCfgParams ) {
    portName = aCfgParams.getStr( PORT_NAME_PARAM_ID );
    baudRate = aCfgParams.getInt( BAUD_RATE_PARAM_ID );
    parity = aCfgParams.getStr( PARITY_PARAM_ID, NONE );
    modbusReceiveTimeout = aCfgParams.getInt( MODBUS_TIMEOUT_PARAM_ID, 200 );
  }

  @Override
  public void close() {
    closeConnection();
  }

  /**
   * Возвращает соединение.
   *
   * @return SerialConnection - соединение.
   * @throws Exception - ошибка, возникшая при получении соединения.
   */
  SerialConnection getConnection()
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
  private SerialConnection openConnection()
      throws Exception {

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

    // open the connection
    SerialConnection retVal = new SerialConnection( params );
    retVal.open();
    // Поскольку на выполнении запроса программа повисает, то пробуем это
    retVal.setReceiveTimeout( modbusReceiveTimeout );
    return retVal;
  }

  void closeConnection() {
    if( connection != null && connection.isOpen() ) {
      connection.close();
    }
  }

}

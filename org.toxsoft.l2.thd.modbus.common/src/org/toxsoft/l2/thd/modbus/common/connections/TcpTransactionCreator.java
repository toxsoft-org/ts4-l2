package org.toxsoft.l2.thd.modbus.common.connections;

import static org.toxsoft.l2.thd.modbus.common.IModbusThdConstants.*;

import java.net.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

import net.wimpi.modbus.io.*;
import net.wimpi.modbus.net.*;

/**
 * Заготовка создателя транзакций modbus tcp
 *
 * @author max
 */
public class TcpTransactionCreator
    implements ITransactionCreator {

  /**
   * IP адрес
   */
  private String ipAddress;

  /**
   * Порт
   */
  private int port;

  /**
   * Подключение к устройствам на шине.
   */
  private TCPMasterConnection connection;

  @Override
  public ModbusTransaction createModbusTransaction() {
    TCPMasterConnection currConnection = null;
    try {
      currConnection = getConnection();
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
    TsIllegalStateRtException.checkFalse( currConnection != null && currConnection.isConnected() );
    // Возвращает созданную транзакцию

    return new ModbusTCPTransaction( currConnection );
  }

  @Override
  public void config( IOptionSet aCfgParams ) {
    ipAddress = aCfgParams.getStr( IP_PARAM_ID );
    port = aCfgParams.getInt( PORT_PARAM_ID );
  }

  @Override
  public void close() {
    closeConnection();
  }

  /**
   * Возвращает соединение.
   *
   * @return TCPMasterConnection - соединение.
   * @throws Exception - ошибка, возникшая при получении соединения.
   */
  TCPMasterConnection getConnection()
      throws Exception {
    if( connection == null || !connection.isConnected() ) {
      connection = openConnection();
    }
    return connection;
  }

  /**
   * Устанавливает и возвращает соединение.
   *
   * @return TCPMasterConnection - соединение.
   * @throws Exception - ошибка, возникшая при установке соединения.
   */
  private TCPMasterConnection openConnection()
      throws Exception {
    InetAddress addr = null;
    try {
      addr = InetAddress.getByName( ipAddress );
    }
    catch( UnknownHostException ex ) {
      // TODO Auto-generated catch block
      LoggerUtils.errorLogger().error( ex );
      return null;
    }
    TCPMasterConnection tcpConnection = new TCPMasterConnection( addr );

    tcpConnection.setPort( port );
    tcpConnection.connect();
    return tcpConnection;
  }

  void closeConnection() {
    if( connection != null && connection.isConnected() ) {
      connection.close();
    }
  }

}

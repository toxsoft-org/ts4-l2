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
  public ModbusTransaction createModbusTransaction()
      throws TsIllegalStateRtException {
    TCPMasterConnection currConnection = getConnection();

    TsIllegalStateRtException.checkFalse( currConnection != null && currConnection.isConnected(),
        "Transaction cannot be created due to connection is not established" );

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
   */
  TCPMasterConnection getConnection() {
    if( connection == null || !connection.isConnected() ) {
      try {
        connection = openConnection();
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
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
    InetAddress addr = InetAddress.getByName( ipAddress );
    TCPMasterConnection tcpConnection = new TCPMasterConnection( addr );

    tcpConnection.setPort( port );
    tcpConnection.connect();
    return tcpConnection;
  }

  void closeConnection() {
    if( connection != null && connection.isConnected() ) {
      connection.close();
      connection = null;
    }
  }

}

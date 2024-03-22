package org.toxsoft.l2.thd.modbus.common;

import org.toxsoft.core.tslib.av.opset.*;

import net.wimpi.modbus.io.*;

/**
 * Создатель modbus транзакций
 *
 * @author max
 */
public interface ITransactionCreator {

  /**
   * Создаёт транзакцию запроса modbus
   *
   * @return ModbusTransaction - объект транзакции.
   */
  ModbusTransaction createModbusTransaction();

  /**
   * Конфигурирует создателя транзакций.
   *
   * @param aCfgParams IOptionSet - параметры
   */
  void config( IOptionSet aCfgParams );

  /**
   * Закрывает ресурсы создателя
   */
  void close();
}

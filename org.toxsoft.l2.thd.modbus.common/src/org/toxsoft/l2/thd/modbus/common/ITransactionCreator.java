package org.toxsoft.l2.thd.modbus.common;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;

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
   * @throws TsIllegalStateRtException - transaction cannot be created.
   */
  ModbusTransaction createModbusTransaction()
      throws TsIllegalStateRtException;

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

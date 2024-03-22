package org.toxsoft.l2.thd.modbus.common;

import org.toxsoft.core.tslib.av.*;

/**
 * Транслятор аналогового сигнала
 *
 * @author Max
 */
public interface IAnalogTranslator {

  /**
   * Переводит данные из массива аналоговых данных в атомарное значение
   *
   * @param aBytes - массив аналоговых данных
   * @return IAtomicValue - атомпрное значение.
   */
  IAtomicValue translate( int[] aBytes );
}

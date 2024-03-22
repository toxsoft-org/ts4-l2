package org.toxsoft.l2.thd.modbus.common;

import org.toxsoft.core.tslib.av.*;

/**
 * Транслятор дискретного сигнала
 *
 * @author Max
 */
public interface IDiscretTranslator {

  /**
   * Переводит данные из массива дискретных данных в атомарное значение
   *
   * @param aBytes - массив дискретных данных
   * @return IAtomicValue - атомпрное значение.
   */
  IAtomicValue translate( boolean[] aBytes );
}

package org.toxsoft.l2.dlm.tags.submodules.data;

import org.toxsoft.core.tslib.av.*;

/**
 * Установщик данных (одного) в датасет на запись.
 *
 * @author max
 */
public interface IGwidValueSetter {

  /**
   * Пустая реализация установщика - ничего не делает.
   */
  IGwidValueSetter NULL = new IGwidValueSetter() {

    @Override
    public boolean setGwidValue( IAtomicValue aValue, long aTime ) {
      return false;
    }

    @Override
    public void close() {
      // без реализации

    }
  };

  /**
   * Устанавливает данные (одно) в дата-сет
   *
   * @param aValue - {@link IAtomicValue} - значение данного.
   * @param aTime long - время.
   * @return true - поступившее данное установлено в датасет, false - поступившее данное не установлено в дата-сет
   *         (например, потому что не изменилось по сравнению с предыдущим).
   */
  boolean setGwidValue( IAtomicValue aValue, long aTime );

  /**
   * Закрывает установщик данных
   */
  void close();
}

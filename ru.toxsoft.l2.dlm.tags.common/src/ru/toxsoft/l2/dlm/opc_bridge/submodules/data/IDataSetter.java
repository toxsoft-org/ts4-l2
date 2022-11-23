package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import org.toxsoft.core.tslib.av.*;

/**
 * Установщик данных (одного) в датасет на запись.
 *
 * @author max
 */
public interface IDataSetter {

  /**
   * Пустая реализация установщика - ничего не делает.
   */
  IDataSetter NULL = new IDataSetter() {

    @Override
    public boolean setDataValue( IAtomicValue aValue, long aTime ) {
      return false;
    }

    @Override
    public void sendOnServer() {
      // без реализации

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
  boolean setDataValue( IAtomicValue aValue, long aTime );

  /**
   * Отправляет данные на сервер
   */
  void sendOnServer();

  /**
   * Закрывает установщик данных
   */
  void close();
}

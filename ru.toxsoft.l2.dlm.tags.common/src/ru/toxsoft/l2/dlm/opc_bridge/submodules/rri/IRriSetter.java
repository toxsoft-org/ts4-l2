package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.skf.rri.lib.*;

/**
 * Установщик одного НСИ.
 *
 * @author dima
 */
public interface IRriSetter {

  /**
   * Пустая реализация установщика - ничего не делает.
   */
  /**
   * Пустая реализация установщика - ничего не делает.
   */
  IRriSetter NULL = new IRriSetter() {

    @Override
    public boolean setRriValue( IAtomicValue aValue, long aTime ) {
      return false;
    }

    @Override
    public IMap<Gwid, ISkRriSection> gwid2Section() {
      return null;
    }

  };

  /**
   * Устанавливает одно значение НСИ
   *
   * @param aValue - {@link IAtomicValue} - значение данного.
   * @param aTime long - время.
   * @return true - поступившее данное установлено в датасет, false - поступившее данное не установлено в дата-сет
   *         (например, потому что не изменилось по сравнению с предыдущим).
   */
  boolean setRriValue( IAtomicValue aValue, long aTime );

  IMap<Gwid, ISkRriSection> gwid2Section();
}

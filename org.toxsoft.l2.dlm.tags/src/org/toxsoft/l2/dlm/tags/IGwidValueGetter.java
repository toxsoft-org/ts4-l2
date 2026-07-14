package org.toxsoft.l2.dlm.tags;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * Источник значения gwid
 *
 * @author max
 */
public interface IGwidValueGetter {

  /**
   * Возвращает значение одного gwid
   *
   * @param aTime long - время.
   * @return IAtomicValue - значение одного gwid.
   */
  IAtomicValue getGwidValue( long aTime );

  /**
   * Закрывает источник данных
   */
  void close();

  /**
   * Возвращает gwid, за возвращение значения которого отвечает этот getter
   *
   * @return Gwid - gwid, за возвращение значения которого отвечает этот getter
   */
  Gwid gwid();
}

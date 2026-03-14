package org.toxsoft.l2.lib.hal;

import org.toxsoft.core.tslib.coll.*;

/**
 * Обработчик исключений, возникающих во время физической записи и чтения с устройств.
 *
 * @author MAX
 */
public interface IHalErrorProcessor {

  /**
   * Вызывается аппаратом в основном потоке выполнения в случае ошибок, которые аппарат считает нужными передать наверх.
   *
   * @param aErrors IList - параметры исключения (нужно уточнять)
   */
  void onApparatError( IList<ApparatError> aErrors );
}

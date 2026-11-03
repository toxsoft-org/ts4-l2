package org.toxsoft.l2.lib_old.net;

import org.toxsoft.uskat.core.connection.*;

/**
 * Слой работы с сетью.
 *
 * @author max
 */
public interface INetwork {

  /**
   * Предоставляет соединение с сервером.
   *
   * @return ISkConnection - соединение с сервером.
   */
  ISkConnection getSkConnection();

}

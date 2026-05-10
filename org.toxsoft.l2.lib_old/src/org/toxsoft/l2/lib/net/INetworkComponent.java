package org.toxsoft.l2.lib.net;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.l2.lib.main.*;

/**
 * Компонента доступа к ВУ и сети.
 * <p>
 * Компонента расширяет интерфейс {@link INetwork} методами управления жизненным циклом.
 *
 * @author goga
 */
public interface INetworkComponent
    extends ICooperativeWorkerComponent, INetwork {

  /**
   * Возвращает команду завершения работы, если такова была получена или null.
   *
   * @return {@link IProgramQuitCommand} - команда завершения (перезагрузки) программы НУ
   */
  IProgramQuitCommand getQuitCommandIfAny();
}

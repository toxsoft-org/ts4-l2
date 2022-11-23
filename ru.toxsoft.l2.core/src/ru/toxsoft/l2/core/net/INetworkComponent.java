package ru.toxsoft.l2.core.net;

import org.toxsoft.core.tslib.bricks.*;

import ru.toxsoft.l2.core.main.*;

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

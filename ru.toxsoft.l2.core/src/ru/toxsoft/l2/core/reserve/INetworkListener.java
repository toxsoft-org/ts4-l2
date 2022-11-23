package ru.toxsoft.l2.core.reserve;

import org.toxsoft.core.tslib.bricks.*;

/**
 * Слушатель состояния соединения с сервером и команд с сервера
 *
 * @author max
 */
public interface INetworkListener
    extends ICooperativeWorkerComponent {

  /**
   * Проверяет наличие соединения с сервером.
   *
   * @return true - соединение с сервером прервано, false - обмен с сервером осуществляется.
   */
  boolean isConnectionBreak();

  /**
   * Проверяет наличие команд с сервера.
   *
   * @return true - есть команды, полученные с сервера, false - команд с сервера не поступало.
   */
  boolean isCommandReceived();

  /**
   * Команда, полученная с сервера
   *
   * @return ReserveCommandOnExecuting - команды с сервера.
   */
  ReserveCommandOnExecuting getReceivedCommand();

  /**
   * Команда выполнена.
   *
   * @param aCommand ReserveCommandOnExecuting - выполненная команда.
   */
  void commandHasBeenDone( ReserveCommandOnExecuting aCommand );
}

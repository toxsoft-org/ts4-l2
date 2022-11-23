package ru.toxsoft.l2.core.reserve;

import org.toxsoft.core.tslib.bricks.*;

/**
 * Контроллер остановки, запуска
 *
 * @author max
 */
public interface IDlmsController
    extends ICooperativeWorkerComponent {

  /**
   * Запускает работу модулей.
   */
  void startDLMs();

  /**
   * Останавливает работу модулей.
   */
  void stopDlms();

  /**
   * Возвращает состояние работы DLM модулей.
   *
   * @return true - модули работают,
   *         <p>
   *         false - модули не работают.
   */
  boolean isDLMsRun();
}

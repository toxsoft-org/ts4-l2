package ru.toxsoft.l2.core.reserve;

/**
 * Интерфейс поддержки резервирования
 * 
 * @author max
 */
public interface IReserve {

  /**
   * Возвращает команду остановки модулей, если такова была получена или null.
   * 
   * @return {@link IProgramStopDlmsCommand} - команда установки НУ на паузу.
   */
  // IProgramStopDlmsCommand getStopDlmsCommandIfAny();

  /**
   * Сообщает о том что функциональные модули остановлены
   * 
   * @param aIsSuspended
   */
  // void becomeStopDlms( boolean aIsSuspended );

  /**
   * Возвращает состояние работы DLM модулей.
   * 
   * @return true - модули работают,
   *         <p>
   *         false - модули не работают.
   */
  boolean isDLMsRun();
}

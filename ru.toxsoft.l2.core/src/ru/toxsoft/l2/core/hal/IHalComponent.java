package ru.toxsoft.l2.core.hal;

import org.toxsoft.core.tslib.bricks.*;

/**
 * Компонента доступа к оборудованию БУ.
 * <p>
 * Компонента расширяет интерфейс {@link IHal} методами управления жизненным циклом.
 *
 * @author goga
 */
public interface IHalComponent
    extends ICooperativeWorkerComponent, IHal, IHalErrorProcessor {

  /**
   * Метод, инициирующий физическое чтение данных с устройств.
   */
  void doReadDevices();

  /**
   * Метод получения данных с НУ (не физическое чтение, а получение прочитанных из буфера)
   */
  void doReadValues();

  /**
   * Метод передачи данных на НУ (передача данных в буфер) и дальнейшего инициирования физической записи на устройства.
   */
  void doWriteValues();
}

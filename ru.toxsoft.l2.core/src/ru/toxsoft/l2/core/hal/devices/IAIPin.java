package ru.toxsoft.l2.core.hal.devices;

/**
 * Аналоговый вход устройства.
 * 
 * @author max
 */
public interface IAIPin
    extends IPin {

  /**
   * Возвращает значение аналогового входа.
   * 
   * @return Float - значение аналогового входа, может быть null, если устройство не
   *         инициализировано или ошиба??? XXX
   */
  Float getAI();

}

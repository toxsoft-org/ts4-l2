package ru.toxsoft.l2.core.hal.devices;

/**
 * Аналоговый выход.
 * 
 * @author max
 */
public interface IAOPin
    extends IPin {

  /**
   * Устанавливает значение аналогового выхода.
   * 
   * @param aValue Float - значение аналогового выхода, может быть null??? XXX
   */
  void setAO( Float aValue );

}

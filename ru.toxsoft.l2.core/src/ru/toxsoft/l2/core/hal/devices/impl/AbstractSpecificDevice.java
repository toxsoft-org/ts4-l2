package ru.toxsoft.l2.core.hal.devices.impl;

import ru.toxsoft.l2.core.hal.IHalErrorProcessor;
import ru.toxsoft.l2.core.hal.devices.AbstractHalIoDevice;
import ru.toxsoft.l2.core.hal.devices.ISpecificDevice;

/**
 * Абстрактная реализация спец оборудования, помимо методов API добавляет обязательные методы для обмена с
 * низкоуровневыми протоколами. Всё спец оборудование должно быть наследниками этого класса (а иначе оно не сможет
 * попасть в hal через создателя-продюсера).
 * 
 * @author max
 */
public abstract class AbstractSpecificDevice
    extends AbstractHalIoDevice
    implements ISpecificDevice {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Конструктор по идентификатору и описанию.
   * 
   * @param aId String - идентификатор,
   * @param aDescription String - описание.
   * @param aErrorProcessor IHalErrorProcessor - обработчик ошибок, возникающих при обмене с устройствами.
   */
  public AbstractSpecificDevice( String aId, String aDescription, IHalErrorProcessor aErrorProcessor ) {
    super( aId, aDescription, aErrorProcessor );
  }

}

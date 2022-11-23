package ru.toxsoft.l2.core.hal.devices.impl;

import org.toxsoft.core.tslib.bricks.strid.impl.*;

import ru.toxsoft.l2.core.hal.devices.*;

/**
 * Абстрактная реализация пина, помимо методов API добавляет обязательные методы для обмена с низкоуровневыми
 * протоколами. Все пины должны быть наследниками этого класса (а иначе они не смогут попасть в hal через
 * создателя-продюсера). Наследует интерфесы всех типов пинов, чтоб нельзя было создать реализацию, не наследующую
 * конкретный нужный тип.
 *
 * @author max
 */
public abstract class AbstractPin
    extends Stridable
    implements IAIPin, IAOPin, IDIPin, IDOPin {

  /**
   * Конструктор по идентификатору и описанию.
   *
   * @param aId String - идентификатор,
   * @param aDescription String - описание.
   */
  public AbstractPin( String aId, String aDescription ) {
    super( aId, aDescription, aDescription, true );
  }

  //
  // --------------------------------------------------------
  // доп методы

  /**
   * Осуществляет некоторую работу
   */
  public abstract void doJob();

  //
  // ----------------------------------------------------------
  // Пустые методы конкретных типов пинов

  @Override
  public void setDO( Boolean aValue ) {
    // реализация в наследнике AbstractDOPin
  }

  @Override
  public Boolean getDI() {
    return null;
  }

  @Override
  public void setAO( Float aValue ) {
    // реализация в наследнике AbstractAOPin
  }

  @Override
  public Float getAI() {
    return null;
  }

}

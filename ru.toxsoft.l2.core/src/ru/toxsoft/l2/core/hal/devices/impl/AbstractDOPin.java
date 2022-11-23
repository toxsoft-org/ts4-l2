package ru.toxsoft.l2.core.hal.devices.impl;

import ru.toxsoft.l2.core.hal.devices.EPinKind;

/**
 * Абстрактная реализация дискретного выхода.
 * 
 * @author max
 */
public abstract class AbstractDOPin
    extends AbstractPin {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Значение выхода, которое следует записать на устройство.
   */
  protected Boolean value = null;

  /**
   * Конструктор по идентификатору и описанию.
   * 
   * @param aId String - идентификатор,
   * @param aDescription String - описание.
   */
  public AbstractDOPin( String aId, String aDescription ) {
    super( aId, aDescription );
  }

  //
  // -----------------------------------------------
  // Реализация метода интерфейса IPin

  @Override
  public EPinKind kind() {
    return EPinKind.DO;
  }

  //
  // -------------------------------------------------
  // Реализация метода интерфейса IDOPin

  @Override
  public void setDO( Boolean aValue ) {
    value = aValue;
  }

}

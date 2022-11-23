package ru.toxsoft.l2.core.hal.devices.impl;

import ru.toxsoft.l2.core.hal.devices.EPinKind;

/**
 * Абстрактная реализация аналогового выхода.
 * 
 * @author max
 */
public abstract class AbstractAOPin
    extends AbstractPin {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  

  /**
   * Значение выхода, которое следует записать на устройство.
   */
  protected Float value = null;

  /**
   * Конструктор по идентификатору и описанию.
   * 
   * @param aId String - идентификатор,
   * @param aDescription String - описание.
   */
  public AbstractAOPin( String aId, String aDescription ) {
    super( aId, aDescription );
  }

  //
  // -----------------------------------------------
  // Реализация метода интерфейса IPin

  @Override
  public EPinKind kind() {
    return EPinKind.AO;
  }

  //
  // -------------------------------------------------
  // Реализация метода интерфейса IAOPin

  @Override
  public void setAO( Float aValue ) {
    value = aValue;    
  }

  

}

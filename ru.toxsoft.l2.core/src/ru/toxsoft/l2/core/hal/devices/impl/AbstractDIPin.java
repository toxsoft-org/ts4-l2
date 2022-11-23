package ru.toxsoft.l2.core.hal.devices.impl;

import ru.toxsoft.l2.core.hal.devices.EPinKind;

/**
 * Абстрактная реализация дискретного входа.
 * 
 * @author max
 */
public abstract class AbstractDIPin
    extends AbstractPin {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * Значение дискретного входа, полученное от устройства.
   */
  protected Boolean value = null;

  /**
   * Конструктор по идентификатору и описанию.
   * 
   * @param aId String - идентификатор,
   * @param aDescription String - описание.
   */
  public AbstractDIPin( String aId, String aDescription ) {
    super( aId, aDescription );
  }

  //
  // -----------------------------------------------
  // Реализация метода интерфейса IPin

  @Override
  public EPinKind kind() {
    return EPinKind.DI;
  }

  //
  // -------------------------------------------------
  // Реализация метода интерфейса IDIPin

  @Override
  public Boolean getDI() {
    return value;
  }

  
}

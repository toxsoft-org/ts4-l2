package ru.toxsoft.l2.core.hal.devices.impl;

import ru.toxsoft.l2.core.hal.devices.EPinKind;

/**
 * Абстрактная реализация аналогового входа.
 * 
 * @author max
 */
public abstract class AbstractAIPin
    extends AbstractPin {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * Значение аналогового входа, полученное от устройства.
   */
  protected Float value = null;

  /**
   * Конструктор по идентификатору и описанию.
   * 
   * @param aId String - идентификатор,
   * @param aDescription String - описание.
   */
  public AbstractAIPin( String aId, String aDescription ) {
    super( aId, aDescription );
  }

  //
  // -----------------------------------------------
  // Реализация метода интерфейса IPin

  @Override
  public EPinKind kind() {
    return EPinKind.AI;
  }

  //
  // -------------------------------------------------
  // Реализация метода интерфейса IAIPin

  @Override
  public Float getAI() {
    return value;
  }

  
}

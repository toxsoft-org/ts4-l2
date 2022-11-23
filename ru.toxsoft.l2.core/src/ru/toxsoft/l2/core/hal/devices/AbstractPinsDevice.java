package ru.toxsoft.l2.core.hal.devices;

import org.toxsoft.core.tslib.bricks.strid.coll.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;

/**
 * Аппарат, инкапсулирующий все пины устройства.
 *
 * @author MAX
 */
public abstract class AbstractPinsDevice
    extends AbstractSpecificDevice {

  /**
   * Для корректной сериализации.
   */
  private static final long serialVersionUID = 1L;

  protected IStridablesListEdit<AbstractPin> pins;

  protected IStridablesListEdit<AbstractPin> pinsDO;

  protected IStridablesListEdit<AbstractPin> pinsDI;

  protected IStridablesListEdit<AbstractPin> pinsAO;

  protected IStridablesListEdit<AbstractPin> pinsAI;

  /**
   * Конструктор по идентификатору, описанию и обработчику ошибок.
   *
   * @param aId String - идентификатор.
   * @param aDescription String - описание.
   * @param aErrorProcessor IHalErrorProcessor - обработчик ошибок.
   */
  public AbstractPinsDevice( String aId, String aDescription, IHalErrorProcessor aErrorProcessor ) {
    super( aId, aDescription, aErrorProcessor );

  }

  /**
   * Возвращает список пинов устройства в виде их абстрактной реализации.
   *
   * @return IStridablesList - список пинов устройства в виде их абстрактной реализации.
   */
  public IStridablesList<AbstractPin> getPins() {
    return pins;
  }

}

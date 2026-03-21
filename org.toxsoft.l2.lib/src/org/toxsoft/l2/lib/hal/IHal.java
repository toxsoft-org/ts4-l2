package org.toxsoft.l2.lib.hal;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.l2.lib.hal.devices.*;
import org.toxsoft.l2.lib.reserve.*;

/**
 * HAL (Hardware Abstraction Layer) - уровень абстрагирования от железа блока управления.
 * <p>
 *
 * @author max
 */
public interface IHal
    extends IHealthMeasurable {

  /**
   * Возвращает список всех зарегистрированных пинов.
   *
   * @return IStridablesList - список всех зарегистрированных пинов.
   */
  IStridablesList<? extends IPin> listPins();

  /**
   * Возвращает список всех зарегистрированных аналоговых входов.
   *
   * @return IStridablesList - список всех зарегистрированных аналоговых входов.
   */
  IStridablesList<? extends IAIPin> listAIPins();

  /**
   * Возвращает список всех зарегистрированных аналоговых выходов.
   *
   * @return IStridablesList - список всех зарегистрированных аналоговых выходов.
   */
  IStridablesList<? extends IAOPin> listAOPins();

  /**
   * Возвращает список всех зарегистрированных дискретных входов.
   *
   * @return IStridablesList - список всех зарегистрированных аналоговых входов.
   */
  IStridablesList<? extends IDIPin> listDIPins();

  /**
   * Возвращает список всех зарегистрированных дискретных выходов.
   *
   * @return IStridablesList - список всех зарегистрированных аналоговых выходов.
   */
  IStridablesList<? extends IDOPin> listDOPins();

  /**
   * Returns all registered specific devices.
   *
   * @return {@link IStridablesList}&lt;{@link ISpecificDevice}&gt; - list of specific devices
   */
  IStridablesList<? extends ISpecificDevice> listSpecificDevices();

  /**
   * Возвращает параметры настройки HAL
   *
   * @return {@link IOptionSet} - параметры настройки HAL.
   */
  IOptionSet options();

  /**
   * Возвращает признак того, что работает как резервный (все устройства общения с железом находятся в отключённом
   * состоянии (или резерве))
   *
   * @return true - все устройства работают как главные
   */
  boolean isWorkAsReserve();

  /**
   * Запрос на то, чтобы HAL ушёл в состояние резерва (те устройства, которые должны уходить в резерв). Метод должен
   * вернуть управление сразу. О том, что HAL ушёл в состояние резерва, следует узнавать с помощью метода
   * {@link #isWorkAsReserve()}==true
   */
  void queryReserve();

  /**
   * Запрос на то, чтобы HAL ушёл в состояние главного. Метод должен вернуть управление сразу. О том, что HAL ушёл в
   * состояние главного, следует узнавать с помощью метод {@link #isWorkAsReserve()}==false
   */
  void queryMain();
}

package ru.toxsoft.l2.core.hal.devices;

import org.toxsoft.core.tslib.bricks.strid.coll.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;

/**
 * Интерфейс ВСЕХ создателей пинов и спец. устройств (бывших top half драйверов).
 * <p>
 * Реализация создателя создается методом {@link Class#forName(String)}, и поэтому, создатель должен иметь открытый
 * конструктор без параметров. Имя класса берется HAL-ом из загруженной конфигурации {@link IUnitConfig#params()} по
 * названию поля <b>javaClassName</b>. Создатель реализует интерфейс {@link IConfigurableUnit}, т.е. реализация
 * создателя должна инициализироваться по соответствующей поступившей конфигурационной информации {@link IUnitConfig}.
 * <p>
 * Далее описание бывших top half drivers На данный момент известны как минимум следующие top half драйверы:
 * <ul>
 * <li>UNIO - плат(ы) Unio48 и Unio96, работает с BH драйвером unio;</li>
 * <li>TS_CONSOLE - консоль системы ТРОЛЛЬ-4,5 (существуют разные версии!), работает с BH драйвером bus485;</li>
 * <li>TS_ENGINE_CONTROL - плата управления двигателями системы ТРОЛЛЬ-4,5 (существуют разные версии!), работает с BH
 * драйвером bus485;</li>
 * <li>TS_RELAY_OUTS - плата релейных выходов общего назначения ТоксСофт, работает с BH драйвером bus485;</li>
 * <li>CONSTEL_AIN - плата фирмы Констэл аналогового ввода напряжения электролизеров, работает с BH драйвером (или
 * напрямую библиотекой?) modbus;</li>
 * <li>CONSTEL_ENGINE_CURRENTS - плата фирмы Констэл аналогового ввода токов дигателей, работает с BH драйвером (или
 * напрямую библиотекой?) modbus;</li>
 * <li>CONSTEL_??? - еще какая-то 9Надо уточнить) плата фирмы Констэл, работает с BH драйвером (или напрямую
 * библиотекой?) modbus;</li>
 * <li>??? - может быть еще оборудование, надо уточнить у Сергея Синько.</li>
 * </ul>
 *
 * @author goga
 */
public interface IDevicesProducer
    extends IConfigurableUnit {

  /**
   * Устанавливает обработчика ошибок обмена с физ. уровнем.
   *
   * @param aErrorProcessor IHalErrorProcessor - обработчик ошибок обмена с физ. уровнем.
   */
  void setHalErrorProcessor( IHalErrorProcessor aErrorProcessor );

  /**
   * Создаёт и возвращает список пинов устройства в виде их абстрактной реализации.
   *
   * @return IStridablesList - список пинов устройства в виде их абстрактной реализации.
   */
  // IStridablesList<AbstractPin> createPins();

  /**
   * Создаёт и возвращает аппарат с пинами.
   *
   * @return AbstractPinsDevice аппарат с пинами.
   * @throws Exception - ошибка создания аппарата.
   */
  AbstractPinsDevice createPinsDevice()
      throws Exception;

  /**
   * Создаёт и возвращает список спец. оборудования устройства в виде их абстрактной реализации.
   *
   * @return IStridablesList - список спец. оборудования устройства в виде их абстрактной реализации.
   * @throws Exception - ошибка создания аппаратов.
   */
  IStridablesList<AbstractSpecificDevice> createSpecificDevices()
      throws Exception;
}

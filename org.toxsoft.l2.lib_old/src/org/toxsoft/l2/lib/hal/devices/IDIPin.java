package org.toxsoft.l2.lib.hal.devices;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

/**
 * Дискретный вход.
 *
 * @author max
 */
public interface IDIPin
    extends IPin {

  /**
   * Возвращает значение дискретного входа.
   *
   * @return Boolean - значение дискретного входа, может быть null, если устройство не инициализировано или ошиба??? XXX
   */
  Boolean getDI();

  /**
   * Пустая заглушка DI пина
   */
  IDIPin STUB_DI_PIN = new StubDIPin();

  /**
   * Реализация пустой заглушки
   *
   * @author max
   */
  static class StubDIPin
      implements IDIPin {

    @Override
    public EPinKind kind() {
      return EPinKind.DI;
    }

    @Override
    public String id() {
      return NONE_ID;
    }

    @Override
    public String description() {
      return EMPTY_STRING;
    }

    @Override
    public Boolean getDI() {
      // пустая реализация
      return null;
    }

    @Override
    public String nmName() {
      return id();
    }

  }

}

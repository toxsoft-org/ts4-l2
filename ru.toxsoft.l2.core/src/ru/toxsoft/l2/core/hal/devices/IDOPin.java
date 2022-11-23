package ru.toxsoft.l2.core.hal.devices;

/**
 * Дискретный выход.
 *
 * @author max
 */
public interface IDOPin
    extends IPin {

  /**
   * Устанавливает значение дискретного выхода.
   *
   * @param aValue Boolean - значение дискретного выхода, может быть null??? XXX
   */
  void setDO( Boolean aValue );

  /**
   * Пустая заглушка DO пина
   */
  IDOPin STUB_DO_PIN = new StubDOPin();

  /**
   * Реализация пустой заглушки
   *
   * @author max
   */
  static class StubDOPin
      implements IDOPin {

    @Override
    public EPinKind kind() {
      return EPinKind.DO;
    }

    @Override
    public String id() {
      return NONE_ID;
    }

    @Override
    public String description() {
      return ru.toxsoft.l2.core.util.StridUtils.EMPTY_STRING;
    }

    @Override
    public void setDO( Boolean aValue ) {
      // ничего не делает
    }

    @Override
    public String nmName() {
      return id();
    }

  }

}

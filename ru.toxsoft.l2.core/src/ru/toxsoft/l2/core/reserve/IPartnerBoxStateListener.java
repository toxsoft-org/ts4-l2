package ru.toxsoft.l2.core.reserve;

import org.toxsoft.core.tslib.bricks.*;

import ru.toxsoft.l2.core.util.*;

/**
 * Слушатель состояния шкафа-напарника
 *
 * @author max
 */
public interface IPartnerBoxStateListener
    extends ICooperativeWorkerComponent {

  /**
   * Возвращает состояние шкафа-напарника.
   *
   * @return EPartnerBoxReserveState - состояние шкафа-напарника.
   */
  EPartnerBoxReserveState getPartnerBoxState();

  /**
   * Экземпляр с реализацией - заглушкой
   */
  IPartnerBoxStateListener EMPTY_REALISATION = new EmptyPartnerBoxStateListener();

  /**
   * Пустая реализация
   *
   * @author max
   */
  static class EmptyPartnerBoxStateListener
      extends WorkerComponentBase
      implements IPartnerBoxStateListener {

    @Override
    public void doJob() {
      // no
    }

    @Override
    public EPartnerBoxReserveState getPartnerBoxState() {
      // no
      return EPartnerBoxReserveState.UNKNOWN;
    }

  }
}

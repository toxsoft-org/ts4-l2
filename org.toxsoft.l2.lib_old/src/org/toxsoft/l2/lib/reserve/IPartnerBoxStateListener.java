package org.toxsoft.l2.lib.reserve;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.l2.lib.util.*;

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

package ru.toxsoft.l2.core.reserve;

import org.toxsoft.core.tslib.bricks.*;

import ru.toxsoft.l2.core.util.*;

/**
 * Класс отправки состояния шкафа
 *
 * @author max
 */
public interface IBoxStateSender
    extends ICooperativeWorkerComponent {

  /**
   * Отправляет шкафу партнёру своё состояние.
   *
   * @param aState EPartnerBoxReserveState - собственное состояние (в части резервирования)
   */
  void setReserveSignalOwnState( EPartnerBoxReserveState aState );

  /**
   * Экземпляр с реализацией - заглушкой
   */
  IBoxStateSender EMPTY_REALISATION = new EmptyBoxStateSender();

  /**
   * @author max
   */
  class EmptyBoxStateSender
      extends WorkerComponentBase
      implements IBoxStateSender {

    @Override
    public void doJob() {
      // no
    }

    @Override
    public void setReserveSignalOwnState( EPartnerBoxReserveState aState ) {
      // no
    }

  }
}

package ru.toxsoft.l2.core.reserve;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.coll.*;

/**
 * Класс отправки данных и событий на сервер.
 *
 * @author max
 */
public interface INetworkDataSender
    extends ICooperativeWorkerComponent {

  /**
   * Устанавливает состояние шкафа для отправки на сервер.
   *
   * @param aState EReserveState - устанавливаемое состояние шкафа.
   * @param aChangeReason String - причина изменения состояния.
   */
  void setState( EReserveState aState, String aChangeReason );

  /**
   * Устанавливает состояние критического ухудшения функциональности шкафа для отправки на сервер.
   *
   * @param aSick true - критическое ухудшение состояния шкафа,
   *          <p>
   *          false - состояние шкафа позволяет полноценно функционировать.
   */
  void setSick( boolean aSick );

  /**
   * Устанавливает состояние недоступности сервера для шкафа для отправки на сервер!!!!.
   *
   * @param aConnectionBreak true - сервер не доступен для шкафа,
   *          <p>
   *          false - сервер доступен для шкафа.
   */
  void setConnectionBreak( boolean aConnectionBreak );

  /**
   * Устанавливает состояние шкафа-напарника для отправки на сервер.
   *
   * @param aState EPartnerBoxReserveState - устанавливаемое состояние шкафа-напарника.
   */
  void setPartnerState( EPartnerBoxReserveState aState );

  /**
   * Отправляет событие на сервер.
   *
   * @param aEventType EReserveEvent - тип события.
   * @param aParamsValues IList - параметры события.
   */
  void sendEvent( EReserveEvent aEventType, IList<IAtomicValue> aParamsValues );

  /**
   * Устанавливает индикацию того, что начался процесс перехода из резервного в главное.
   *
   * @param aFromReserveToMain true - переход из состояния резервного в главное,
   *          <p>
   *          false - отсутствие перехода из состояния резервного в главное.
   */
  void setFromReserveToMainTransition( boolean aFromReserveToMain );

  /**
   * Устанавливает индикацию того, что начался процесс перехода из главного в резервный.
   *
   * @param aFromMainToReserve true - переход из состояния главного в резервный,
   *          <p>
   *          false - отсутствие перехода из состояния главного в резервный.
   */
  void setFromMainToReserveTransition( boolean aFromMainToReserve );
}

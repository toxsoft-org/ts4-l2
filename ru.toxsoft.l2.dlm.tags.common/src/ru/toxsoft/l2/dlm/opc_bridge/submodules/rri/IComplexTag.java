package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.tslib.av.*;

/**
 * Сложный тег, аггрегирующий несколько тегов: адрес значения (идентификатор команды), теги зачений разных типов, тег
 * обратной связи.
 *
 * @author max
 */
public interface IComplexTag {

  /**
   * Определяет занятость тега
   *
   * @return true - тег занят, не имеет смысла устанавливать новоей значение, false - тег свободен для установки наового
   *         значения.
   */
  boolean isBusy();

  /**
   * Устанавливает значение тега
   *
   * @param aAddress int - адрес значений - условный номер (идентификатор команды)
   * @param aValue IAtomicValue - устанавливаемое значение
   * @return long - время начала установки тега - может использоваться как идентфикатор выполняемого действия, если
   *         возвращается 0 - процедура на установление значения отклонена в самом начала (возможно тег занят устанвокой
   *         другого значения)
   */
  long setValue( int aAddress, IAtomicValue aValue );

  /**
   * Возвращает статус выполнения установки значения, которое было начато во время, вернувшееся в
   * {@link #setValue(int, IAtomicValue)}.
   *
   * @param aSetTime long - время начала установки значения {@link #setValue(int, IAtomicValue)}.
   * @param aDelIfCan boolean true - удалить память об установке значения с началом установки в
   *          {@link #setValue(int, IAtomicValue)}, если такая возможность есть.
   * @return EComplexTagState - состояние установки значения, {@link EComplexTagState#UNKNOWN} - в случае отсутствия
   *         данных об установке значения в указанное время.
   */
  EComplexTagState getState( long aSetTime, boolean aDelIfCan );

  /**
   * Состояние установки значения тега
   *
   * @author max
   */
  public enum EComplexTagState {
    /**
     * установка успешно проведена
     */
    DONE,

    /**
     * установка сброшена по тайм-ауту
     */
    TIMEOUT,

    /**
     * установка сброшена по ошибке
     */
    ERROR,

    /**
     * установка в процессе (этот состояние не может быть удалено из памяти)
     */
    PROCESS,

    /**
     * состояни установки не определено
     */
    UNKNOWN
  }
}

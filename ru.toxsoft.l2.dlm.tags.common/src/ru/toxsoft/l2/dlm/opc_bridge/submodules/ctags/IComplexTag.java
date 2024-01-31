package ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags;

import org.toxsoft.core.tslib.av.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Сложный тег, аггрегирующий несколько тегов: адрес значения (идентификатор команды), теги зачений разных типов, тег
 * обратной связи.
 *
 * @author max
 */
public interface IComplexTag
    extends ITag {

  /**
   * Возвращает идентификатор тега
   *
   * @return String - идентификатор тега
   */
  @Override
  String id();

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
   * @param aValue IAtomicValue - устанавливаемое значение (может быть null или {@link IAtomicValue#NULL} - если
   *          интересует только факт выставления адреса)
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
    DONE( true ),

    /**
     * установка сброшена по тайм-ауту
     */
    TIMEOUT( true ),

    /**
     * установка сброшена по ошибке
     */
    ERROR( true ),

    /**
     * установка в процессе (этот состояние не может быть удалено из памяти)
     */
    PROCESS( false ),

    /**
     * состояни установки не определено
     */
    UNKNOWN( true );

    private boolean isCanBeDeleted = false;

    /**
     * Конструктор по признаку возможности удаления этого состояния
     *
     * @param aIsCanBeDeleted true - состояние может быть удалено.
     */
    EComplexTagState( boolean aIsCanBeDeleted ) {
      isCanBeDeleted = aIsCanBeDeleted;
    }

    /**
     * Возвращает признак возможности удаления этого состояния
     *
     * @return true - состояние может быть удалено.
     */
    public boolean isCanBeDeleted() {
      return isCanBeDeleted;
    }

  }
}

/**
 *
 */
package ru.toxsoft.l2.thd.opc;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * API тега OPC сервера
 *
 * @author Dima
 */
public interface ITag
    extends IStridable {

  /**
   * id тега - уникальное имя тега на OPC сервере
   *
   * @return id тега
   */
  String tagId();

  /**
   * имя тега - имя тега в S5 (опционально)
   *
   * @return имя тега в S5
   */
  String name();

  /**
   * типа тега (на чтение/запись)
   *
   * @return тип тега
   */
  EKind kind();

  /**
   * health of tag
   *
   * @return { {@link ETagHealth} current state of health
   */
  default ETagHealth health() {
    return ETagHealth.UNKNOWN;
  }

  /**
   * типа значения тега
   *
   * @return тип значения тега
   */
  EAtomicType valueType();

  /**
   * Подтип основного типа (например целые: Short, Byte, UShort и т.д.)
   *
   * @return подтип основного типа
   */
  default Class<?> valueTypeExtra() {
    return null;
  }

  /**
   * получить значение тега
   *
   * @return значения тега
   */
  IAtomicValue get();

  /**
   * установить значение тега
   *
   * @param aVal - значение тега
   */
  void set( IAtomicValue aVal );

  /**
   * @return признак установки нового значения
   */
  boolean isDirty();
}

package ge.toxsoft.gwp.opcuabridge;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Тег на чтение.
 *
 * @author max
 */
public interface IReadTag
    extends IStridable {

  /**
   * Возвращает текущее значение тега.
   *
   * @return IAtomicValue - текущее значение тега.
   */
  IAtomicValue getValue();

  /**
   * Тип данных тега.
   *
   * @return EAtomicType - тип данных тега.
   */
  EAtomicType type();
}

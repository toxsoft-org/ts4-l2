package ge.toxsoft.gwp.opcuabridge;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Тег на запись
 *
 * @author max
 */
public interface IWriteTag
    extends IStridable {

  /**
   * Возвращает текущее значение тега.
   *
   * @param aValue IAtomicValue - текущее значение тега.
   */
  void setValue( IAtomicValue aValue );

  /**
   * Тип данных тега.
   *
   * @return EAtomicType - тип данных тега.
   */
  EAtomicType type();
}

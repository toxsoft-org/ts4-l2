package ru.toxsoft.l2.core.invokable;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Описание структуры, идентификатор переменной - {@link IStridable#id()}.
 *
 * @author max
 */
public interface IStructInfo
    extends IStridable {

  /**
   * Тип структуры.
   *
   * @return IStructType - тип структуры.
   */
  IDataDef structType();
}

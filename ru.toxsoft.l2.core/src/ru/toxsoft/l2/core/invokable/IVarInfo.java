package ru.toxsoft.l2.core.invokable;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Описание переменной, идентификатор переменной - {@link IStridable#id()}.
 *
 * @author max
 */
public interface IVarInfo
    extends IStridable {

  /**
   * Признак того, что переменная неизменяемая.
   *
   * @return <b>true</b> - переменна неизменяемая, <br>
   *         <b>false</b> - переменная изменяемая.
   */
  boolean isReadOnly();

  /**
   * Тип переменной.
   *
   * @return IDataType - тип переменной.
   */
  IDataType dataType();
}

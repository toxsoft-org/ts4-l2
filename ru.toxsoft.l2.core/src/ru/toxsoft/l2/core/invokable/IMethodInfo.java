package ru.toxsoft.l2.core.invokable;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.primtypes.*;

/**
 * Описание вызываемого метода, идентификатор метода - {@link IStridable#id()}.
 *
 * @author max
 */
public interface IMethodInfo
    extends IStridable {

  /**
   * ??? тут должны быть типы аргументов? Возвращает описание аргуметов, включает значения по умолчанию.
   *
   * @return IStringMap - описание аршументов метода.
   */
  IStringMap<IDataType> argsInfo();
}

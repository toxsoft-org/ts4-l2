package ru.toxsoft.l2.core.invokable;

import javax.script.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;

/**
 * Информация об объекте класса {@link Invocable} - описание переменных и методов.
 *
 * @author max
 */
public interface IInvokableInfo {

  /**
   * Возвращает список описаний переменных.
   *
   * @return IStridablesList - список описаний переменных.
   */
  IStridablesList<IVarInfo> listVarInfoes();

  /**
   * Возвращает список описаний структур.
   *
   * @return IStridablesList - список описаний структур.
   */
  IStridablesList<IStructInfo> listStructInfoes();

  /**
   * Возвращает список описаний методов.
   *
   * @return IStridablesList - список описаний методов.
   */
  IStridablesList<IMethodInfo> listMethodInfoes();
}

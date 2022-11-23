package ru.toxsoft.l2.core.invokable;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Объект, у которого может быть вызван метод, взято или установлено значение переменной.
 *
 * @author max
 */
public interface IInvokable {

  /**
   * Возвращает значение переменной.
   *
   * @param aVarId - идентификатор переменной.
   * @return IAtomicValue - значение переменной.
   * @throws TsIllegalArgumentRtException - при отсутствии переменной с таким идетификатором
   */
  IAtomicValue getVar( String aVarId )
      throws TsIllegalArgumentRtException;

  /**
   * Устанавливает значение переменной.
   *
   * @param aVarId - идентификатор переменной.
   * @param aValue - значение переменной.
   * @throws TsIllegalArgumentRtException - при отсутствии переменной с таким идетификатором.
   */
  void setVar( String aVarId, IAtomicValue aValue )
      throws TsIllegalArgumentRtException;

  /**
   * Вызывает метод.
   *
   * @param aMethodId - идентификатор метода.
   * @param aArgsValues - аргументы метода.
   * @return ECallStatus - статус вызова метода.
   * @throws TsIllegalArgumentRtException - при отсутствии метода с таким идетификатором.
   */
  ECallStatus call( String aMethodId, IOptionSet aArgsValues )
      throws TsIllegalArgumentRtException;

  /**
   * Возвращает значение структуры
   *
   * @param aStructId - идентификатор структуры.
   * @return IStructValue (Замена на IOptionSet) - значение структуры.
   * @throws TsIllegalArgumentRtException - при отсутствии структуры с таким идетификатором.
   */
  IOptionSet getStructVar( String aStructId )
      throws TsIllegalArgumentRtException;
}

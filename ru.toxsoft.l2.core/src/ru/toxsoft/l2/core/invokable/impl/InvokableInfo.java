package ru.toxsoft.l2.core.invokable.impl;

import javax.script.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;

import ru.toxsoft.l2.core.invokable.*;

/**
 * Реализация описания объекта класса {@link Invocable} - описание переменных и методов.
 *
 * @author MAX
 */
public class InvokableInfo
    implements IInvokableInfo {

  private IStridablesList<IVarInfo> varInfoes;

  private IStridablesList<IMethodInfo> methodInfoes;

  private IStridablesList<IStructInfo> structInfoes;

  /**
   * Конструктор описания объекта класса {@link Invocable}
   *
   * @param aVarInfoes - список описаний переменных, может быть null.
   * @param aMethodInfoes - список описаний методов, может быть null.
   * @param aStructInfoes - список описаний структур, может быть null.
   */
  @SuppressWarnings( "unchecked" )
  public InvokableInfo( IStridablesList<IVarInfo> aVarInfoes, IStridablesList<IMethodInfo> aMethodInfoes,
      IStridablesList<IStructInfo> aStructInfoes ) {
    super();
    if( aVarInfoes != null ) {
      varInfoes = aVarInfoes;
    }
    else {
      varInfoes = IStridablesList.EMPTY;
    }
    if( aMethodInfoes != null ) {
      methodInfoes = aMethodInfoes;
    }
    else {
      methodInfoes = IStridablesList.EMPTY;
    }

    if( aStructInfoes != null ) {
      structInfoes = aStructInfoes;
    }
    else {
      structInfoes = IStridablesList.EMPTY;
    }
  }

  @Override
  public IStridablesList<IVarInfo> listVarInfoes() {
    return varInfoes;
  }

  @Override
  public IStridablesList<IMethodInfo> listMethodInfoes() {
    return methodInfoes;
  }

  @Override
  public IStridablesList<IStructInfo> listStructInfoes() {
    return structInfoes;
  }

}

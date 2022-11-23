package ru.toxsoft.l2.core.invokable.impl;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;

import ru.toxsoft.l2.core.invokable.*;

/**
 * Реализация описания переменной из {@link IInvokableInfo#listVarInfoes()}
 *
 * @author MAX
 */
public class VarInfo
    extends Stridable
    implements IVarInfo {

  private boolean isReadOnly;

  private IDataType dataType;

  /**
   * Конструктор описания переменной.
   *
   * @param aId String - идентификатор переменной.
   * @param aDescr String - описание переменной.
   * @param aDataType IDataType - тип переменной.
   * @param aIsReadOnly boolean - признак того, что переменная только для чтения.
   */
  public VarInfo( String aId, String aDescr, IDataType aDataType, boolean aIsReadOnly ) {
    super( aId, aDescr, aDescr );
    isReadOnly = aIsReadOnly;
    dataType = aDataType;
  }

  @Override
  public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override
  public IDataType dataType() {
    return dataType;
  }

}

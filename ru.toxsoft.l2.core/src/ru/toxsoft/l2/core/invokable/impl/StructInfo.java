package ru.toxsoft.l2.core.invokable.impl;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;

import ru.toxsoft.l2.core.invokable.*;

/**
 * Реализация описания структуры из {@link IInvokableInfo#listStructInfoes()}
 *
 * @author MAX
 */
public class StructInfo
    extends Stridable
    implements IStructInfo {

  private IDataDef structType;

  /**
   * Конструктор описания структуры.
   *
   * @param aId String - идентификатор структуры.
   * @param aDescr String - описание структуры.
   * @param aStructType IStructType - описание типа структуры.
   */
  public StructInfo( String aId, String aDescr, IDataDef aStructType ) {
    super( aId, aDescr, aDescr );
    structType = aStructType;
  }

  @Override
  public IDataDef structType() {
    return structType;
  }

}

package org.toxsoft.l2.lib.invokable.impl;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.l2.lib.invokable.*;

/**
 * Реализация описания метода из {@link IInvokableInfo#listMethodInfoes()}
 *
 * @author MAX
 */
public class MethodInfo
    extends Stridable
    implements IMethodInfo {

  private IStringMap<IDataType> argsInfo;

  /**
   * Конструктор описания метода.
   *
   * @param aId String - идентификатор метода.
   * @param aDescr String - описание метода.
   * @param aArgsInfo IStringMap - описание аргументов метода.
   */
  public MethodInfo( String aId, String aDescr, IStringMap<IDataType> aArgsInfo ) {
    super( aId, aDescr, aDescr );
    argsInfo = aArgsInfo;
  }

  @Override
  public IStringMap<IDataType> argsInfo() {
    return argsInfo;
  }

}

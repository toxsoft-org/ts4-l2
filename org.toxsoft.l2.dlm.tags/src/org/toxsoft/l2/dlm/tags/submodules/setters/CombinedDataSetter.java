package org.toxsoft.l2.dlm.tags.submodules.setters;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.l2.dlm.tags.submodules.data.*;

/**
 * Составной источник данных
 *
 * @author max
 */
public class CombinedDataSetter
    implements IDataSetter {

  private IListEdit<IDataSetter> dataSetters = new ElemArrayList<>();

  @Override
  public boolean setDataValue( IAtomicValue aValue, long aTime ) {
    boolean result = false;
    for( IDataSetter setter : dataSetters ) {
      result = result | setter.setDataValue( aValue, aTime );
    }
    return result;
  }

  @Override
  public void sendOnServer() {
    for( IDataSetter setter : dataSetters ) {
      setter.sendOnServer();
    }
  }

  @Override
  public void close() {
    for( IDataSetter setter : dataSetters ) {
      setter.close();
    }
  }

  /**
   * Добавляет новый сеттер в составной источник данных
   *
   * @param aDataSetter IDataSetter - новый сеттер
   */
  public void addDataSetter( IDataSetter aDataSetter ) {
    dataSetters.add( aDataSetter );
  }
}

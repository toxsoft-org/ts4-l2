package org.toxsoft.l2.dlm.tags.submodules.setters;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.dlm.tags.submodules.data.*;

/**
 * Составной источник данных
 *
 * @author max
 */
public class CombinedDataSetter
    implements IGwidValueSetter {

  private IListEdit<IGwidValueSetter> dataSetters = new ElemArrayList<>();

  @Override
  public boolean setGwidValue( IAtomicValue aValue, long aTime ) {
    boolean result = false;
    for( IGwidValueSetter setter : dataSetters ) {
      result = result | setter.setGwidValue( aValue, aTime );
    }
    return result;
  }

  @Override
  public void close() {
    for( IGwidValueSetter setter : dataSetters ) {
      setter.close();
    }
  }

  /**
   * Добавляет новый сеттер в составной источник данных
   *
   * @param aDataSetter IDataSetter - новый сеттер
   */
  public void addDataSetter( IGwidValueSetter aDataSetter ) {
    dataSetters.add( aDataSetter );
  }
}

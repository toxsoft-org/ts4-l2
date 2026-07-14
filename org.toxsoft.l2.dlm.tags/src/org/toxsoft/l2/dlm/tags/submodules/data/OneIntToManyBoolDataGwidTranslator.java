package org.toxsoft.l2.dlm.tags.submodules.data;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Реализация передатчика - один Integer тег на массив Boolean данных.
 *
 * @author max
 */
public class OneIntToManyBoolDataGwidTranslator
    implements IDataGwidTranslator {

  /**
   * Журнал работы
   */
  private static ILogger logger;

  /**
   * Integer Тег.
   */
  protected IL2Tag tag;

  /**
   * Массив текущих значений
   */
  private Boolean[] oldVals;

  /**
   * Массив установщиков значений в дата-сет
   */
  protected IGwidValueSetter[] dataSetters;

  @Override
  public boolean translate( long aTime ) {
    boolean result = false;
    IAtomicValue tagValue = tag.get();

    if( tagValue == null || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    int value = tagValue.asInt();

    for( int i = 0; i < dataSetters.length; i++ ) {
      if( dataSetters[i] != null ) {
        boolean val = ((value >> i) & 1) == 1;

        if( oldVals[i] == null || oldVals[i].booleanValue() != val ) {
          result = result || dataSetters[i].setGwidValue( AvUtils.avBool( val ), aTime );
          oldVals[i] = Boolean.valueOf( val );
        }
      }
    }

    return result;
  }

  @Override
  public void config( IAvTree aParams ) {
    // без реализации

  }

  @Override
  public void start( IGwidValueSetter[] aDataSetindexes, IGwidValueGetter[] aDataGetters, IList<IL2Tag> aTags ) {
    // inDataSetIndex = aDataSetindexes[0];
    tag = aTags.get( 0 );
  }

  protected IL2Tag getTag() {
    return tag;
  }

}

package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация передатчика - один Integer тег на массив Boolean данных.
 *
 * @author max
 * @param <T> - класс дата-сета
 */
public class OneIntToManyBoolTransmitter<T extends ISkRtdataChannel>
    implements IDataTransmitter<T>, ITagable {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( OneIntToManyBoolTransmitter.class.getName() );

  /**
   * Integer Тег.
   */
  protected ITag tag;

  /**
   * Массив текущих значений
   */
  private Boolean[] oldVals;

  /**
   * Массив установщиков значений в дата-сет
   */
  protected IDataSetter[] dataSetters;

  @Override
  public boolean transmit( long aTime ) {
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
          result = result || dataSetters[i].setDataValue( AvUtils.avBool( val ), aTime );
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
  public void start( IDataSetter[] aDataSetindexes, IList<ITag> aTags, IMap<Gwid, T> aWriteDataSet ) {
    // inDataSetIndex = aDataSetindexes[0];
    tag = aTags.get( 0 );
  }

  protected ITag getTag() {
    return tag;
  }

  @Override
  public ITag tag() {
    return tag;
  }

}

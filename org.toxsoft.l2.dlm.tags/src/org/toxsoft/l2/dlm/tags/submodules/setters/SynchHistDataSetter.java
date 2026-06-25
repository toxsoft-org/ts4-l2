package org.toxsoft.l2.dlm.tags.submodules.setters;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Установщик синхронных исторических данных в датасет исторических данных.
 *
 * @author max
 */
public class SynchHistDataSetter
    extends SimpleHistDataSetter {

  private long minWritePeriod = 0;

  protected long prevSetTime = 0;

  private IAtomicValue value;

  public SynchHistDataSetter( IMap<Gwid, ISkWriteHistDataChannel> aDataSet, Gwid aDataGwid, long aMinWritePeriod ) {
    super( aDataSet, aDataGwid );

    // Dima, 04.03.16
    // Чтобы минимизировать пропуски данных уменьшаем период записи в два раза
    // как это работает в приборах ТМ
    minWritePeriod = aMinWritePeriod / 2;
  }

  @Override
  public boolean setDataValue( IAtomicValue aValue, long aTime ) {
    IAtomicValue newVal = aValue;

    boolean result =
        // Dima, 04.03
        (value == null || !newVal.equals( value ) || aTime - prevSetTime > minWritePeriod || aTime < prevSetTime)
            && newVal.isAssigned();

    if( result ) {
      // просто устанавливается значение
      result = result && super.doSetDataValue( newVal, aTime );
      value = newVal;
      prevSetTime = aTime;
    }
    return result;
  }

  @Override
  public void sendOnServer() {
    super.sendOnServer();
  }

}

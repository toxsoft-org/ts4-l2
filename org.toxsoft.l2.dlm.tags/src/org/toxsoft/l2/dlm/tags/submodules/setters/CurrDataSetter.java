package org.toxsoft.l2.dlm.tags.submodules.setters;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.dlm.tags.submodules.data.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Установщик текущих данных в датасет текущих данных.
 *
 * @author max
 */
public class CurrDataSetter
    implements IDataSetter {

  /**
   * Журнал работы
   */
  private ILogger logger;

  private ISkWriteCurrDataChannel channel;

  private IAtomicValue value;

  private Gwid dataGwid;

  private long minWritePeriod = 0;

  protected long prevSetTime = 0;

  public CurrDataSetter( IMap<Gwid, ISkWriteCurrDataChannel> aDataSet, Gwid aDataGwid, long aMinWritePeriod ) {
    TsIllegalArgumentRtException.checkFalse( aDataSet.hasKey( aDataGwid ) );
    channel = aDataSet.getByKey( aDataGwid );
    dataGwid = aDataGwid;

    minWritePeriod = aMinWritePeriod / 2;
  }

  @Override
  public boolean setDataValue( IAtomicValue aValue, long aTime ) {
    if( !aValue.isAssigned() ) {
      return false;
    }
    boolean result = value == null || !value.equals( aValue );

    // 2023.02.01 проверка в случае синхронного данного
    result = result || (minWritePeriod > 0 && (aTime - prevSetTime > minWritePeriod || aTime < prevSetTime));

    // test
    // if( dataGwid.toString().contains( "n2AI_TP1" ) && dataGwid.toString().contains( "currentValue" ) ) {
    // logger.debug( "n2AI_TP1 CV: value: %s", (aValue.isAssigned() ? aValue.asString() : "Not Assigned") );
    // }

    if( result ) {
      // просто устанавливается значение
      channel.setValue( aValue );
      value = aValue;
      prevSetTime = aTime;

      logger.debug( "curr data: %s - change value on: %s", dataGwid.asString(),
          (aValue.isAssigned() ? aValue.asString() : "Not Assigned") );
    }

    return result;
  }

  @Override
  public void sendOnServer() {
    // dima 28.03.25 force re-upload current value
    channel.setValue( value );
  }

  @Override
  public void close() {
    channel.close();
  }

  @Override
  public String toString() {
    return dataGwid.asString();
  }
}

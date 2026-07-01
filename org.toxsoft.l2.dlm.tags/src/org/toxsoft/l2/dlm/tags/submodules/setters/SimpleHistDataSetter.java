package org.toxsoft.l2.dlm.tags.submodules.setters;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.dlm.tags.submodules.data.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Установщик обычных исторических данных в датасет исторических данных.
 *
 * @author max
 */
public class SimpleHistDataSetter
    implements IGwidValueSetter {

  /**
   * Журнал работы
   */
  private static ILogger logger;

  /**
   * Канал записи хранимых данных.
   */
  private ISkWriteHistDataChannel channel;

  /**
   * Набор исторических значений
   */
  private TimedList<ITemporalAtomicValue> values = new TimedList<>();

  /**
   * начало интервала данных
   */
  private long startTime = -1;

  /**
   * ококнчание интервала данных
   */
  private long endTime;

  private Gwid dataGwid;

  private IAtomicValue value;

  /**
   * Метка времени последней записи данных на сервер
   */
  private long lastWriteStamp  = System.currentTimeMillis();
  /**
   * Период между записью данными на сервер
   */
  private long dataWritePeriod = 15000L;

  public SimpleHistDataSetter( IMap<Gwid, ISkWriteHistDataChannel> aDataSet, Gwid aDataGwid ) {
    TsIllegalArgumentRtException.checkFalse( aDataSet.hasKey( aDataGwid ) );
    channel = aDataSet.getByKey( aDataGwid );
    dataGwid = aDataGwid;
  }

  @Override
  public boolean setGwidValue( IAtomicValue aValue, long aTime ) {
    boolean result = aValue.isAssigned() && (value == null || !value.equals( aValue ));

    if( result ) {
      result = result && doSetDataValue( aValue, aTime );
      value = aValue;
    }
    if( checkSendOnServer( aTime ) ) {
      sendOnServer();
    }
    return result;
  }

  private boolean checkSendOnServer( long aCurrTime ) {
    return aCurrTime - lastWriteStamp > dataWritePeriod || aCurrTime < lastWriteStamp;
  }

  protected boolean doSetDataValue( IAtomicValue aValue, long aTime ) {

    long currWriteStamp = aTime;// System.currentTimeMillis();
    TemporalAtomicValue tVal = new TemporalAtomicValue( currWriteStamp, aValue );
    values.add( tVal );
    // обновляем данные по интервалу
    if( startTime < 0 ) {
      startTime = currWriteStamp;
    }
    endTime = currWriteStamp;
    return true;

  }

  protected void sendOnServer() {
    if( values.size() > 0 ) {
      try {
        channel.writeValues( timeInterval(), values );
      }
      catch( Exception e ) {
        logger.error( e, "Set Hist data error: gwid: %s,  error: %s", dataGwid, e.getMessage() );
      }
      // logger.info( "wdChannel.writeValues() it's gwid %s", channel.gwid() );

      startTime = System.currentTimeMillis();
      lastWriteStamp = startTime;
      values = new TimedList<>();
    }
  }

  private ITimeInterval timeInterval() {
    if( startTime < 0 ) {
      return ITimeInterval.NULL;
    }
    return new TimeInterval( startTime, endTime );
  }

  @Override
  public void close() {
    channel.close();
  }

  @Override
  public String toString() {
    return dataGwid.canonicalString();
  }

}

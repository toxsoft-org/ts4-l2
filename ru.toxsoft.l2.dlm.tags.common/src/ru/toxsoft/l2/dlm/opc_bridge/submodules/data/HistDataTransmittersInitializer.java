package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.dlm.opc_bridge.*;

/**
 * Реализация для Исторических данных инициализатора передатчиков данных от железа на сервер, призванный по конфиг
 * информации сопоставить каналу НУ данное системы, наладить эту связь (найти канал и найти данное), зарегистрировать
 * всю необходимую информацию (датасеты)
 *
 * @author max
 */
public class HistDataTransmittersInitializer
    extends AbstractDataTransmittersInitializer<ISkWriteHistDataChannel> {

  /**
   * id набора для записи исторических данных
   */
  @SuppressWarnings( "nls" )
  private String whdSetId = "dlm.opc_bridge.write.hist.dataset.id";

  @Override
  public void addDataConfigParamsForTransmitter( IAvTree aTransConfig )
      throws TsIllegalStateRtException {
    // проверяем, что в конфигурации трансмиттера есть исторические данные
    if( isTransmitterHasHistData( aTransConfig ) ) {
      super.addDataConfigParamsForTransmitter( aTransConfig );
    }
  }

  /**
   * Проверяет необходимость включения данного передатчика в список передатчиков исторических данных.
   *
   * @param aTransConfig IAvTree - конфигурация передатчика.
   * @return boolean - передатчик формирует исторические данные, false - передатчик не формирует исторические данные.
   */
  @SuppressWarnings( "static-method" )
  private boolean isTransmitterHasHistData( IAvTree aTransConfig ) {
    boolean allHave = false;

    // если есть несколько данных
    if( aTransConfig.nodes().hasKey( TRANSMITTER_DATA_ARRAY ) ) {
      allHave = true;
      IAvTree dataTree = aTransConfig.nodes().getByKey( TRANSMITTER_DATA_ARRAY );
      for( int i = 0; i < dataTree.arrayLength(); i++ ) {
        IAvTree dParamsTree = dataTree.arrayElement( i );

        if( dParamsTree.fields().hasValue( IS_HIST ) ) {
          if( dParamsTree.fields().getBool( IS_HIST ) ) {
            return true;
          }
        }
        else {
          allHave = false;
        }

      }
    }

    return !allHave && aTransConfig.fields().hasValue( IS_HIST ) && aTransConfig.fields().getBool( IS_HIST );

  }

  @Override
  protected IMap<Gwid, ISkWriteHistDataChannel> createWriteDataSet( ISkConnection aConnection, IGwidList aGwids ) {
    return aConnection.coreApi().rtdService().createWriteHistDataChannels( aGwids );
  }

  @Override
  protected boolean needsToAddCodToDataSet( DataObjNameExtension aDataObjName ) {

    return aDataObjName.isHist();
  }

  @Override
  protected IDataSetter createSetter( Gwid aDataGwid, DataObjNameExtension aDataObjName,
      IMap<Gwid, ISkWriteHistDataChannel> aDataSet ) {
    boolean synch = aDataObjName.isSynch();
    long synchPeriod = aDataObjName.getSynchPeriod();

    return synch ? new SynchHistDataSetter( aDataSet, aDataGwid, synchPeriod )
        : new SimpleHistDataSetter( aDataSet, aDataGwid );
  }

  /**
   * Установщик обычных исторических данных в датасет исторических данных.
   *
   * @author max
   */
  private static class SimpleHistDataSetter
      implements IDataSetter {

    /**
     * Журнал работы
     */
    private static ILogger logger = LoggerWrapper.getLogger( SimpleHistDataSetter.class.getName() );

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

    public SimpleHistDataSetter( IMap<Gwid, ISkWriteHistDataChannel> aDataSet, Gwid aDataGwid ) {
      super();
      TsIllegalArgumentRtException.checkFalse( aDataSet.hasKey( aDataGwid ) );
      channel = aDataSet.getByKey( aDataGwid );
      dataGwid = aDataGwid;
    }

    @Override
    public boolean setDataValue( IAtomicValue aValue, long aTime ) {

      if( !aValue.equals( IAtomicValue.NULL ) ) {
        long currWriteStamp = System.currentTimeMillis();
        TemporalAtomicValue tVal = new TemporalAtomicValue( currWriteStamp, aValue );
        values.add( tVal );
        // обновляем данные по интервалу
        if( startTime < 0 ) {
          startTime = currWriteStamp;
          endTime = currWriteStamp;
        }
        else {
          endTime = currWriteStamp;
        }
        return true;
      }

      return false;
    }

    @Override
    public void sendOnServer() {
      if( values.size() > 0 ) {
        try {
          channel.writeValues( timeInterval(), values );
        }
        catch( Exception e ) {
          logger.error( e, "Set Hist data error: gwid: %s,  error: %s", dataGwid, e.getMessage() );
        }
        // logger.info( "wdChannel.writeValues() it's gwid %s", channel.gwid() );

        startTime = System.currentTimeMillis();
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

  }

  /**
   * Установщик синхронных исторических данных в датасет исторических данных.
   *
   * @author max
   */
  private static class SynchHistDataSetter
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
        super.setDataValue( aValue, aTime );
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
}

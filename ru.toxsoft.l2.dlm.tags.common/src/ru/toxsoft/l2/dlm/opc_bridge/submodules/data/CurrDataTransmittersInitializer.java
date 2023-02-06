package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.dlm.opc_bridge.*;

/**
 * Реализация для текущих данных инициализатора передатчиков данных от железа на сервер, призванный по конфиг информации
 * сопоставить каналу НУ данное системы, наладить эту связь (найти канал и найти данное), зарегистрировать всю
 * необходимую информацию (датасеты)
 *
 * @author max
 */
public class CurrDataTransmittersInitializer
    extends AbstractDataTransmittersInitializer<ISkWriteCurrDataChannel> {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( CurrDataTransmittersInitializer.class.getName() );

  @Override
  public void addDataConfigParamsForTransmitter( IAvTree aTransConfig )
      throws TsIllegalStateRtException {
    // проверяем, что в конфигурации трансмиттера есть текущие данные
    if( isTransmitterHasCurrData( aTransConfig ) ) {
      super.addDataConfigParamsForTransmitter( aTransConfig );
    }
  }

  /**
   * Проверяет необходимость включения данного передатчика в список передатчиков текущих данных.
   *
   * @param aTransConfig IAvTree - конфигурация передатчика.
   * @return boolean - передатчик формирует текущие данные, false - передатчик не формирует текущие данные.
   */
  @SuppressWarnings( "static-method" )
  private boolean isTransmitterHasCurrData( IAvTree aTransConfig ) {
    boolean allHave = false;

    // если есть несколько данных
    if( aTransConfig.nodes().hasKey( TRANSMITTER_DATA_ARRAY ) ) {
      allHave = true;
      IAvTree dataTree = aTransConfig.nodes().getByKey( TRANSMITTER_DATA_ARRAY );
      for( int i = 0; i < dataTree.arrayLength(); i++ ) {
        IAvTree dParamsTree = dataTree.arrayElement( i );

        if( dParamsTree.fields().hasValue( IS_CURR ) ) {
          if( dParamsTree.fields().getBool( IS_CURR ) ) {
            return true;
          }
        }
        else {
          allHave = false;
        }

      }
    }

    return !allHave && aTransConfig.fields().hasValue( IS_CURR ) && aTransConfig.fields().getBool( IS_CURR );

  }

  @Override
  protected IMap<Gwid, ISkWriteCurrDataChannel> createWriteDataSet( ISkConnection aConnection, IGwidList aGwids ) {
    return aConnection.coreApi().rtdService().createWriteCurrDataChannels( aGwids );
  }

  @Override
  protected boolean needsToAddCodToDataSet( DataObjNameExtension aDataObjName ) {

    return aDataObjName.isCurr();
  }

  @Override
  protected IDataSetter createSetter( Gwid aDataGwid, DataObjNameExtension aDataObjName,
      IMap<Gwid, ISkWriteCurrDataChannel> aDataSet ) {
    boolean synch = aDataObjName.isSynch();
    long synchPeriod = aDataObjName.getSynchPeriod();
    return new CurrDataSetter( aDataSet, aDataGwid, synch ? synchPeriod : 0 );
  }

  /**
   * Установщик текущих данных в датасет текущих данных.
   *
   * @author max
   */
  private static class CurrDataSetter
      implements IDataSetter {

    private ISkWriteCurrDataChannel channel;

    private IAtomicValue value;

    private Gwid dataGwid;

    private long minWritePeriod = 0;

    protected long prevSetTime = 0;

    public CurrDataSetter( IMap<Gwid, ISkWriteCurrDataChannel> aDataSet, Gwid aDataGwid, long aMinWritePeriod ) {
      super();
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
      // реализация не требуется
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

}

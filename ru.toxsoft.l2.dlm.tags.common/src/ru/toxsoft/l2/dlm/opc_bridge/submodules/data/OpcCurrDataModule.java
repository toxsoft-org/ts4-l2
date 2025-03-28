package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.data.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.net.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Модуль работы с текущими данными.
 *
 * @author max
 */
public class OpcCurrDataModule
    extends ConfigurableWorkerModuleBase {

  /**
   * Журнал работы
   */
  ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст..
   */
  IDlmContext context;

  /**
   * Информация о модуле DLM
   */
  private IDlmInfo dlmInfo;

  /**
   * Инициализатор.
   */
  private IDataTransmittersInitializer<ISkWriteCurrDataChannel> initializer;

  /**
   * Набор описаний данных, полученный из конфиг информации.
   */
  private IList<IDataTransmitter<ISkWriteCurrDataChannel>> pinDataTransmitters;

  /**
   * Набор выходных текущих данных.
   */
  IMap<Gwid, IDataSetter> wCurrDataSet;

  /**
   * Признак того, что данные зарегистрированы в сервисе качества
   */
  private volatile boolean qualityRegistered = false;

  /**
   * Входные устройства в аспекте их здоровья.
   */
  private IHealthMeasurable[] healthMeasDevices = {};

  /**
   * Конструктор по DLM контексту
   *
   * @param aContext IDlmContext - контекст.
   * @param aDlmInfo IDlmInfo - информация о DLM
   * @param aInitializer IPinDataInitializer - инициализатор пинов.
   */
  public OpcCurrDataModule( IDlmContext aContext, IDlmInfo aDlmInfo,
      IDataTransmittersInitializer<ISkWriteCurrDataChannel> aInitializer ) {
    dlmInfo = aDlmInfo;
    context = aContext;
    initializer = aInitializer;
  }

  @Override
  protected void doConfigYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {

    IAvTree dataDefs = aConfig.params().nodes().findByKey( DATA_DEFS );

    // наполнение конфигуратора данными (для текущих данных)
    if( dataDefs != null && dataDefs.isArray() ) {
      for( int i = 0; i < dataDefs.arrayLength(); i++ ) {
        // описание одного даннного класса
        IAvTree oneDataDef = dataDefs.arrayElement( i );

        // try {
        // if( oneDataDef.fields().hasValue( IS_CURR ) && oneDataDef.fields().getBool( IS_CURR ) ) {
        initializer.addDataConfigParamsForTransmitter( oneDataDef );
        // }
        // }
        // catch( TsItemNotFoundRtException | TsUnsupportedFeatureRtException | DvTypeCastRtException e ) {
        // LoggerUtils.defaultLogger().error( e );
        // // при наступлении ошибки выйти - конфигурация завершена не была
        // return;
        // }
      }
    }

  }

  @Override
  protected void doStartComponent() {

    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_CURR_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    // IS5Connection connection = context.network().getConnection();
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    // инициализирует с помощью конфигуратора основные сущности (на данном этапе идёт выборка информации с сервера)
    initializer.initialize( context );

    // получение датасета текущих данных на запись
    wCurrDataSet = initializer.getDataSetters();

    // получение объектов, отвечающих за передачу сигнала с единичного пина на единичное данное
    pinDataTransmitters = initializer.getDataTransmitters();

    // test log
    logger.debug( "PinDataTransmitters: %s ", String.valueOf( pinDataTransmitters.size() ) );
    for( IDataTransmitter<ISkWriteCurrDataChannel> transmitter : pinDataTransmitters ) {
      if( transmitter instanceof OneToOneDataTransmitter ) {
        ITag tag = ((OneToOneDataTransmitter)transmitter).getTag();
        IDataSetter dataSet = ((OneToOneDataTransmitter)transmitter).getInDataSetIndex();
        logger.debug( "Tag: %s, Set: %s", tag.id(), dataSet.toString() );
      }
    }

    IList<String> tagsDevicesIds = initializer.getTagsDevices();

    healthMeasDevices = new IHealthMeasurable[tagsDevicesIds.size()];

    for( int i = 0; i < tagsDevicesIds.size(); i++ ) {
      IHealthMeasurable tagsDevice =
          (IHealthMeasurable)context.hal().listSpecificDevices().getByKey( tagsDevicesIds.get( i ) );
      healthMeasDevices[i] = tagsDevice;
    }

    // регистрация слушателя состояния соединеня с целью обозначения данных в сервисе качества
    context.network().getSkConnection().addConnectionListener( connectionListener );

    logger.info( MSG_CURR_DATA_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );
  }

  @Override
  protected void doDoJob() {

    // String tagsSpecDev = "aTransTagsParams.getStr( TAG_DEVICE_ID )";// TODO list
    // IHealthMeasurable tagsDevice = (IHealthMeasurable)context.hal().listSpecificDevices().getByKey( tagsSpecDev );

    if( !isDevicesWell() ) {
      if( qualityRegistered && context.network().getSkConnection().state() == ESkConnState.ACTIVE ) {
        try {
          // удаление данных из сервиса качества
          NetworkUtils.removeDataFromQualityService( context.network().getSkConnection(),
              new GwidList( wCurrDataSet.keys() ) );

          qualityRegistered = false;
        }
        catch( Exception e ) {
          logger.error( e );
        }
      }
      return;
    }

    if( !qualityRegistered && context.network().getSkConnection().state() == ESkConnState.ACTIVE ) {
      try {
        NetworkUtils.addToDataQualityService( context.network().getSkConnection(),
            new GwidList( wCurrDataSet.keys() ) );
        qualityRegistered = true;
      }
      catch( Exception e ) {
        logger.error( e, "Exception during curr data params registering in Quality service" ); //$NON-NLS-1$
      }
    }

    if( !qualityRegistered ) {
      return;
    }

    // текущее время - чтоб у всех данных было одно время
    long currTime = System.currentTimeMillis();

    boolean doCurrWrite = false;
    // выполнение работы каждым передатчиком с проверкой изменения данных
    for( IDataTransmitter<ISkWriteCurrDataChannel> transmitter : pinDataTransmitters ) {
      try {
        doCurrWrite |= transmitter.transmit( currTime );
      }
      catch( Exception e ) {
        logger.error( e.getMessage() );
      }
    }
    // dima 17.01.25 transmit tags health to Uskat server
    GwidList toRemoveQS = new GwidList();
    GwidList toAddQS = new GwidList();
    for( IDataTransmitter<ISkWriteCurrDataChannel> transmitter : pinDataTransmitters ) {
      try {
        if( transmitter instanceof ITagable tagable ) {
          ITag tag = tagable.tag();
          if( tag.health().equals( ETagHealth.JUST_BROKEN ) ) {
            IGwidList gwidList = initializer.tag2GwidList( tag.id() );
            toRemoveQS.addAll( gwidList );
          }
          if( tag.health().equals( ETagHealth.JUST_RECOVERED ) ) {
            IGwidList gwidList = initializer.tag2GwidList( tag.id() );
            toAddQS.addAll( gwidList );
          }
        }
      }
      catch( Exception e ) {
        logger.error( e.getMessage() );
      }
    }
    // notify quality service
    if( !toRemoveQS.isEmpty() ) {
      NetworkUtils.removeDataFromQualityService( context.network().getSkConnection(), toRemoveQS );
    }
    if( !toAddQS.isEmpty() ) {
      NetworkUtils.addToDataQualityService( context.network().getSkConnection(), toAddQS );
    }

    if( doCurrWrite ) {
      // wCurrDataSet.write(); //TODO

      // try {
      // context.network().getSkConnection().coreApi().rtdService().writeCurrValues();
      // }
      // catch( Exception e ) {
      // logger.error( e, "Cant transfer curr data to server" ); //$NON-NLS-1$
      // }
    }

  }

  @Override
  protected boolean doQueryStop() {
    if( qualityRegistered ) {
      try {
        // удаление данных из сервиса качества
        NetworkUtils.removeDataFromQualityService( context.network().getSkConnection(),
            new GwidList( wCurrDataSet.keys() ) );
        qualityRegistered = false;

        // удаление слушателя состояния соединеня с целью обозначения данных в сервисе качества
        context.network().getSkConnection().removeConnectionListener( connectionListener );
      }
      catch( Exception e ) {
        logger.error( e );
      }
    }
    if( wCurrDataSet != null ) {
      for( IDataSetter c : wCurrDataSet.values() ) {
        c.close();
      }
      logger.info( "Curr data channels are closed, size = %d", Integer.valueOf( wCurrDataSet.size() ) ); //$NON-NLS-1$
      wCurrDataSet = null;
    }
    return true;
  }

  private ISkConnectionListener connectionListener = ( aSource, aOldState ) -> {
    switch( aSource.state() ) {
      case ACTIVE:
        // добавление данных в сервис качества
        try {
          NetworkUtils.addToDataQualityService( aSource, new GwidList( wCurrDataSet.keys() ) );
          qualityRegistered = true;
          // dima 28.03.25 re-upload all current data to server uskat
          if( wCurrDataSet != null ) {
            for( IDataSetter c : wCurrDataSet.values() ) {
              c.sendOnServer();
            }
            logger.info( "Curr data channels are forced to re-upload curr values, size = %d", //$NON-NLS-1$
                Integer.valueOf( wCurrDataSet.size() ) );
          }
        }
        catch( Exception e ) {
          logger.error( e, "Exception during curr data params registering in Quality service" ); //$NON-NLS-1$
        }
        break;
      case CLOSED:
        break;
      case INACTIVE:
        break;
      default:
        break;
    }
  };

  private static <P> IMap<Gwid, ISkWriteCurrDataChannel> formEmptyChannelsMap( IMap<Gwid, P> aMapByGwid ) {
    IMapEdit<Gwid, ISkWriteCurrDataChannel> result = new ElemMap<>();

    IList<Gwid> gwids = aMapByGwid.keys();
    for( Gwid g : gwids ) {
      result.put( g, EMPTY_CHANNEL );
    }

    return result;
  }

  /**
   * Возвращает признак, что устройства работают нормально (определяется здоровьем). Если устройства не удаётся найти -
   * считается, что они работают хорошо!
   *
   * @return true - устройства работают нормально, false - иначе.
   */
  private boolean isDevicesWell() {
    for( IHealthMeasurable tagsDevice : healthMeasDevices ) {
      if( tagsDevice.getHealth() < 50 ) {
        return false;
      }
    }

    return true;
  }

  static ISkWriteCurrDataChannel EMPTY_CHANNEL = new EmptySkWriteCurrDataChannel();

  static class EmptySkWriteCurrDataChannel
      implements ISkWriteCurrDataChannel {

    @Override
    public void close() {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean isOk() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public Gwid gwid() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setValue( IAtomicValue aValue ) {
      // TODO Auto-generated method stub

    }
  }
}

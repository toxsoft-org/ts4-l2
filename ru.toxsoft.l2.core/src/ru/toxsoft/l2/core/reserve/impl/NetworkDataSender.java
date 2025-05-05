package ru.toxsoft.l2.core.reserve.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.core.reserve.IReserveHardConstants.*;
import static ru.toxsoft.l2.sysdescr.constants.IL2CoreSysdescrConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.net.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Реализация отправки данных и событий на сервер.
 *
 * @author max
 */
public class NetworkDataSender
    extends WorkerComponentBase
    implements INetworkDataSender, ISkConnectionListener {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст
   */
  private GlobalContext globalContext;

  /**
   * Соединение с сервером.
   */
  private ISkConnection connection;

  /**
   * Набор выходных каналов текущих данных.
   */
  private IMap<Gwid, ISkWriteCurrDataChannel> wCurrDataSet;

  /**
   * Мгновенные значения текущих данных.
   */
  private IMapEdit<Gwid, IAtomicValue> wCurrDataSetValues = new ElemMap<>();

  /**
   * Признак того, что в дата-сетах изменилась информация для передачи на сервер со времени последней передачи
   */
  private boolean dataChanged = false;

  /**
   * Параметры настройки резервирования.
   */
  private final IOptionSet reserveOps;

  /**
   * Состояние (в части резервирования) шкафа.
   */
  private EReserveState prevState = EReserveState.UNKNOWN;

  private String boxClassId;

  private String boxObjName;

  /**
   * Список gwid текущих данных, передаваемых на сервер.
   */
  private GwidList dataGwidsList;

  /**
   * Конструктор по контексту и параметрам резервирования.
   *
   * @param aGlobalContext GlobalContext - контекст.
   * @param aReserveOps IOptionSet - параметры резервирования.
   */
  public NetworkDataSender( GlobalContext aGlobalContext, IOptionSet aReserveOps ) {
    globalContext = aGlobalContext;
    this.reserveOps = aReserveOps;
  }

  @Override
  public void doJob() {
    if( dataChanged ) {
      // long currTime = System.currentTimeMillis();

      for( Gwid gwid : wCurrDataSet.keys() ) {
        if( wCurrDataSetValues.hasKey( gwid ) ) {
          IAtomicValue channelValue = wCurrDataSetValues.removeByKey( gwid );
          wCurrDataSet.getByKey( gwid ).setValue( channelValue );
        }
      }

      // try {
      // connection.coreApi().rtdService().writeCurrValues();
      // }
      // catch( Exception e ) {
      // logger.error( "Cant transfer reserve curr data to server" ); //$NON-NLS-1$
      // }
      // logger.info( "WRITE RESERVE DONE" );
      // wHistDataSet.writeAsync();
      // wHistDataSet.setInterval( new QueryInterval( EQueryIntervalType.CSCE, currTime, TimeUtils.MAX_TIMESTAMP ) );
      dataChanged = false;
    }
  }

  @Override
  protected void doStartComponent() {
    connection = globalContext.network().getSkConnection();
    initReserveDataSets();
  }

  @Override
  protected boolean doQueryStop() {
    connection.removeConnectionListener( this );
    for( ISkWriteCurrDataChannel channel : wCurrDataSet.values() ) {
      channel.close();
    }

    return true;
  }

  @Override
  public void setState( EReserveState aState, String aChangeReason ) {
    setValueToDataset( 0, AvUtils.avInt( aState.dbId() ) );
    setValueToDataset( 4, AvUtils.avBool( aState == EReserveState.MAIN || aState == EReserveState.MAIN_INVALID ) );
    // попытка мгновенно отправить состояние на сервер
    doJob();

    sendEvent( EReserveEvent.STATE_CHANGED, new ElemArrayList<>( AvUtils.avStr( prevState.id() ),
        AvUtils.avStr( aState.id() ), AvUtils.avStr( aChangeReason ) ) );
    prevState = aState;
  }

  @Override
  public void setSick( boolean aSick ) {
    setValueToDataset( 1, avBool( aSick ) );
    setValueToDataset( 5, avBool( !aSick ) );

    sendEvent( EReserveEvent.BECOME_SICK, new ElemArrayList<>( AvUtils.avBool( aSick ) ) );
  }

  @Override
  public void setConnectionBreak( boolean aConnectionBreak ) {
    setValueToDataset( 2, avBool( aConnectionBreak ) );
    sendEvent( EReserveEvent.CONNECTION_BREAK, new ElemArrayList<>( avBool( aConnectionBreak ) ) );
  }

  @Override
  public void setPartnerState( EPartnerBoxReserveState aState ) {
    setValueToDataset( 3, avInt( aState.dbId() ) );
  }

  @Override
  public void sendEvent( EReserveEvent aEventType, IList<IAtomicValue> aParamsValues ) {

    // отправка события с проверкой исключений
    try {
      OptionSet params = new OptionSet();
      StringBuilder paramsForMessage = new StringBuilder();
      IStringList paramIdsList = aEventType.getParamsIds();
      for( int i = 0; i < paramIdsList.size(); i++ ) {
        String pId = paramIdsList.get( i );
        IAtomicValue pVal = aParamsValues.get( i );
        params.setValue( pId, pVal );

        paramsForMessage.append( String.format( ", param:%s=%s", pId, pVal.asString() ) ); //$NON-NLS-1$
      }

      Gwid eventGwid = Gwid.createEvent( boxClassId, boxObjName, aEventType.getId() );

      SkEvent event = new SkEvent( System.currentTimeMillis(), eventGwid, params );

      connection.coreApi().eventService().fireEvent( event );
      String message = String.format( "Event sended: %s", aEventType.getId() ) + paramsForMessage.toString(); //$NON-NLS-1$
      logger.debug( message );
    }
    catch( Exception e ) {
      logger.error( "Reserve event send error: %s", e.getMessage() ); //$NON-NLS-1$
    }
  }

  //
  // -----------------------------------------------------
  // внутренние методы

  /**
   * Инициализирует дата-сет текущих данных - состояния резервирования
   */
  private void initReserveDataSets() {
    try {
      // из настроек
      boxObjName = reserveOps.getStr( L2_RESERVE_BOX_OBJ_NAME );

      // из сгенерированного skide файла
      boxClassId = CLSID_L2_HOTSWAP;

      // из сгенерированного skide файла
      String stateDataId = RTDID_CLSID_L2_HOTSWAP_STATE;
      String sickDataId = RTDID_CLSID_L2_HOTSWAP_SICK;
      String connDataId = RTDID_CLSID_L2_HOTSWAP_CONNECTION;
      String partnerDataId = RTDID_CLSID_L2_HOTSWAP_PARTNER;
      String isMainDataId = RTDID_CLSID_L2_HOTSWAP_IS_MAIN;// "isMain";
      String isOkDataId = RTDID_CLSID_L2_HOTSWAP_IS_OK;// "isOk";

      // Добавлено для иникации переходных процессов
      String transitionMain = RTDID_CLSID_L2_HOTSWAP_TRANSITIONMAIN; // transitionMain
      String transitionReserve = RTDID_CLSID_L2_HOTSWAP_TRANSITIONRESERV; // transitionReserv

      dataGwidsList = new GwidList();

      dataGwidsList.add( Gwid.createRtdata( boxClassId, boxObjName, stateDataId ) );
      dataGwidsList.add( Gwid.createRtdata( boxClassId, boxObjName, sickDataId ) );
      dataGwidsList.add( Gwid.createRtdata( boxClassId, boxObjName, connDataId ) );
      dataGwidsList.add( Gwid.createRtdata( boxClassId, boxObjName, partnerDataId ) );

      dataGwidsList.add( Gwid.createRtdata( boxClassId, boxObjName, isMainDataId ) );
      dataGwidsList.add( Gwid.createRtdata( boxClassId, boxObjName, isOkDataId ) );

      // Добавлено для иникации переходных процессов
      dataGwidsList.add( Gwid.createRtdata( boxClassId, boxObjName, transitionMain ) );
      dataGwidsList.add( Gwid.createRtdata( boxClassId, boxObjName, transitionReserve ) );

      wCurrDataSet = connection.coreApi().rtdService().createWriteCurrDataChannels( dataGwidsList );
      // currDataService().createWriteChannels( dataGwidsList );

      // long time = System.currentTimeMillis();
      // QueryInterval interval = new QueryInterval( EQueryIntervalType.CSCE, time, TimeUtils.MAX_TIMESTAMP );
      // wHistDataSet = connection.coreApi().histDataService().
      // createWriteHistDataSet( whdSetId + time, interval,
      // new ElemArrayList<>( stateDataCod, sickDataCod, connDataCod, partnerDataCod ) );

      // добавление данных в сервис качества
      try {
        // TODO
        NetworkUtils.addToDataQualityService( connection, new GwidList( wCurrDataSet.keys() ) );
      }
      catch( Exception e ) {
        logger.error( e, "Exception during reserve data params registering in Quality service" );
      }

      connection.addConnectionListener( this );

    }
    catch( TsItemNotFoundRtException e ) {
      logger.error( e );
    }
  }

  private void setValueToDataset( int aIndex, IAtomicValue aValue ) {
    Gwid gwid = dataGwidsList.get( aIndex );
    wCurrDataSetValues.put( gwid, aValue );
    dataChanged = true;

    // long currTime = System.currentTimeMillis();
    // wHistDataSet.set( aIndex, new TimedList<>( new TemporalAtomicValue( currTime, aValue ) ) );

  }

  //
  // ------------------------------------------------------------------------
  // Реализация слушателя соединения

  @Override
  public void onSkConnectionStateChanged( ISkConnection aSource, ESkConnState aOldState ) {
    switch( aSource.state() ) {
      case ACTIVE:

        // добавление данных в сервис качества
        try {
          // TODO
          NetworkUtils.addToDataQualityService( aSource, new GwidList( wCurrDataSet.keys() ) );
        }
        catch( Exception e ) {
          logger.error( e, "Exception during reserve data params registering in Quality service" );
        }
        break;
      case CLOSED:
        break;
      case INACTIVE:
        break;
      default:
        break;
    }

  }

  // Для дебага - предыдущее состояние

  private boolean fromReserveToMain = false;
  private boolean fromMainToReserve = false;

  @Override
  public void setFromReserveToMainTransition( boolean aFromReserveToMain ) {
    if( fromReserveToMain != aFromReserveToMain ) {
      setValueToDataset( 6, avBool( aFromReserveToMain ) );
      logger.debug( "curr data ResrveToMain= %s", String.valueOf( aFromReserveToMain ) );
      fromReserveToMain = aFromReserveToMain;
    }

    // попытка мгновенно отправить состояние на сервер
    doJob();
  }

  @Override
  public void setFromMainToReserveTransition( boolean aFromMainToReserve ) {
    if( fromMainToReserve != aFromMainToReserve ) {
      setValueToDataset( 7, avBool( aFromMainToReserve ) );
      logger.debug( "curr data MainToResrve= %s", String.valueOf( aFromMainToReserve ) );
      fromMainToReserve = aFromMainToReserve;
    }

    // попытка мгновенно отправить состояние на сервер
    doJob();
  }

}

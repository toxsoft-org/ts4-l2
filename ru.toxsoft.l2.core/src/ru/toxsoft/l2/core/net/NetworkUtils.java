package ru.toxsoft.l2.core.net;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.skf.dq.lib.ISkDataQualityService;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.rtdserv.ISkWriteCurrDataChannel;
import org.toxsoft.uskat.core.connection.ESkConnState;
import org.toxsoft.uskat.core.connection.ISkConnection;

import ru.toxsoft.l2.core.main.impl.GlobalContext;
import ru.toxsoft.l2.core.net.impl.NetworkImpl;

/**
 * Статические и вспомогательные методы работы с сетью.
 *
 * @author goga
 */
public class NetworkUtils {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( NetworkUtils.class );

  /**
   * Запрет на создание экземпляров.
   */
  private NetworkUtils() {

  }

  /**
   * Создаёт и возвращает компонент работы с сетью по глобальному контексту.
   *
   * @param aGlobalContext - глобальный контекст.
   * @return INetworkComponent - компонент работы с сетью.
   */
  public static INetworkComponent createNetwork( GlobalContext aGlobalContext ) {

    return new NetworkImpl( aGlobalContext );
  }

  /**
   * Добавляет все данные дата-сета в сервис качества для соответствующей сессии (определяемой соединением).
   *
   * @param aConnection ISkConnection - соединение с сервером.
   * @param aCurrDataSet IMap - дата-сет.
   */
  public static void addToDataQualityService( ISkConnection aConnection,
      IMap<Gwid, ISkWriteCurrDataChannel> aCurrDataSet ) {
    // при отсутствии соединения - сообщить и прекратить регистрацию
    if( aConnection.state() != ESkConnState.ACTIVE ) {
      logger.error( "Data params are not added to quality service due to Connection is not connected" );
      return;
    }

    ISkCoreApi clientApi = aConnection.coreApi();
    ISkDataQualityService qService = getDataQualityService( clientApi );

    // при отсутствии сервиса качества - выйти
    if( qService == null ) {

      // ((S5SynchronizedCoreApi)aConnection.coreApi()).addService();

      logger.error( "Data params are not added to quality service due to Quality service is not available" );
      return;
    }

    GwidList resources = new GwidList();
    resources.addAll( aCurrDataSet.keys() );

    // (ISkDataQualityService)clientApi.services().getByKey( ISkDataQualityService.SERVICE_ID );

    // Skid sessionID = aConnection.sessionInfo().skid();

    qService.addConnectedResources( resources );
    logger.info( "IDataQualityService addConnectedResources: size = %d", Integer.valueOf( resources.size() ) );
  }

  /**
   * Удаляет все данные дата-сета из сервиса качества для соответствующей сессии (определяемой соединением).
   *
   * @param aConnection ISkConnection - соединение с сервером.
   * @param aCurrDataSet IMap - дата-сет.
   */
  public static void removeDataFromQualityService( ISkConnection aConnection,
      IMap<Gwid, ISkWriteCurrDataChannel> aCurrDataSet ) {
    // при отсутствии соединения - сообщить и прекратить регистрацию
    if( aConnection.state() != ESkConnState.ACTIVE ) {
      logger.error( "Data params are not removed from quality service due to Connection is not connected" );
      return;
    }

    ISkCoreApi clientApi = aConnection.coreApi();

    ISkDataQualityService qService = getDataQualityService( clientApi );

    // при отсутствии сервиса качества - выйти
    if( qService == null ) {
      logger.error( "Data params are not removed from quality service due to Quality service is not available" );
      return;
    }

    GwidList resources = new GwidList();
    resources.addAll( aCurrDataSet.keys() );

    // (ISkDataQualityService)clientApi.services().getByKey( ISkDataQualityService.SERVICE_ID );

    qService.removeConnectedResources( resources );
    logger.info( "IDataQualityService removeDataFromQualityService: size = %d", Integer.valueOf( resources.size() ) );
  }

  private static ISkDataQualityService getDataQualityService( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    ISkDataQualityService service =
        (ISkDataQualityService)aCoreApi.services().findByKey( ISkDataQualityService.SERVICE_ID );
    return service;
  }

}

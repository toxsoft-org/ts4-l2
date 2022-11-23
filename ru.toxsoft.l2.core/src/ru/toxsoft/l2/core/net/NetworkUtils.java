package ru.toxsoft.l2.core.net;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.net.impl.*;

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
    // при отсутствии сервиса качества - выйти
    // if( !aConnection.coreApi().services().hasKey( ISkDataQualityService.SERVICE_ID ) ) {
    // logger.error( "Data params are not added to quality service due to Quality service is not available" );
    // return;
    // }

    GwidList resources = new GwidList();
    resources.addAll( aCurrDataSet.keys() );

    ISkCoreApi clientApi = aConnection.coreApi();

    // ISkDataQualityService qService =
    // (ISkDataQualityService)clientApi.services().get( ISkDataQualityService.SERVICE_ID );
    //
    // Skid sessionID = aConnection.sessionInfo().skid();
    //
    // qService.addConnectedResources( sessionID, resources );
    logger.info( "IDataQualityService addConnectedResources: size = %d", resources.size() );
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
    // при отсутствии сервиса качества - выйти
    // if( !aConnection.coreApi().services().hasKey( ISkDataQualityService.SERVICE_ID ) ) {
    // logger.error( "Data params are not removed from quality service due to Quality service is not available" );
    // return;
    // }

    GwidList resources = new GwidList();
    resources.addAll( aCurrDataSet.keys() );

    ISkCoreApi clientApi = aConnection.coreApi();

    // ISkDataQualityService qService =
    // (ISkDataQualityService)clientApi.services().get( ISkDataQualityService.SERVICE_ID );
    //
    // Skid sessionID = aConnection.sessionInfo().skid();
    //
    // qService.removeConnectedResources( sessionID, resources );
    logger.info( "IDataQualityService removeDataFromQualityService: size = %d", resources.size() );
  }

}

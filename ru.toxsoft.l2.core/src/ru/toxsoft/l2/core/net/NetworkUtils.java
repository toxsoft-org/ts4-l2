package ru.toxsoft.l2.core.net;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.skf.dq.lib.*;
import org.toxsoft.uskat.core.*;
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
   * @param aConnection {@link ISkConnection } - соединение с сервером.
   * @param aGwidList {@link GwidList} - данные для добавления к сервису качества
   */
  public static void addToDataQualityService( ISkConnection aConnection, GwidList aGwidList ) {
    // при отсутствии соединения - сообщить и прекратить регистрацию
    if( aConnection.state() != ESkConnState.ACTIVE ) {
      logger.error( "Data params are not added to quality service due to Connection is not connected" );
      return;
    }

    ISkCoreApi clientApi = aConnection.coreApi();
    ISkDataQualityService qService = getDataQualityService( clientApi );

    // при отсутствии сервиса качества - выйти
    if( qService == null ) {

      logger.error( "Data params are not added to quality service due to Quality service is not available" );
      return;
    }

    qService.addConnectedResources( aGwidList );
    logger.info( "IDataQualityService addConnectedResources: size = %d", Integer.valueOf( aGwidList.size() ) );
  }

  /**
   * Удаляет все данные дата-сета из сервиса качества для соответствующей сессии (определяемой соединением).
   *
   * @param aConnection ISkConnection - соединение с сервером.
   * @param aGwidList {@link GwidList} - данные для удаления из сервиса качества
   */
  public static void removeDataFromQualityService( ISkConnection aConnection, GwidList aGwidList ) {
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

    qService.removeConnectedResources( aGwidList );
    logger.info( "IDataQualityService removeDataFromQualityService: size = %d", Integer.valueOf( aGwidList.size() ) );
  }

  private static ISkDataQualityService getDataQualityService( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    ISkDataQualityService service =
        (ISkDataQualityService)aCoreApi.services().findByKey( ISkDataQualityService.SERVICE_ID );
    return service;
  }

}

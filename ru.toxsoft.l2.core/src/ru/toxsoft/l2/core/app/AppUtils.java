package ru.toxsoft.l2.core.app;

import java.lang.reflect.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.main.*;
import ru.toxsoft.l2.core.main.impl.*;

/**
 * Статические и вспомогательные методы работы с компонентой проектно-зависимого функционала.
 *
 * @author goga
 */
public class AppUtils {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( AppUtils.class.getName() );

  /**
   * Запрет на создание экземпляров.
   */
  private AppUtils() {

  }

  /**
   * Создаёт и возвращает компонент работы с APP.
   *
   * @param aGlobalContext - глобальный контекст.
   * @return IAppComponent - компонент работы с APP.
   */
  public static IAppComponent createApp( GlobalContext aGlobalContext ) {
    IAppComponent result = createAppImpl( aGlobalContext );
    aGlobalContext.setAppApi( result );
    return result;
  }

  private static IAppComponent createAppImpl( GlobalContext aGlobalContext ) {
    String appClassName = aGlobalContext.globalOps().getStr( EGlobalOps.APP_CLASS_NAME );

    try {
      Class<?> appClass = Class.forName( appClassName );
      Constructor<?> appClassConstructor = appClass.getConstructor( IGlobalContext.class );

      IAppComponent result = (IAppComponent)appClassConstructor.newInstance( aGlobalContext );
      return result;
    }
    catch( Exception e ) {
      logger.error( e );
    }
    return null;
  }
}

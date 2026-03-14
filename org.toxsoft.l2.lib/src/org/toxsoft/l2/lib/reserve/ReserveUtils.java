package org.toxsoft.l2.lib.reserve;

import java.io.*;

import org.toxsoft.l2.lib.main.impl.*;
import org.toxsoft.l2.lib.reserve.impl.*;

/**
 * Статические и вспомогательные методы работы с резервированием.
 *
 * @author max
 */
public class ReserveUtils {

  /**
   * Запрет на создание экземпляров.
   */
  private ReserveUtils() {

  }

  /**
   * Создаёт и возвращает компонент работы с резервированием.
   *
   * @param aGlobalContext - глобальный контекст.
   * @return IHalComponent - компонент работы с резервированием.
   */
  public static IReserveComponent createReserve( GlobalContext aGlobalContext ) {
    if( new File( IReserveHardConstants.L2_RESERVE_CFG_FILE_NAME ).exists() ) {
      return new ReserveImpl2( aGlobalContext );
    }

    return new EmptyReserveImpl( aGlobalContext );
  }

}

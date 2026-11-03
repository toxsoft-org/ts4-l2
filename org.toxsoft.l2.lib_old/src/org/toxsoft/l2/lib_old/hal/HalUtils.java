package org.toxsoft.l2.lib_old.hal;

import org.toxsoft.l2.lib_old.hal.impl.*;
import org.toxsoft.l2.lib_old.main.impl.*;

/**
 * Статические и вспомогательные методы работы с HAL.
 * 
 * @author goga
 */
public class HalUtils {
  
  /**
   * Запрет на создание экземпляров.
   */
  private HalUtils(){
    
  }

  /**
   * Создаёт и возвращает компонент работы с HAL.
   * 
   * @param aGlobalContext - глобальный контекст.
   * @return IHalComponent - компонент работы с HAL.
   */
  public static IHalComponent createHal( GlobalContext aGlobalContext ) {
    return new HalImpl(aGlobalContext);
  }

  
}

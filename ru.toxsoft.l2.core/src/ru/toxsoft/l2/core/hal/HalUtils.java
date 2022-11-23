package ru.toxsoft.l2.core.hal;

import ru.toxsoft.l2.core.hal.impl.HalImpl;
import ru.toxsoft.l2.core.main.impl.GlobalContext;

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

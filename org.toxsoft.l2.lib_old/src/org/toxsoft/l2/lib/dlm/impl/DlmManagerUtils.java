package org.toxsoft.l2.lib.dlm.impl;

import org.toxsoft.l2.lib.dlm.*;
import org.toxsoft.l2.lib.main.impl.*;

/**
 * Статические и вспомогательные методы работы с менеджером загружаемых модулей.
 * 
 * @author goga
 */
public class DlmManagerUtils {

  /**
   * Создает экземпляр менеджера загружаемых модулей.
   * 
   * @param aContext {@link GlobalContext} - глобальный контекст работы компоненты
   * @return {@link IDlmManagerComponent} - созданный объект
   */
  public static IDlmManagerComponent createDlmManager( GlobalContext aContext ) {
    return new DlmManager( aContext );
  }

  /**
   * Запрет создания экзмпляров.
   */
  private DlmManagerUtils() {
    // nop
  }
}

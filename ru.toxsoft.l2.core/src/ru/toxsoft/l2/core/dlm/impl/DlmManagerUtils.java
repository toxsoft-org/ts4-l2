package ru.toxsoft.l2.core.dlm.impl;

import ru.toxsoft.l2.core.dlm.IDlmManagerComponent;
import ru.toxsoft.l2.core.main.impl.GlobalContext;

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

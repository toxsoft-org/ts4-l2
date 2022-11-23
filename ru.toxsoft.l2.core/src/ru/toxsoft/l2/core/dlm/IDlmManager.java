package ru.toxsoft.l2.core.dlm;

import org.toxsoft.core.tslib.coll.*;

/**
 * Менеджер управления загружаемыми модулями.
 *
 * @author goga
 */
public interface IDlmManager {

  /**
   * Находит модуль по его идентификатору.
   *
   * @param aModuleId - идентификатор модуля.
   * @return IDlm - модуль.
   * @throws TsItemNotFoundRtException - в случае отсутствия модуля с таким идентификатором.
   */
  // IDlm findByModuleId( String aModuleId )
  // throws TsItemNotFoundRtException;

  /**
   * Возвращает список загруженных модулей.
   *
   * @return IList - список загруженных модулей.
   */
  IList<IDlm> modules();

}

/**
 *
 */
package ru.toxsoft.l2.thd.opc;

import org.toxsoft.core.tslib.coll.primtypes.*;

/**
 * API стандартного сервера OPC
 *
 * @author Dima
 */
public interface ITsOpc {

  /**
   * Получить тег по идентификатору
   *
   * @param aTagId id тега (полное имя тега на OPC сервере)
   * @return тег или null
   */
  ITag tag( String aTagId );

  /**
   * @return все теги
   */
  IStringMap<ITag> tags();

}

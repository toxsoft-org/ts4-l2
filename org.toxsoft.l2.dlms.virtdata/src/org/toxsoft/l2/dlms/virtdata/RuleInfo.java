/**
 *
 */
package org.toxsoft.l2.dlms.virtdata;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;

/**
 * Контейнер для описания одного правила.<br>
 * пример скрипта: <br>
 * "var isError = !(light0.getValue() && light1.getValue() && light2.getValue())"
 *
 * @author dima
 */
class RuleInfo {

  /**
   * описание правила
   */
  private String definition;

  /**
   * скрипт для выполненния правила
   */
  private String javaScript;

  /**
   * Описание входных параметров
   */
  private IListEdit<ParamInfo> inputParams = new ElemArrayList<>();

  /**
   * Описание вЫходного параметра
   */
  private ParamInfo outParam = null;

  /**
   * Таймаут на установку флага после срабатывания правила
   */
  private int timeout;

  /**
   * Текст сообщения генерируемого при установке ошибки
   */
  private String eventText;

  /**
   * @param aDefinition описание правила
   * @param aInputParams список входных параметров
   * @param aOutParam вЫходной параметр
   * @param aScript текст скрипта
   * @param aTimeout таймаут
   * @param aEventText текст сообщения генерируемого при установке флага
   */
  RuleInfo( String aDefinition, IListEdit<ParamInfo> aInputParams, ParamInfo aOutParam, String aScript, int aTimeout,
      String aEventText ) {
    super();
    definition = aDefinition;
    inputParams = aInputParams;
    outParam = aOutParam;
    javaScript = aScript;
    timeout = aTimeout;
    eventText = aEventText;
  }

  /**
   * @return описание входных параметров правила
   */
  public IListEdit<ParamInfo> getInputParams() {
    return inputParams;
  }

  /**
   * @return описание вЫходного параметра правила
   */
  public ParamInfo getOutParam() {
    return outParam;
  }

  /**
   * @return текст скрипта
   */
  public String getJavaScript() {
    return javaScript;
  }

  /**
   * @return значение таймаута
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * @return поясняющий текст в сообщение об устновке флага
   */
  public String getEventText() {
    return eventText;
  }

  /**
   * @return описание правила
   */
  public String getDefinition() {
    return definition;
  }

}

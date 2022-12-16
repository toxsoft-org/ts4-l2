/**
 *
 */
package org.toxsoft.l2.dlms.virtdata;

import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * Контейнер для описания одного параметра правила
 *
 * @author dima
 */
public class ParamInfo {

  /**
   * Имя параметера в скрипте
   */
  private String name;

  /**
   * parameter's Gwid
   */
  private Gwid gwid;

  /**
   * @param aScriptName название в параметра тексте JavaScript
   * @param aClassId id класса
   * @param aObjName название объекта
   * @param aDataId id данного
   */
  public ParamInfo( String aScriptName, String aClassId, String aObjName, String aDataId ) {
    super();
    name = aScriptName;
    gwid = Gwid.createRtdata( aClassId, aObjName, aDataId );
  }

  /**
   * @return название параметра
   */
  public String getName() {
    return name;
  }

  /**
   * @return class id
   */
  public String getClassId() {
    return gwid.classId();
  }

  /**
   * @return название объекта
   */
  public String getObjName() {
    return gwid.strid();
  }

  /**
   * @return id данного
   */
  public String getDataId() {
    return gwid.propId();

  }

  /**
   * @return Gwid данного
   */
  public Gwid gwid() {
    return gwid;
  }

}

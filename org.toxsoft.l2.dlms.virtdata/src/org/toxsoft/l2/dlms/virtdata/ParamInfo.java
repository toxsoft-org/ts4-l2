/**
 *
 */
package org.toxsoft.l2.dlms.virtdata;

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
   * class id
   */
  private String classId;

  /**
   * название объекта
   */
  private String objName;

  /**
   * data id
   */
  private String dataId;

  /**
   * @param aScriptName название в параметра тексте JavaScript
   * @param aClassId id класса
   * @param aObjName название объекта
   * @param aDataId id данного
   */
  public ParamInfo( String aScriptName, String aClassId, String aObjName, String aDataId ) {
    super();
    name = aScriptName;
    classId = aClassId;
    objName = aObjName;
    dataId = aDataId;
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
    return classId;
  }

  /**
   * @return название объекта
   */
  public String getObjName() {
    return objName;
  }

  /**
   * @return id данного
   */
  public String getDataId() {
    return dataId;
  }

}

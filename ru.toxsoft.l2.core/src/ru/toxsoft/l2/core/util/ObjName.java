package ru.toxsoft.l2.core.util;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Класс и имя объекта.
 *
 * @author Max
 */
public class ObjName {

  private String classId;

  private String objName;

  /**
   * Конструктор по идентфикатору класса и имени объекта.
   *
   * @param aClassId - идентификатор класса.
   * @param aObjName - имя объекта.
   */
  public ObjName( String aClassId, String aObjName ) {
    TsNullArgumentRtException.checkNulls( aClassId, aObjName );
    classId = aClassId;
    objName = aObjName;
  }

  /**
   * Возвращает идентификатор класса.
   *
   * @return String - идентификатор класса.
   */
  public String getClassId() {
    return classId;
  }

  /**
   * Имя объекта.
   *
   * @return String - имя объекта.
   */
  public String getObjName() {
    return objName;
  }
}

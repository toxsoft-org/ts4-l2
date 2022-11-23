package ru.toxsoft.l2.core.util;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Инкапсулирует имя объекта и идентификатор данного - соответствует классу Cod - только вместо идентификатора объекта -
 * имя.
 *
 * @author Max
 */
public class DataObjName
    extends ObjName {

  private String dataId;

  /**
   * Конструктор по классу и имени объекта и данному.
   *
   * @param aClassId - идентификатор класса.
   * @param aObjName - имя объекта.
   * @param aDataId - идентификатор данного.
   */
  public DataObjName( String aClassId, String aObjName, String aDataId ) {
    super( aClassId, aObjName );
    TsNullArgumentRtException.checkNulls( aDataId );
    dataId = aDataId;
  }

  /**
   * Возвращает идентификатор данного.
   *
   * @return String - идентификатор данного.
   */
  public String getDataId() {
    return dataId;
  }

  /**
   * Конвертирует набор ClassId, ObjName, DataId в объект класса Gwid.
   *
   * @return Gwid - объект, идентифицирующий данное.
   */
  public Gwid convertToGwid() {
    return Gwid.createRtdata( getClassId(), getObjName(), getDataId() );
  }

}

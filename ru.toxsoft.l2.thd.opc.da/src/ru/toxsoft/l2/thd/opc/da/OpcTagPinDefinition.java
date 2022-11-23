package ru.toxsoft.l2.thd.opc.da;

import org.toxsoft.core.tslib.av.*;

/**
 * Конфигурационная информация одного тэга OPC.
 *
 * @author dima
 */
class OpcTagPinDefinition {

  /**
   * Полное имя тэга OPC
   */
  private String      tagId;
  /**
   * id пина
   */
  private String      pinId;
  /**
   * Тип значения тэга/пина
   */
  private EAtomicType valType;

  /**
   * Конструктор.
   *
   * @param aPinId String - строковый идентификатор пина.
   * @param aTagId String - имя тэга OPC.
   * @param aValType EAtomicType - тип значения пина.
   */
  public OpcTagPinDefinition( String aPinId, String aTagId, EAtomicType aValType ) {
    super();
    pinId = aPinId;
    tagId = aTagId;
    valType = aValType;
  }

  /**
   * Возвращает тип значения пина.
   *
   * @return {@link EAtomicType} - тип значния.
   */
  public EAtomicType valueType() {
    return valType;
  }

  /**
   * Возвращает идентификатор тэга OPC.
   *
   * @return String - строковый идентифкатор тэга OPC.
   */
  public String tagId() {
    return tagId;
  }

  /**
   * Возвращает идентификатор пина.
   *
   * @return String - строковый идентифкатор пина.
   */
  public String pinId() {
    return pinId;
  }

}

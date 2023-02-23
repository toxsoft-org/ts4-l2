package org.toxsoft.l2.thd.opc.ua.milo;

import org.toxsoft.core.tslib.av.*;

/**
 * Конфигурация тега
 *
 * @author max
 */
public class TagCfgItem {

  private int          namespaceId;
  private IAtomicValue tagId;
  private EAtomicType  tagType;

  private String tagTypeExtra;

  /**
   * Конструктор конфигурации тега по идентификаторм и типу.
   *
   * @param aNamespaceId int - идентификатор пространства имён
   * @param aTagId IAtomicValue - идентификатор тега
   * @param aTagType EAtomicType - тип значения тега
   * @param aTagTypeExtra String - подтип значения тега, по умолчанию - пустое
   */
  public TagCfgItem( int aNamespaceId, IAtomicValue aTagId, EAtomicType aTagType, String aTagTypeExtra ) {
    super();
    namespaceId = aNamespaceId;
    tagId = aTagId;
    tagType = aTagType;

    tagTypeExtra = aTagTypeExtra;
  }

  /**
   * Возвращает идентификатор пространства имён
   *
   * @return int - идентификатор пространства имён
   */
  public int getNamespaceId() {
    return namespaceId;
  }

  /**
   * Возвращает идентификатор тега
   *
   * @return IAtomicValue - идентификатор тега
   */
  public IAtomicValue getTagId() {
    return tagId;
  }

  /**
   * Возвращает тип значения тега
   *
   * @return EAtomicType - тип значения тега
   */
  public EAtomicType getTagType() {
    return tagType;
  }

  /**
   * Возвращает подтип.
   *
   * @return String - подтип.
   */
  public String getTagTypeExtra() {
    return tagTypeExtra;
  }

}

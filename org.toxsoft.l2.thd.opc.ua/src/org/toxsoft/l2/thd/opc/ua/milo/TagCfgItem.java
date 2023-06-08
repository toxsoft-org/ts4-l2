package org.toxsoft.l2.thd.opc.ua.milo;

import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;

/**
 * Конфигурация тега
 *
 * @author max
 */
public class TagCfgItem {

  private NodeId nodeId;

  private int          namespaceId;
  private IAtomicValue tagId;
  private EAtomicType  tagType;

  private String  tagTypeExtra;
  private boolean isControlWord;

  /**
   * Конструктор конфигурации тега по идентификаторм и типу.
   *
   * @param aNamespaceId int - идентификатор пространства имён
   * @param aTagId IAtomicValue - идентификатор тега
   * @param aTagType EAtomicType - тип значения тега
   * @param aTagTypeExtra String - подтип значения тега, по умолчанию - пустое
   * @param aIsControlWord boolean - тег является контрольным словом
   */
  public TagCfgItem( int aNamespaceId, IAtomicValue aTagId, EAtomicType aTagType, String aTagTypeExtra,
      boolean aIsControlWord ) {
    super();
    namespaceId = aNamespaceId;
    tagId = aTagId;
    tagType = aTagType;

    tagTypeExtra = aTagTypeExtra;
    isControlWord = aIsControlWord;
  }

  /**
   * Конструктор конфигурации тега по идентификатору узла и типу.
   *
   * @param aNodeId - идентификатор узла
   * @param aTagType EAtomicType - тип значения тега
   * @param aTagTypeExtra String - подтип значения тега, по умолчанию - пустое
   * @param aIsControlWord boolean - тег является контрольным словом
   */
  public TagCfgItem( NodeId aNodeId, EAtomicType aTagType, String aTagTypeExtra, boolean aIsControlWord ) {
    super();
    nodeId = aNodeId;
    namespaceId = nodeId.getNamespaceIndex().intValue();

    // TODO - это неправильно!!!! может быть int!!! переделать
    tagId = AvUtils.avStr( nodeId.getIdentifier().toString() );

    tagType = aTagType;

    tagTypeExtra = aTagTypeExtra;
    isControlWord = aIsControlWord;
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

  /**
   * Вовзвращает признак того, что тег является контрольным словом (биты формируются независимо)
   *
   * @return boolean true - тег является контрольным словом.
   */
  public boolean isControlWord() {
    return isControlWord;
  }

  /**
   * Возвращает идентификатор узла - может быть null!!!
   *
   * @return NodeId - идентификатор узла - может быть null!!!
   */
  public NodeId getNodeId() {
    return nodeId;
  }

}

package org.toxsoft.l2.thd.opc.ua.milo;

import org.toxsoft.core.tslib.av.*;

/**
 * Конфигурация тега
 *
 * @author max
 */
public class TagCfgItem {

  private int         namespaceId;
  private String      tagId;
  private EAtomicType tagType;

  public TagCfgItem( int aNamespaceId, String aTagId, EAtomicType aTagType ) {
    super();
    namespaceId = aNamespaceId;
    tagId = aTagId;
    tagType = aTagType;
  }

  public int getNamespaceId() {
    return namespaceId;
  }

  public String getTagId() {
    return tagId;
  }

  public EAtomicType getTagType() {
    return tagType;
  }

}

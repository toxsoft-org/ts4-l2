package org.toxsoft.l2.thd.opc.ua.milo;

import org.toxsoft.core.tslib.av.*;

public class TagCfgItem {

  int         namespaceId = 2;
  String      tagId       = "";
  EAtomicType tagType     = EAtomicType.BOOLEAN;

  public TagCfgItem( int aNamespaceId, String aTagId, EAtomicType aTagType ) {
    super();
    namespaceId = aNamespaceId;
    tagId = aTagId;
    tagType = aTagType;
  }

}

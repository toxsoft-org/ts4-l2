package ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Container of complex tags.
 *
 * @author max
 */
public interface IComplexTagsContainer {

  /**
   * Returns all complex tags identifiers.
   *
   * @return IStringList - all complex tags identifiers.
   */
  IStringList complexTagIds();

  /**
   * Return tag with the pointed id.
   *
   * @param aId String - tag identifier.
   * @return IComplexTag - tag.
   * @throws TsNullArgumentRtException argument = <code>null</code>
   * @throws TsItemNotFoundRtException specified id not found
   */
  IComplexTag getComplexTagById( String aId );
}

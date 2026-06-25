package org.toxsoft.l2.dlm.tags.submodules.commands;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * Класс, отвечающий за один тип (идентификатор) комнад, управляющий одним тегом.
 *
 * @author max
 */
public abstract class AbstractOneTagCommandExec
    extends AbstractCommandExec {

  protected IL2Tag tag;

  @Override
  public void start( IStringMap<IL2Tag> aTags, ISkCommandService aCommandStateEditor ) {
    super.start( aTags, aCommandStateEditor );
    tag = aTags.values().get( 0 );
  }

}

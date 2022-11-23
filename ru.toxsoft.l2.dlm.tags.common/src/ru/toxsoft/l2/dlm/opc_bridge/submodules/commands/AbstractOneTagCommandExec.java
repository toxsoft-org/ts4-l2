package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Класс, отвечающий за один тип (идентификатор) комнад, управляющий одним тегом.
 *
 * @author max
 */
public abstract class AbstractOneTagCommandExec
    extends AbstractCommandExec {

  protected ITag tag;

  @Override
  public void start( IStringMap<ITag> aTags, ISkCommandService aCommandStateEditor ) {
    super.start( aTags, aCommandStateEditor );
    tag = aTags.values().get( 0 );
  }

}

package org.toxsoft.l2.dlm.tags.submodules.commands2;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

public class CmdGwidValueGetter
    implements IGwidValueGetter {

  /**
   * Сссылка на очередь команд
   */
  private IQueue<IDtoCommand> queue;

  private Gwid gwid;

  public CmdGwidValueGetter( IQueue<IDtoCommand> aQueue, Gwid aGwid ) {
    queue = aQueue;
    gwid = aGwid;
  }

  @Override
  public IAtomicValue getGwidValue( long aTime ) {
    IDtoCommand cmd = queue.peekHeadOrNull();
    if( cmd == null || !cmd.cmdGwid().equals( gwid ) ) {
      return IAtomicValue.NULL;
    }

    return AvUtils.avStr( cmd.cmdGwid().propId() );
  }

  @Override
  public void close() {
    //
  }

  @Override
  public Gwid gwid() {
    return gwid;
  }

}

package org.toxsoft.l2.dlm.tags.submodules.commands2;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

public class CmdArgGwidValueGetter
    implements IGwidValueGetter {

  /**
   * Сссылка на очередь команд
   */
  private IQueue<IDtoCommand> queue;

  private Gwid argGwid;

  public CmdArgGwidValueGetter( IQueue<IDtoCommand> aQueue, Gwid aArgGwid ) {
    queue = aQueue;
    argGwid = aArgGwid;
  }

  @Override
  public IAtomicValue getGwidValue( long aTime ) {
    IDtoCommand cmd = queue.peekHeadOrNull();
    if( cmd == null ) {
      return IAtomicValue.NULL;
    }
    Gwid cmdGwid = cmd.cmdGwid();
    if( !cmdGwid.skid().equals( argGwid.skid() ) || !cmdGwid.propId().equals( argGwid.propId() ) ) {
      return IAtomicValue.NULL;
    }

    return cmd.argValues().getValue( argGwid.subPropId() );
  }

  @Override
  public void close() {
    //
  }

  @Override
  public Gwid gwid() {
    return argGwid;
  }

}

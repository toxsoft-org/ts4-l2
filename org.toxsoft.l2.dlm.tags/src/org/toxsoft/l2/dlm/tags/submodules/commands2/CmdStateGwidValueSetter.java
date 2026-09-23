package org.toxsoft.l2.dlm.tags.submodules.commands2;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

public class CmdStateGwidValueSetter
    implements IGwidValueSetter {

  /**
   * Сссылка на очередь команд
   */
  private IQueue<IDtoCommand> queue;

  private ISkCommandService cmdService;

  private Gwid gwid;

  public CmdStateGwidValueSetter( IQueue<IDtoCommand> aQueue, ISkCommandService aCmdService, Gwid aGwid ) {
    queue = aQueue;
    cmdService = aCmdService;
    gwid = aGwid;
  }

  @Override
  public boolean setGwidValue( IAtomicValue aValue, long aTime ) {
    DtoCommandStateChangeInfo state = aValue.asValobj();
    if( state != null ) {
      String instanceId = state.instanceId();

      IDtoCommand cmd = queue.peekHeadOrNull();

      if( cmd != null && cmd.instanceId().equals( instanceId ) ) {
        queue.getHeadOrNull();
        cmdService.changeCommandState( state );
        return true;
      }
    }
    return false;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

}

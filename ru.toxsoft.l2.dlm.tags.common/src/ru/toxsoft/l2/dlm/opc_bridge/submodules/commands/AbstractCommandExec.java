package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Класс, отвечающий за один тип (идентификатор) комнад.
 *
 * @author max
 */
public abstract class AbstractCommandExec
    implements ICommandExec {

  protected IStringMap<ITag> tags;

  protected ISkCommandService commandStateEditor;

  protected IDtoCommand currentCmd;

  @Override
  public void config( IAvTree aParams ) {
    // без реализации
  }

  @Override
  public void start( IStringMap<ITag> aTags, ISkCommandService aCommandStateEditor ) {
    tags = aTags;
    commandStateEditor = aCommandStateEditor;
  }

  @Override
  public final void execCommand( IDtoCommand aCmd, long aTime ) {
    TsIllegalStateRtException.checkTrue( currentCmd != null,
        "Command %s with id %s are not completed yet while command (%s) comes",
        currentCmd != null ? currentCmd.cmdGwid() : TsLibUtils.EMPTY_STRING,
        currentCmd != null ? currentCmd.instanceId() : TsLibUtils.EMPTY_STRING, aCmd.instanceId() );

    doExecCommand( aCmd, aTime );
    currentCmd = aCmd;
  }

  @Override
  public abstract void doJob( long aTime );

  protected abstract void doExecCommand( IDtoCommand aCmd, long aTime );

  protected void clearCommand() {
    currentCmd = null;
  }

  @Override
  public boolean isBusy() {
    return currentCmd != null;
  }

}

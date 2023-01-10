package ge.toxsoft.gwp.opcuabridge.server.cmds;

import org.toxsoft.uskat.core.api.cmdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Абстрактная реализация исполнителя команды.
 *
 * @author max
 */
public abstract class AbstractCommandExecuter
    implements ISkCommandExecutor {

  private ITag[] wTags;

  private ITag[] rTags;

  public void job() {
    doJob();
  }

  @Override
  public void executeCommand( IDtoCommand aCmd ) {
    doExecCommand( aCmd );
  }

  abstract void doExecCommand( IDtoCommand aCmd );

  abstract void doJob();
}

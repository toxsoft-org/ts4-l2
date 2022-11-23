package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.IL2Resources.*;

import javax.script.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Исполнитель команды, логика которого программируется в скрипте, расположенном в конфигурационном файле
 *
 * @author max
 */
public class ScriptCommandExec
    extends AbstractCommandExec {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ScriptCommandExec.class );

  private String execCommandScript;

  private String doJobScript = null;

  private String initScript = null;

  private ScriptEngine engine;

  @Override
  public void config( IAvTree aParams ) {

    super.config( aParams );

    execCommandScript = aParams.fields().getStr( CMD_EXEC_SCRIPT );

    if( aParams.fields().hasValue( CMD_DOJOB_SCRIPT ) ) {
      doJobScript = aParams.fields().getStr( CMD_DOJOB_SCRIPT );
    }

    if( aParams.fields().hasValue( CMD_INIT_SCRIPT ) ) {
      initScript = aParams.fields().getStr( CMD_INIT_SCRIPT );
    }
  }

  @Override
  public void start( IStringMap<ITag> aTags, ISkCommandService aCommandStateEditor ) {

    super.start( aTags, aCommandStateEditor );

    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName( JAVA_SCRIPT_NAME );

    // устанавливаем теги

    for( String tagName : aTags.keys() ) {
      engine.put( tagName.replaceAll( DOT, DASH ), aTags.getByKey( tagName ) );
    }

    // инициализационный скрипт
    if( initScript != null ) {
      // текущее время
      engine.put( SCRIPT_CURR_TIME_VAR, Long.valueOf( System.currentTimeMillis() ) );

      try {
        engine.eval( initScript );
      }
      catch( ScriptException ex ) {
        logger.error( ex, ERR_MSG_CMD_INIT_SCRIPT_THREW_EXCEPTION );
        throw new TsIllegalArgumentRtException( ex, ERR_MSG_CMD_INIT_SCRIPT_THREW_EXCEPTION );
      }
    }

  }

  @Override
  protected void doExecCommand( IDtoCommand aCmd, long aTime ) {
    // время прихода команды
    engine.put( SCRIPT_CMD_EXEC_TIME_VAR, Long.valueOf( aTime ) );

    // устанавливаем параметры

    for( String argName : aCmd.argValues().keys() ) {
      engine.put( argName.replaceAll( DOT, DASH ), aCmd.argValues().getValue( argName ) );
    }

    try {
      engine.eval( execCommandScript );
    }
    catch( ScriptException ex ) {
      logger.error( ex, ERR_MSG_EXEC_COMMAND_SCRIPT_THREW_EXCEPTION );
    }
  }

  @Override
  public void doJob( long aTime ) {
    if( doJobScript != null ) {
      // текущее время
      engine.put( SCRIPT_CURR_TIME_VAR, Long.valueOf( aTime ) );
      try {
        engine.eval( doJobScript );
      }
      catch( ScriptException ex ) {
        logger.error( ex, ERR_MSG_DO_JOB_SCRIPT_THREW_EXCEPTION );
      }
    }
  }

  /**
   * Тест скрипта.
   *
   * @param a - параметры запуска.
   */

  // public static void main( String[] a ) {
  // ScriptCommandExec testCommand = new ScriptCommandExec();
  //
  // String argId = "value"; //$NON-NLS-1$
  // String tagId = "tag"; //$NON-NLS-1$
  //
  // testCommand.execCommandScript = String.format( "%s.set(%s);", tagId, argId ); //$NON-NLS-1$
  //
  // ITag tag = new ITag() {
  //
  // @Override
  // public String id() {
  //
  // return null;
  // }
  //
  // @Override
  // public String description() {
  //
  // return null;
  // }
  //
  // @Override
  // public EAtomicType valueType() {
  //
  // return null;
  // }
  //
  // @Override
  // public String tagId() {
  //
  // return null;
  // }
  //
  // @Override
  // public void set( IAtomicValue aVal ) {
  // System.out.println( aVal );
  // }
  //
  // @Override
  // public String name() {
  //
  // return null;
  // }
  //
  // @Override
  // public EKind kind() {
  //
  // return null;
  // }
  //
  // @Override
  // public IAtomicValue get() {
  //
  // return DvUtils.avInt( 5 );
  // }
  // };
  //
  // IStringMapEdit<ITag> tags = new StringMap<>();
  // tags.put( tagId, tag );
  //
  // testCommand.start( tags, null );
  //
  // testCommand.execCommand( new ICommand() {
  //
  // @Override
  // public int compareTo( ICommand aO ) {
  // // TODO Auto-generated method stub
  // return 0;
  // }
  //
  // @Override
  // public long timestamp() {
  // // TODO Auto-generated method stub
  // return 0;
  // }
  //
  // @Override
  // public ITimedList<ITemporalCommandState> statesHistory() {
  // // TODO Auto-generated method stub
  // return null;
  // }
  //
  // @Override
  // public ITemporalCommandState state() {
  // // TODO Auto-generated method stub
  // return null;
  // }
  //
  // @Override
  // public long objId() {
  // // TODO Auto-generated method stub
  // return 0;
  // }
  //
  // @Override
  // public String id() {
  // // TODO Auto-generated method stub
  // return null;
  // }
  //
  // @Override
  // public String cmdId() {
  // // TODO Auto-generated method stub
  // return null;
  // }
  //
  // @Override
  // public long authorObjId() {
  // // TODO Auto-generated method stub
  // return 0;
  // }
  //
  // @Override
  // public IStringMap<IAtomicValue> argValues() {
  // IStringMapEdit<IAtomicValue> values = new StringMap<>();
  // values.put( argId, DvUtils.avStr( "Hellow world" ) ); //$NON-NLS-1$
  // return values;
  // }
  // }, System.currentTimeMillis() );
  //
  // }

}

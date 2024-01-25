package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.IComplexTag.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Команда, подающая на комплексный тег указанное в команде значение и код команды.
 *
 * @author max
 */
public class ValCommandByComplexTagExec
    extends AbstractCommandExec {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ValueCommandExec.class );

  /**
   * Тег отправки кода и значения команды
   */
  protected IComplexTag complexTag;

  /**
   * Идентификатор команды в OPC (из настроек) - передавать в тег cmdIdTag
   */
  private int cmdId;

  /**
   * Идентификатор параметра команды (из настроек) - значение параметра передавать в cmdArgTag
   */
  private String valueParamId;

  /**
   * Начало выполнения текущей команды.
   */
  private long startCmdExec = 0;

  @Override
  public void config( IAvTree aParams ) {

    super.config( aParams );

    cmdId = aParams.fields().getInt( CMD_OPC_ID );
    valueParamId = aParams.fields().hasKey( CMD_VALUE_PARAM_ID ) ? aParams.fields().getStr( CMD_VALUE_PARAM_ID )
        : TsLibUtils.EMPTY_STRING;
  }

  @Override
  public void start( IStringMap<ITag> aTags, ISkCommandService aCommandStateEditor ) {
    super.start( aTags, aCommandStateEditor );

    TsIllegalArgumentRtException.checkFalse( aTags.size() > 0, "min count of tags is 1" ); //$NON-NLS-1$
    complexTag = (IComplexTag)aTags.values().get( 0 );
  }

  @Override
  public boolean isBusy() {
    boolean result = super.isBusy();

    if( result ) {
      return result;
    }

    return complexTag.isBusy();
  }

  @Override
  protected void doExecCommand( IDtoCommand aCmd, long aTime ) {
    IAtomicValue value = null;
    if( valueParamId.length() > 0 && aCmd.argValues().hasValue( valueParamId ) ) {
      value = aCmd.argValues().getValue( valueParamId );
      logger.debug( "Value = %s", value.asString() ); //$NON-NLS-1$
    }

    startCmdExec = complexTag.setValue( cmdId, value );

    logger.debug( "in do exec isBusy = %s", String.valueOf( complexTag.isBusy() ) ); //$NON-NLS-1$
  }

  @Override
  public void doJob( long aTime ) {
    if( currentCmd != null ) {
      logger.debug( "DoJob  currentCmd != null" ); //$NON-NLS-1$
      logger.debug( "in do job isBusy = %s", String.valueOf( complexTag.isBusy() ) ); //$NON-NLS-1$

      if( complexTag.getState( startCmdExec, true ) == EComplexTagState.DONE ) {
        setCmdStateSuccess( currentCmd );
        clearCommand();
        startCmdExec = 0;
      }
    }

  }

  private void setCmdStateSuccess( IDtoCommand aCmd ) {
    SkCommandState state = new SkCommandState( System.currentTimeMillis(), ESkCommandState.SUCCESS );
    DtoCommandStateChangeInfo cmdStateChangeInfo = new DtoCommandStateChangeInfo( aCmd.instanceId(), state );

    try {
      commandStateEditor.changeCommandState( cmdStateChangeInfo );
      logger.debug( "State of command ( %s ) changed  on: %s", aCmd.instanceId(), ESkCommandState.SUCCESS.id() ); //$NON-NLS-1$
    }
    catch( Exception e ) {
      logger.error( "Cant change command ( %s ) state: %s", aCmd.instanceId(), e.getMessage() ); //$NON-NLS-1$
      logger.error( e );
    }

  }

}

package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import java.util.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Команда, подающая на тег указанное в команде значение ИДЕНТИФИКАТОРА команды, а на другой тег значен параметра
 * команды.
 *
 * @author max
 */
public class ValCommandByOneTagWithParamExec
    extends AbstractCommandExec {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ValueCommandExec.class );

  /**
   * Тег отправки кода команды
   */
  protected ITag cmdIdTag;

  /**
   * Тег отправки параметра команды
   */
  protected ITag cmdArgTag;

  /**
   * Тег контроля отправки команды и сброса контроля
   */
  protected ITag cmdFeedbackTag;

  /**
   * Идентификатор команды в OPC (из настроек) - передавать в тег cmdIdTag
   */
  private int cmdId;

  /**
   * Идентификатор параметра команды (из настроек) - значение параметра передавать в cmdArgTag
   */
  private String valueParamId;

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

    TsIllegalArgumentRtException.checkFalse( aTags.size() > 1, "min count of tags is 2" ); //$NON-NLS-1$

    Iterator<ITag> iterator = aTags.iterator();

    cmdIdTag = iterator.next();
    if( aTags.size() > 2 ) {
      cmdArgTag = iterator.next();
    }
    cmdFeedbackTag = iterator.next();

  }

  @Override
  public boolean isBusy() {
    boolean result = super.isBusy();

    if( result ) {
      return result;
    }

    return cmdFeedbackTag.isDirty() || cmdFeedbackTag.get().asInt() != 0;
  }

  @Override
  protected void doExecCommand( IDtoCommand aCmd, long aTime ) {
    cmdIdTag.set( AvUtils.avInt( cmdId ) );
    if( valueParamId.length() > 0 && aCmd.argValues().hasValue( valueParamId ) ) {
      IAtomicValue value = aCmd.argValues().getValue( valueParamId );
      cmdArgTag.set( value );
      logger.debug( "Value = %s", value.asString() ); //$NON-NLS-1$
    }

    logger.debug( "in do exec isDirty = %s", String.valueOf( cmdIdTag.isDirty() ) ); //$NON-NLS-1$
  }

  @Override
  public void doJob( long aTime ) {
    if( currentCmd != null ) {
      logger.debug( "DoJob  currentCmd != null" ); //$NON-NLS-1$
      logger.debug( "in do job isDirty = %s", String.valueOf( cmdIdTag.isDirty() ) ); //$NON-NLS-1$

      if( !cmdIdTag.isDirty() && !cmdFeedbackTag.isDirty() && cmdFeedbackTag.get().asInt() == cmdId ) {
        cmdIdTag.set( AvUtils.avInt( 0 ) );
        setCmdStateSuccess( currentCmd );
        clearCommand();
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

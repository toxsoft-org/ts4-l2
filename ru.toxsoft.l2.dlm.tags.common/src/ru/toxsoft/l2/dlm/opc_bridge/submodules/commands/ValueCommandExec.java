package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

import ru.toxsoft.l2.thd.opc.*;
import ru.toxsoft.l2.thd.opc.da.*;

/**
 * Команда, подающая на тег указанное в команде значение.
 *
 * @author max
 */
public class ValueCommandExec
    extends AbstractOneTagCommandExec {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ValueCommandExec.class );

  private String valueParamId;

  private int bitIndex = -1;

  @Override
  public void start( IStringMap<ITag> aTags, ISkCommandService aCommandStateEditor ) {
    super.start( aTags, aCommandStateEditor );
    TsIllegalArgumentRtException.checkFalse( bitIndex < 0 || (bitIndex >= 0 && tag.valueType() == EAtomicType.INTEGER),
        "bit index is set for tag that is not integer" );
  }

  @Override
  protected void doExecCommand( IDtoCommand aCmd, long aTime ) {
    // Dima, 28.06.16
    // Допускаются команды без аргументов (сброс целочисленных счетчиков)
    IAtomicValue value =
        aCmd.argValues().hasValue( valueParamId ) ? aCmd.argValues().getValue( valueParamId ) : AvUtils.avInt( 0 );

    if( bitIndex > 0 ) {

      int newBitValue = value.asBool() ? 1 : 0;

      IAtomicValue currTagValue = tag.get();
      int currTagValueInt = currTagValue.asInt();
      int currBitValue = (currTagValueInt >> bitIndex) % 2;

      int sign = newBitValue - currBitValue;

      int newTagValueInt = currTagValueInt + sign * (1 << bitIndex);
      value = AvUtils.avInt( newTagValueInt );
    }

    tag.set( value );

    logger.debug( "ValueCmd tag = %s", tag.id() ); //$NON-NLS-1$
    logger.debug( "in do exec isDirty = %s", String.valueOf( ((Tag)tag).isDirty() ) ); //$NON-NLS-1$
  }

  @Override
  public void config( IAvTree aParams ) {

    super.config( aParams );

    valueParamId = aParams.fields().getStr( EVENT_VALUE_PARAM_ID_PARAM_IN_CFG );

    if( aParams.fields().hasValue( BIT_INDEX ) ) {
      bitIndex = aParams.fields().getInt( BIT_INDEX );
    }
  }

  @Override
  public void doJob( long aTime ) {
    if( currentCmd != null ) {
      logger.debug( "DoJob  currentCmd != null" ); //$NON-NLS-1$
      logger.debug( "in do job isDirty = %s", String.valueOf( ((Tag)tag).isDirty() ) ); //$NON-NLS-1$

      if( !((Tag)tag).isDirty() ) {

        setCmdStateSuccess( currentCmd );
        clearCommand();
      }
    }
    // без реализации

  }

  private void setCmdStateSuccess( IDtoCommand aCmd ) {
    SkCommandState state = new SkCommandState( System.currentTimeMillis(), ESkCommandState.SUCCESS );
    DtoCommandStateChangeInfo cmdStateChangeInfo = new DtoCommandStateChangeInfo( aCmd.instanceId(), state );

    try {
      commandStateEditor.changeCommandState( cmdStateChangeInfo );
    }
    catch( Exception e ) {

      logger.error( "Cant change command ( %s ) state: %s", aCmd.instanceId(), e.getMessage() );
      logger.error( e );
    }

  }

}

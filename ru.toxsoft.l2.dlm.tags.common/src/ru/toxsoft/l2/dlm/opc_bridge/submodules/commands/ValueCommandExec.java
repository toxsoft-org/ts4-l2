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

import ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.*;
import ru.toxsoft.l2.thd.opc.*;

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
    logger.debug( "ValueCmd tag = %s", tag.id() ); //$NON-NLS-1$
    setTagBit( tag, bitIndex, value, logger );
  }

  /**
   * dima 25.12.23 вынес в отдельны метод чтобы использовать повторно в коде класса
   * {@link SingleIntToSingleBoolRriDataTransmitter}
   *
   * @param aTag {@link ITag} узел куда пишем новое значение
   * @param aBitIndex индекс значения в слове
   * @param aValue новое значение
   * @param aLogger логгер для вывода отладочной инфы
   */
  public static void setTagBit( ITag aTag, int aBitIndex, IAtomicValue aValue, ILogger aLogger ) {
    if( aBitIndex >= 0 ) {

      int newBitValue = aValue.asBool() ? 1 : 0;

      IAtomicValue currTagValue = aTag.get();
      int currTagValueInt = currTagValue.asInt();

      aLogger.debug( "bitIndex = %d, newBitVal = %d, currTagValue = %d", aBitIndex, newBitValue, currTagValueInt ); //$NON-NLS-1$

      if( currTagValueInt < 0 ) {
        currTagValueInt = Short.toUnsignedInt( (short)currTagValueInt );
        aLogger.debug( "unsigned currTagValue = %d", currTagValueInt );
      }

      int currBitValue = (currTagValueInt >> aBitIndex) % 2;

      int sign = newBitValue - currBitValue;

      int newTagValueInt = currTagValueInt + sign * (1 << aBitIndex);

      aLogger.debug( "currBitValue = %d, sign = %d, newTagValueInt = %d", currBitValue, sign, newTagValueInt ); //$NON-NLS-1$

      aValue = AvUtils.avInt( newTagValueInt );
    }

    aLogger.debug( "Value = %s", aValue.asString() );

    aTag.set( aValue );

    aLogger.debug( "in do exec isDirty = %s", String.valueOf( aTag.isDirty() ) ); //$NON-NLS-1$
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
      logger.debug( "in do job isDirty = %s", String.valueOf( tag.isDirty() ) ); //$NON-NLS-1$

      if( !tag.isDirty() ) {

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
      logger.debug( "State of command ( %s ) changed  on: %s", aCmd.instanceId(), ESkCommandState.SUCCESS.id() );
    }
    catch( Exception e ) {
      logger.error( "Cant change command ( %s ) state: %s", aCmd.instanceId(), e.getMessage() );
      logger.error( e );
    }

  }

  public static void main( String[] a ) {

    short b = -4096;
    System.out.println( b );

    int n = b;
    System.out.println( n );

    short bb = (short)n;
    System.out.println( bb );

    int ui = Short.toUnsignedInt( b );

    System.out.println( ui );

    short ubb = (short)ui;

    System.out.println( ubb );

    System.out.println();

    int cw = 61440;

    for( int i = 0; i < 16; i++ ) {
      int cwc = cw >> i;
      int currBitValue = (cw >> i) % 2;
      System.out.println( String.format( "%d - %d - %d", i, currBitValue, cwc ) );
    }
  }

}

package ru.toxsoft.l2.dlm.opc_bridge.submodules.events;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Простое условие события - изменение значения тега (для float можно указать меру различия).
 *
 * @author max
 */
public class OneTagChangedEventCondition
    implements IOpcTagsCondition {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( OneTagChangedEventCondition.class.getName() );

  private float minChangePercant = 0.1f;

  private IAtomicValue prevValue = null;

  /**
   * Тег, значение которого отслеживается
   */
  private ITag tag;

  @Override
  public void config( IAvTree aParams ) {

    if( aParams.fields().hasValue( CONDITION_CHANGE_PERCANT ) ) {
      minChangePercant = aParams.fields().getFloat( CONDITION_CHANGE_PERCANT );
    }

  }

  @Override
  public void start( IMap<String, ITag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );

    logger.info( "Event Condition OneTagChanged for: '%s' - started", tag.id() );
  }

  @Override
  public boolean isEventCondition( long aTime ) {
    IAtomicValue tagValue = tag.get();

    if( tagValue == null || !tagValue.isAssigned() || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    IAtomicValue value = tagValue;

    EAtomicType type = tagValue.atomicType();
    boolean isHappend = false;
    if( type == EAtomicType.FLOATING ) {
      isHappend = prevValue != null && Math.abs(
          prevValue.asFloat() - value.asFloat() ) > (Math.abs( value.asFloat() ) + Math.abs( prevValue.asFloat() ))
              * minChangePercant / 100f;
    }
    else
      if( type == EAtomicType.INTEGER ) {
        isHappend = prevValue != null && prevValue.asInt() != value.asInt();
      }
      else
        if( type == EAtomicType.BOOLEAN ) {
          isHappend = prevValue != null && prevValue.asBool() != value.asBool();
        }

    if( isHappend ) {
      logger.info( "Event Condition triggered: tag= '%s', old= '%s', new= '%s'", tag.id(), prevValue.asString(),
          value.asString() );
    }

    prevValue = value;
    return isHappend;
  }

}

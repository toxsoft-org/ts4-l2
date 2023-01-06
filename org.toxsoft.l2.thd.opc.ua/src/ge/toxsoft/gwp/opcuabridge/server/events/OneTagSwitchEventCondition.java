package ge.toxsoft.gwp.opcuabridge.server.events;

import static ge.toxsoft.gwp.opcuabridge.server.events.IDlmsBaseConstants.*;
import static ge.toxsoft.gwp.opcuabridge.server.events.IL2Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ge.toxsoft.gwp.opcuabridge.*;

/**
 * Простое условие события - переключение значения одного boolean тега или одного указанного бита int тега.
 *
 * @author max
 */
public class OneTagSwitchEventCondition
    implements IOpcTagsCondition {

  private boolean isOn = false;

  private boolean isOff = false;

  private int bitIndex = -1;

  private Boolean prevValue = null;

  /**
   * Тег, значение которого отслеживается
   */
  private IReadTag tag;

  @Override
  public void config( IAvTree aParams ) {

    if( aParams.fields().hasValue( CONDITION_SWITCH_ON ) ) {
      isOn = aParams.fields().getBool( CONDITION_SWITCH_ON );
    }

    if( aParams.fields().hasValue( CONDITION_SWITCH_OFF ) ) {
      isOff = aParams.fields().getBool( CONDITION_SWITCH_OFF );
    }

    if( aParams.fields().hasValue( BIT_INDEX ) ) {
      bitIndex = aParams.fields().getInt( BIT_INDEX );
    }
  }

  @Override
  public void start( IMap<String, IReadTag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );
    TsIllegalArgumentRtException.checkFalse(
        ((bitIndex >= 0 && tag.type() == EAtomicType.INTEGER) || tag.type() == EAtomicType.BOOLEAN),
        ERR_MSG_IF_TAG_HAS_TYPE_INT_THEN_BIT_INDEX_MUST_BE_SETTED_ELSE_TAG_MUST_HAVE_TYPE_BOOLEAN );
  }

  @Override
  public boolean isEventCondition( long aTime ) {
    IAtomicValue tagValue = tag.getValue();

    if( tagValue == null || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    boolean value = false;

    if( bitIndex >= 0 ) {
      value = ((tagValue.asInt() >> bitIndex) & 1) == 1;
    }
    else {
      value = tagValue.asBool();
    }

    // тег имеет нужное для события значени
    boolean isEvent = (isOn && value) || (isOff && !value);
    if( !isEvent ) {
      return false;
    }

    boolean isHappend = prevValue == null || prevValue.booleanValue() != value;
    prevValue = Boolean.valueOf( value );
    return isHappend;
  }

}

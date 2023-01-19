package ru.toxsoft.l2.dlm.opc_bridge.submodules.events;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.thd.opc.*;

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
  private ITag tag;

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
  public void start( IMap<String, ITag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );
    // проверка что, если bitIndex установлен - тег должен быть Integer
    TsIllegalArgumentRtException.checkTrue( bitIndex >= 0 && tag.valueType() != EAtomicType.INTEGER,
        "if bit index is set, then tag must have type of integer, tagId: %s", tag.tagId() );

    TsIllegalArgumentRtException.checkFalse( tag.valueType() == EAtomicType.INTEGER
        || tag.valueType() == EAtomicType.BOOLEAN || tag.valueType() == EAtomicType.FLOATING,
        "tag must have type of integer, boolean or floating, tagId: %s", tag.tagId() );
  }

  @Override
  public boolean isEventCondition( long aTime ) {
    IAtomicValue tagValue = tag.get();

    if( tagValue == null || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    boolean value = false;

    if( bitIndex >= 0 ) {
      value = ((tagValue.asInt() >> bitIndex) & 1) == 1;
    }
    else {
      if( tagValue.atomicType() == EAtomicType.BOOLEAN ) {
        value = tagValue.asBool();
      }
      else
        if( tagValue.atomicType() == EAtomicType.INTEGER ) {
          value = tagValue.asInt() != 0;
        }
        else
          if( tagValue.atomicType() == EAtomicType.FLOATING ) {
            value = tagValue.asFloat() > 0.0 && tagValue.asFloat() < 0.0;
          }
    }

    // тег имеет нужное для события значени
    boolean isEvent = (isOn && value) || (isOff && !value);
    // if( !isEvent ) {
    // return false;
    // }

    // boolean isHappend = prevValue == null || prevValue.booleanValue() != value;
    boolean isHappend = prevValue != null && prevValue.booleanValue() != value; // Событие действительно произошло, TODO
                                                                                // - залить.
    prevValue = Boolean.valueOf( value );
    return isEvent && isHappend;
  }

}

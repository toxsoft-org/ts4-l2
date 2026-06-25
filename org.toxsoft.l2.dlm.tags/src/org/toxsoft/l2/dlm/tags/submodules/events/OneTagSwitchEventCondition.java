package org.toxsoft.l2.dlm.tags.submodules.events;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Простое условие события - переключение значения одного boolean тега или одного указанного бита int тега.
 *
 * @author max
 */
public abstract class OneTagSwitchEventCondition
    implements IOpcTagsCondition {

  /**
   * Журнал работы
   */
  private ILogger logger;

  private boolean isOn = false;

  private boolean isOff = false;

  private int bitIndex = -1;

  private Boolean prevValue = null;

  /**
   * Для тестирования ложных события TODO - слово целиком.
   */
  private IAtomicValue prevTagValue = IAtomicValue.NULL;

  /**
   * Тег, значение которого отслеживается
   */
  private IL2Tag tag;

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
  public void start( IMap<String, IL2Tag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );
    // проверка что, если bitIndex установлен - тег должен быть Integer
    TsIllegalArgumentRtException.checkTrue( bitIndex >= 0 && tag.dataType().atomicType() != EAtomicType.INTEGER,
        "if bit index is set, then tag must have type of integer, tagId: %s", tag.id() );

    TsIllegalArgumentRtException.checkFalse(
        tag.dataType().atomicType() == EAtomicType.INTEGER || tag.dataType().atomicType() == EAtomicType.BOOLEAN
            || tag.dataType().atomicType() == EAtomicType.FLOATING,
        "tag must have type of integer, boolean or floating, tagId: %s", tag.id() );
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

    // Тестовый код для отлова ошибки ложной генерации событий из слов состояния
    if( bitIndex >= 0 ) {
      // если сложились все условия события - вывести информацию по максимум
      if( isEvent && isHappend ) {
        logger.debug( "!Event: SW= %s, SW_prev=%s, SW_curr=%s, bit=%s, prev=%s, curr=%s ", tag.id(),
            ((prevTagValue != null && prevTagValue.isAssigned()) ? prevTagValue.asString() : "Null"),
            (tagValue.isAssigned() ? tagValue.asString() : "Null"), String.valueOf( bitIndex ),
            String.valueOf( prevValue ), String.valueOf( value ) );
      }
      // запомнить текущее значение слова состояния целиком
      prevTagValue = tagValue;
    }

    prevValue = Boolean.valueOf( value );

    return isEvent && isHappend;
  }

}

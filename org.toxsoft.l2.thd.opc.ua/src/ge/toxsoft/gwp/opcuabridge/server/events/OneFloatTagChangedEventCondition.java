package ge.toxsoft.gwp.opcuabridge.server.events;

import static ge.toxsoft.gwp.opcuabridge.server.events.IDlmsBaseConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Простое условие события - изменение float значения тега.
 *
 * @author max
 */
public class OneFloatTagChangedEventCondition
    implements IOpcTagsCondition {

  private float minChangePercant = 0.1f;

  private Float prevValue = null;

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
    TsIllegalArgumentRtException.checkFalse( (tag.valueType() == EAtomicType.FLOATING), "tag %s must have float type",
        tag.id() );
  }

  @Override
  public boolean isEventCondition( long aTime ) {
    IAtomicValue tagValue = tag.get();

    if( tagValue == null || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    float value = tagValue.asFloat();

    boolean isHappend = prevValue != null
        && Math.abs( prevValue.floatValue() - value ) > Math.abs( ((value * minChangePercant) / 100f) );
    prevValue = Float.valueOf( value );
    return isHappend;
  }

}

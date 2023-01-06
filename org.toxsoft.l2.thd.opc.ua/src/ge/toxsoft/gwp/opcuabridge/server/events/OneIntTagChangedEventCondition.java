package ge.toxsoft.gwp.opcuabridge.server.events;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ge.toxsoft.gwp.opcuabridge.*;

/**
 * Простое условие события - изменение int значения тега.
 *
 * @author max
 */
public class OneIntTagChangedEventCondition
    implements IOpcTagsCondition {

  private Integer prevValue = null;

  /**
   * Тег, значение которого отслеживается
   */
  private IReadTag tag;

  @Override
  public void config( IAvTree aParams ) {

  }

  @Override
  public void start( IMap<String, IReadTag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );
    TsIllegalArgumentRtException.checkFalse( (tag.type() == EAtomicType.INTEGER), "tag %s must have int type",
        tag.id() );
  }

  @Override
  public boolean isEventCondition( long aTime ) {
    IAtomicValue tagValue = tag.getValue();

    if( tagValue == null || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    int value = tagValue.asInt();

    boolean isHappend = prevValue != null && prevValue.intValue() != value;
    prevValue = Integer.valueOf( value );
    return isHappend;
  }

}

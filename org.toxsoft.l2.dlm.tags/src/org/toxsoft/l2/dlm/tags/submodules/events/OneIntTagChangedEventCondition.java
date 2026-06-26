package org.toxsoft.l2.dlm.tags.submodules.events;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.hal.*;

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
  private IL2Tag tag;

  @Override
  public void config( IAvTree aParams ) {
    //
  }

  @Override
  public void start( IMap<String, IL2Tag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );
    TsIllegalArgumentRtException.checkFalse( (tag.dataType().atomicType() == EAtomicType.INTEGER),
        "tag %s must have int type", tag.id() );
  }

  @Override
  public boolean isEventCondition( long aTime ) {
    IAtomicValue tagValue = tag.get();

    if( tagValue == null || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    int value = tagValue.asInt();

    boolean isHappend = prevValue != null && prevValue.intValue() != value;
    prevValue = Integer.valueOf( value );
    return isHappend;
  }

}

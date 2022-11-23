package ru.toxsoft.l2.dlm.opc_bridge.submodules.events;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.events.IL2Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Простое условие события - один тег принимает указанное значение.
 *
 * @author max
 */
public class OneTagEqualesEventCondition
    implements IOpcTagsCondition {

  /**
   * Значение , которое должен принимать тег для фиксации события. Брать из настроек.
   */
  private IAtomicValue value;

  /**
   * Тег, значение которого отслеживается
   */
  private ITag tag;

  @Override
  public void config( IAvTree aParams ) {
    TsIllegalArgumentRtException.checkFalse( aParams.fields().hasValue( CONDITION_VALUE ),
        ERR_MSG_FIELD_VALUE_NOT_SET );

    value = aParams.fields().getValue( CONDITION_VALUE );
  }

  @Override
  public void start( IMap<String, ITag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );

  }

  @Override
  public boolean isEventCondition( long aTime ) {
    IAtomicValue tagValue = tag.get();

    if( tagValue == null || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    return tagValue.compareTo( value ) == 0;
  }

}

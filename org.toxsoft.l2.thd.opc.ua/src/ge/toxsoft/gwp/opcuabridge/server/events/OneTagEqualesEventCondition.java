package ge.toxsoft.gwp.opcuabridge.server.events;

import static ge.toxsoft.gwp.opcuabridge.server.events.IDlmsBaseConstants.*;
import static ge.toxsoft.gwp.opcuabridge.server.events.IL2Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ge.toxsoft.gwp.opcuabridge.*;

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
  private IReadTag tag;

  @Override
  public void config( IAvTree aParams ) {
    TsIllegalArgumentRtException.checkFalse( aParams.fields().hasValue( CONDITION_VALUE ),
        ERR_MSG_FIELD_VALUE_NOT_SET );

    value = aParams.fields().getValue( CONDITION_VALUE );
  }

  @Override
  public void start( IMap<String, IReadTag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );

  }

  @Override
  public boolean isEventCondition( long aTime ) {
    IAtomicValue tagValue = tag.getValue();

    if( tagValue == null || tagValue.equals( IAtomicValue.NULL ) ) {
      return false;
    }

    return tagValue.compareTo( value ) == 0;
  }

}

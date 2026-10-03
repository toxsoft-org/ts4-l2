package org.toxsoft.l2.dlm.tags.submodules.events;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;
import static org.toxsoft.l2.dlm.tags.submodules.events.IL2Resources.*;

import java.util.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Формирователь параметров - простой проброс одного тега в один параметр
 *
 * @author max
 */
public class OneTagToOneParamFormer
    implements IEventParamsFormer {

  /**
   * Тег, значение которого является параметром
   */
  private IL2Tag tag;

  /**
   * Идентификатор параметра.
   */
  private String eventParamId;

  private IGwidValueSetter eventParamSetter;

  private int bitIndex = -1;

  @Override
  public void config( IAvTree aParams ) {
    TsIllegalArgumentRtException.checkFalse( aParams.fields().hasValue( FORMER_EVENT_PARAMS ),
        ERR_MSG_FIELD_VALUE_NOT_SET );

    String paramsStr = aParams.fields().getStr( FORMER_EVENT_PARAMS );

    StringTokenizer st = new StringTokenizer( paramsStr, EVENT_CFG_FIELDS_VALS_DELIM );

    TsIllegalArgumentRtException.checkFalse( st.hasMoreElements(), ERR_MSG_FIELD_HAS_EMPTY_OR_WRONG_VALUE_FORMAT,
        FORMER_EVENT_PARAMS );
    eventParamId = st.nextToken().trim();
    TsIllegalArgumentRtException.checkTrue( st.hasMoreElements(), ERR_MSG_FIELD_HAS_EMPTY_OR_WRONG_VALUE_FORMAT,
        FORMER_EVENT_PARAMS );

    if( aParams.fields().hasValue( BIT_INDEX ) ) {
      bitIndex = aParams.fields().getInt( BIT_INDEX );
    }
  }

  @Override
  public void start( IGwidValueSetter[] aDataSetindexes, IMapEdit<String, IL2Tag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );

    TsIllegalArgumentRtException.checkFalse(
        ((bitIndex >= 0 && tag.atomicType() == EAtomicType.INTEGER) || bitIndex < 0),
        ERR_MSG_IF_BIT_INDEX_IS_SET_THAN_TAG_MUST_HAVE_TYPE_INTEGER );

    for( IGwidValueSetter setter : aDataSetindexes ) {
      if( setter.gwid().kind() == EGwidKind.GW_EVENT_PARAM && setter.gwid().isSubProp() ) {
        if( setter.gwid().subPropId().equals( eventParamId ) ) {
          eventParamSetter = setter;
        }
      }
    }
    TsIllegalArgumentRtException.checkNull( eventParamSetter );
  }

  @Override
  public void formEventParamValues( long aTime ) {
    IAtomicValue tagValue = tag.get();

    if( bitIndex >= 0 ) {
      eventParamSetter.setGwidValue( AvUtils.avBool( ((tagValue.asInt() >> bitIndex) & 1) == 1 ), aTime );
    }
    else {
      eventParamSetter.setGwidValue( tagValue, aTime );
    }
  }

}

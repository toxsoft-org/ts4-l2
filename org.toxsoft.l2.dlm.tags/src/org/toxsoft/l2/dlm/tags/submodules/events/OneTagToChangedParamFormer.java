package org.toxsoft.l2.dlm.tags.submodules.events;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;
import static org.toxsoft.l2.dlm.tags.submodules.events.IL2Resources.*;

import java.util.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Формирователь параметров - текущего и предыдущего значения тега
 *
 * @author max
 */
public class OneTagToChangedParamFormer
    implements IEventParamsFormer {

  /**
   * Тег, значения которого является параметром
   */
  private IL2Tag tag;

  /**
   * Идентификатор параметра - предыдущее значение.
   */
  private String prevValueEventParamId;

  private IGwidValueSetter prevValueSetter;

  /**
   * Идентификатор параметра - текущее значение.
   */
  private String currValueEventParamId;

  private IGwidValueSetter currValueSetter;

  private IAtomicValue prevValue = IAtomicValue.NULL;

  @Override
  public void config( IAvTree aParams ) {
    TsIllegalArgumentRtException.checkFalse( aParams.fields().hasValue( FORMER_EVENT_PARAMS ),
        ERR_MSG_FIELD_VALUE_NOT_SET );

    String paramsStr = aParams.fields().getStr( FORMER_EVENT_PARAMS );

    StringTokenizer st = new StringTokenizer( paramsStr, EVENT_CFG_FIELDS_VALS_DELIM );

    TsIllegalArgumentRtException.checkFalse( st.hasMoreElements(), ERR_MSG_FIELD_HAS_EMPTY_OR_WRONG_VALUE_FORMAT,
        FORMER_EVENT_PARAMS );
    prevValueEventParamId = st.nextToken().trim();
    TsIllegalArgumentRtException.checkFalse( st.hasMoreElements(), ERR_MSG_FIELD_HAS_EMPTY_OR_WRONG_VALUE_FORMAT,
        FORMER_EVENT_PARAMS );
    currValueEventParamId = st.nextToken().trim();
    TsIllegalArgumentRtException.checkTrue( st.hasMoreElements(), ERR_MSG_FIELD_HAS_EMPTY_OR_WRONG_VALUE_FORMAT,
        FORMER_EVENT_PARAMS );
  }

  @Override
  public void start( IGwidValueSetter[] aDataSetindexes, IMapEdit<String, IL2Tag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );
    prevValue = tag.get();

    for( IGwidValueSetter setter : aDataSetindexes ) {
      if( setter.gwid().kind() == EGwidKind.GW_EVENT_PARAM && setter.gwid().isSubProp() ) {
        if( setter.gwid().subPropId().equals( prevValueEventParamId ) ) {
          prevValueSetter = setter;
        }
        if( setter.gwid().subPropId().equals( currValueEventParamId ) ) {
          currValueSetter = setter;
        }
      }
    }
    TsIllegalArgumentRtException.checkNulls( prevValueSetter, currValueSetter );
  }

  @Override
  public void formEventParamValues( long aTime ) {
    IAtomicValue tagValue = tag.get();

    prevValueSetter.setGwidValue( prevValue, aTime );
    currValueSetter.setGwidValue( tagValue, aTime );

    prevValue = tagValue;
  }

}

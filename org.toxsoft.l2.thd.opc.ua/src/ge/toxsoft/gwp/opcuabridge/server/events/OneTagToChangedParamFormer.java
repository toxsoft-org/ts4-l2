package ge.toxsoft.gwp.opcuabridge.server.events;

import static ge.toxsoft.gwp.opcuabridge.server.events.IDlmsBaseConstants.*;
import static ge.toxsoft.gwp.opcuabridge.server.events.IL2Resources.*;

import java.util.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ge.toxsoft.gwp.opcuabridge.*;

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
  private IReadTag tag;

  /**
   * Идентификатор параметра - предыдущее значение.
   */
  private String prevValueEventParamId;

  /**
   * Идентификатор параметра - текущее значение.
   */
  private String currValueEventParamId;

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
  public void start( IMap<String, IReadTag> aTags ) {
    TsNullArgumentRtException.checkNull( aTags );
    TsIllegalArgumentRtException.checkFalse( aTags.size() == 1 );

    tag = aTags.values().get( 0 );
    prevValue = tag.getValue();
  }

  @Override
  public IStringMap<IAtomicValue> getEventParamValues( long aTime ) {
    IAtomicValue tagValue = tag.getValue();

    IStringMapEdit<IAtomicValue> result = new StringMap<>();
    result.put( prevValueEventParamId, prevValue );
    result.put( currValueEventParamId, tagValue );

    prevValue = tagValue;

    return result;
  }

}

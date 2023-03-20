package ru.toxsoft.l2.dlm.opc_bridge.submodules.events;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.events.IL2Resources.*;

import java.util.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Класс формирования и отправки события на сервер, работающий в составе opc моста
 *
 * @author max
 */
public class OpcTagsEventSender
    implements IEventSender {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( OpcTagsEventSender.class );

  /**
   * Соединение с сервером.
   */
  ISkConnection connection;

  // определяются
  private IMapEdit<String, TagConfig> tagsConfig = new ElemMap<>();

  private IMapEdit<String, ITag> tags = new ElemMap<>();

  private IMapEdit<String, IOpcTagsCondition> conditions = new ElemMap<>();

  private IMapEdit<String, IEventParamsFormer> formers = new ElemMap<>();

  private Gwid evenGwid;

  
  private boolean isStarted = false;

  @Override
  public void sendEvent( long aTime ) {
    if( !isStarted ) {
      return;
    }
    for( IOpcTagsCondition condition : conditions.values() ) {
      if( !condition.isEventCondition( aTime ) ) {
        return;
      }
    }
    fireEvent( aTime );
  }

  private void fireEvent( long aTime ) {
    // IStringMapEdit<IAtomicValue> paramValues = new StringMap<>();
    IOptionSetEdit paramValues = new OptionSet();
    try {
      for( IEventParamsFormer paramsFormer : formers.values() ) {
        IStringMap<IAtomicValue> params = paramsFormer.getEventParamValues( aTime );
        paramValues.putAll( params );
      }
    }
    catch( TsException ex ) {
      logger.error( ex, ERR_MSG_EVENT_CANT_BE_SENT_DUE_TO_ERROR_FORMAT, evenGwid.asString() );
      return;
    }
    SkEvent ev = new SkEvent( aTime, evenGwid, paramValues );
    connection.coreApi().eventService().fireEvent( ev );// TODO

    logger.debug( "Event sent: %s", evenGwid.asString() );
    if( paramValues.hasKey( "oldVal" ) && paramValues.hasKey( "newVal" ) ) {
      IAtomicValue oldVal = paramValues.getByKey( "oldVal" );
      IAtomicValue newVal = paramValues.getByKey( "newVal" );
      StringBuilder mes = new StringBuilder();
      if( oldVal != IAtomicValue.NULL ) {
        mes.append( "old=" ).append( oldVal.atomicType() == EAtomicType.FLOATING ? oldVal.asFloat() : oldVal.asInt() );
      }
      if( newVal != IAtomicValue.NULL ) {
        mes.append( ", new=" )
            .append( newVal.atomicType() == EAtomicType.FLOATING ? newVal.asFloat() : newVal.asInt() );
      }
      logger.debug( mes.toString() );
    }
  }

  @Override
  public void config( IAvTree aParams ) {

    String classId = aParams.fields().getStr( CLASS_ID );
    String objName = aParams.fields().getStr( OBJ_NAME );
    String eventId = aParams.fields().getStr( EVENT_ID );
    
    evenGwid = Gwid.createEvent( classId, objName, eventId );

    // начнём с тегов

    // если есть несколько тегов
    if( aParams.nodes().hasKey( EVENT_TAGS_ARRAY ) ) {
      IAvTree tagsTree = aParams.nodes().getByKey( EVENT_TAGS_ARRAY );
      for( int i = 0; i < tagsTree.arrayLength(); i++ ) {
        IAvTree tagParamsTree = tagsTree.arrayElement( i );
        try {
          TagConfig tagConf = createTagConfig( tagParamsTree, aParams );
          tagConf.setCfgId( tagParamsTree.structId() );
          tagsConfig.put( tagConf.getCfgId(), tagConf );
        }
        catch( TsIllegalArgumentRtException e ) {
          throw new TsIllegalArgumentRtException( e, ERR_MSG_DURING_CONFIG_EVENT_SENDER_FORMAT, aParams.structId() );
        }

      }
    }
    // если один тег - использовать корневой
    else {
      TagConfig tagConf = createTagConfig( aParams, aParams );
      tagConf.setCfgId( DEFAULT_TAG_ID );
      tagsConfig.put( tagConf.getCfgId(), tagConf );
    }

    // условия

    // если есть несколько условий
    if( aParams.nodes().hasKey( String.format( CONDITION_PARAMS_FORMAT, Integer.valueOf( 1 ) ) ) ) {

      for( int i = 1;; i++ ) {
        if( aParams.nodes().hasKey( String.format( CONDITION_PARAMS_FORMAT, Integer.valueOf( i ) ) ) ) {
          IAvTree condTree = aParams.nodes().getByKey( String.format( CONDITION_PARAMS_FORMAT, Integer.valueOf( i ) ) );
          IOpcTagsCondition condition = createOpcTagsCondition( condTree, aParams );
          condition.config( condTree );

          conditions.put( condTree.structId(), condition );

        }
        else {
          break;
        }
      }
    }
    // если одно условие - использовать корневой
    else {
      IOpcTagsCondition condition = createOpcTagsCondition( aParams, aParams );
      condition.config( aParams );

      conditions.put( DEFAULT_ID, condition );

    }

    // если есть несколько формирователей параметров
    if( aParams.nodes().hasKey( String.format( PARAMS_FORMER_PARAMS_FORMAT, Integer.valueOf( 1 ) ) ) ) {

      for( int i = 1;; i++ ) {
        if( aParams.nodes().hasKey( String.format( PARAMS_FORMER_PARAMS_FORMAT, Integer.valueOf( i ) ) ) ) {
          IAvTree formerTree =
              aParams.nodes().getByKey( String.format( PARAMS_FORMER_PARAMS_FORMAT, Integer.valueOf( i ) ) );
          IEventParamsFormer former = createEventParamsFormer( formerTree, aParams );
          former.config( formerTree );

          formers.put( formerTree.structId(), former );

        }
        else {
          break;
        }
      }
    }
    // если один формирователь параметров - использовать корневой
    else
      if( aParams.fields().hasValue( PARAM_FORMER_JAVA_CLASS ) ) {
        IEventParamsFormer former = createEventParamsFormer( aParams, aParams );
        former.config( aParams );

        formers.put( DEFAULT_ID, former );

      }
  }

  /**
   * Создаёт объект условеи для события
   *
   * @param aConfigParams
   * @param aDefaultConfigParams
   * @return
   */
  @SuppressWarnings( "unchecked" )
  private IOpcTagsCondition createOpcTagsCondition( IAvTree aConfigParams, IAvTree aDefaultConfigParams ) {

    // класс условия
    String condJavaClass = getConfigParamField( CONDITION_JAVA_CLASS, aConfigParams, aDefaultConfigParams, null );

    try {
      Class<IOpcTagsCondition> condClass = (Class<IOpcTagsCondition>)Class.forName( condJavaClass );

      IOpcTagsCondition condition = condClass.newInstance();
      condition.config( aConfigParams );

      return condition;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_CONDITION_FORMAT, condJavaClass );
    }

  }

  /**
   * Создаёт объект формирователь параметров события
   *
   * @param aConfigParams
   * @param aDefaultConfigParams
   * @return
   */
  @SuppressWarnings( "unchecked" )
  private IEventParamsFormer createEventParamsFormer( IAvTree aConfigParams, IAvTree aDefaultConfigParams ) {

    // класс формирователя
    String formerJavaClassStr =
        getConfigParamField( PARAM_FORMER_JAVA_CLASS, aConfigParams, aDefaultConfigParams, null );

    try {
      Class<IEventParamsFormer> formerJavaClass = (Class<IEventParamsFormer>)Class.forName( formerJavaClassStr );

      IEventParamsFormer former = formerJavaClass.newInstance();
      former.config( aConfigParams );

      return former;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_CONDITION_FORMAT, formerJavaClassStr );
    }

  }

  @Override
  public void start( IDlmContext aContext ) {
    // соединение
    connection = aContext.network().getSkConnection();

    String clasId = evenGwid.classId();
    String objId = evenGwid.strid();
    String evntId = evenGwid.propId();

    ISkClassInfo classInfo = connection.coreApi().sysdescr().findClassInfo( clasId );

    if( classInfo == null ) {
      logger.error( "Class '%s' not found during event '%s' sender registration", clasId, evntId );
      return;
    }

    if( !classInfo.events().list().hasKey( evntId ) ) {
      logger.error( "Event '%s' of class '%s' not found during event sender registration", evntId, clasId );
      return;
    }

    if( connection.coreApi().objService().find( new Skid( clasId, objId ) ) == null ) {
      logger.error( "Object '%s' of class '%s' not found during event '%s' sender registration", objId, clasId,
          evntId );
      return;
    }

    // objId = objNameObject.convert( ); //TODO

    // найти все теги
    for( String tagKey : tagsConfig.keys() ) {
      TagConfig tc = tagsConfig.getByKey( tagKey );

      ITsOpc tagsDevice = (ITsOpc)aContext.hal().listSpecificDevices().getByKey( tc.getDeviceId() );
      ITag tag = tagsDevice.tag( tc.getTagId() );

      if( tag == null ) {
        logger.error( "Tag '%s' not found", tc.getDeviceId() + " | " + tc.getTagId() );
      }
      else {
        tags.put( tagKey, tag );
      }
    }

    if( tags.size() != tagsConfig.size() ) {
      return;
    }

    // условия
    for( String condKey : conditions.keys() ) {
      IOpcTagsCondition condition = conditions.getByKey( condKey );

      IMapEdit<String, ITag> condTags = new ElemMap<>();

      for( String tagKey : tagsConfig.keys() ) {
        TagConfig tc = tagsConfig.getByKey( tagKey );
        if( tc.isConditionInList( condKey ) || tagKey.equals( DEFAULT_TAG_ID ) ) {
          condTags.put( tagKey, tags.getByKey( tagKey ) );
        }
      }

      condition.start( condTags );
    }

    // формирователи параметров
    for( String formerKey : formers.keys() ) {
      IEventParamsFormer former = formers.getByKey( formerKey );

      IMapEdit<String, ITag> formerTags = new ElemMap<>();

      for( String tagKey : tagsConfig.keys() ) {
        TagConfig tc = tagsConfig.getByKey( tagKey );
        if( tc.isParamFormerInList( formerKey ) || tagKey.equals( DEFAULT_TAG_ID ) ) {
          formerTags.put( tagKey, tags.getByKey( tagKey ) );
        }
      }

      former.start( formerTags );
    }

    logger.debug( "Event sender for: '%s' - started", evenGwid.asString() );
    isStarted = true;
  }

  private TagConfig createTagConfig( IAvTree aTagParams, IAvTree aDefaultTagParams ) {
    TagConfig result = new TagConfig();

    result.setTagId( getConfigParamField( TAG_ID, aTagParams, aDefaultTagParams, null ) );
    result.setDeviceId( getConfigParamField( TAG_DEVICE_ID, aTagParams, aDefaultTagParams, null ) );
    result.setConditionsList( getConfigParamField( EVENT_TAG_CONDITIONS, aTagParams, aDefaultTagParams, "" ), //$NON-NLS-1$
        EVENT_CFG_FIELDS_VALS_DELIM );
    result.setParamFormers( getConfigParamField( EVENT_TAG_PARAM_FORMERS, aTagParams, aDefaultTagParams, "" ), //$NON-NLS-1$
        EVENT_CFG_FIELDS_VALS_DELIM );

    return result;
  }

  String getConfigParamField( String aFieldName, IAvTree aParams, IAvTree aDefaultParams, String aDefault ) {
    if( aParams.fields().hasValue( aFieldName ) ) {
      return aParams.fields().getStr( aFieldName );
    }

    if( aDefaultParams.fields().hasValue( aFieldName ) ) {
      return aDefaultParams.fields().getStr( aFieldName );
    }

    if( aDefault != null ) {
      return aDefault;
    }

    throw new TsIllegalArgumentRtException( ERR_MSG_FIELD_IS_NOT_PRESENTED_IN_CFG_FILE_FORMAT, aFieldName );

  }

  static class TagConfig {

    private String cfgId;

    private String deviceId;

    private String tagId;

    private IListEdit<String> conditionsList = new ElemArrayList<>();

    private IListEdit<String> paramFormersList = new ElemArrayList<>();

    public String getCfgId() {
      return cfgId;
    }

    public void setCfgId( String aCfgId ) {
      cfgId = aCfgId;
    }

    public String getDeviceId() {
      return deviceId;
    }

    public void setDeviceId( String aDeviceId ) {
      deviceId = aDeviceId;
    }

    public String getTagId() {
      return tagId;
    }

    public void setTagId( String aTagId ) {
      tagId = aTagId;
    }

    public boolean isConditionInList( String aCondition ) {
      return conditionsList.hasElem( aCondition );
    }

    public void setConditionsList( String aConditionsListStr, String aDelim ) {
      conditionsList.clear();
      StringTokenizer st = new StringTokenizer( aConditionsListStr, aDelim );

      while( st.hasMoreElements() ) {
        conditionsList.add( st.nextToken().trim() );
      }

    }

    public boolean isParamFormerInList( String aParamFormer ) {
      return paramFormersList.hasElem( aParamFormer );
    }

    public void setParamFormers( String aParamFormersListStr, String aDelim ) {
      paramFormersList.clear();
      StringTokenizer st = new StringTokenizer( aParamFormersListStr, aDelim );

      while( st.hasMoreElements() ) {
        paramFormersList.add( st.nextToken().trim() );
      }
    }

  }

}

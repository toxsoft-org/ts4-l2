package ge.toxsoft.gwp.opcuabridge.server.events;

import static ge.toxsoft.gwp.opcuabridge.server.events.IDlmsBaseConstants.*;
import static ge.toxsoft.gwp.opcuabridge.server.events.IL2Resources.*;

import java.util.*;

import javax.script.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.thd.opc.ua.milo.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Формирователь параметров события, логика которого программируется в скрипте, расположенном в конфигурационном файле.
 *
 * @author MAX
 */
public class ScriptParamFormer
    implements IEventParamsFormer {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ScriptParamFormer.class );

  private String paramFormerScript;

  private String initScript = null;

  private ScriptEngine engine;

  private IStringListEdit eventParamsIds = new StringArrayList();

  @Override
  public void config( IAvTree aParams ) {
    paramFormerScript = aParams.fields().getStr( EVENT_PARAM_FORMER_SCRIPT );

    if( aParams.fields().hasValue( EVENT_INIT_PARAM_FORMER_SCRIPT ) ) {
      initScript = aParams.fields().getStr( EVENT_INIT_PARAM_FORMER_SCRIPT );
    }

    String paramsStr = aParams.fields().getStr( FORMER_EVENT_PARAMS );

    StringTokenizer st = new StringTokenizer( paramsStr, EVENT_CFG_FIELDS_VALS_DELIM );

    while( st.hasMoreElements() ) {
      eventParamsIds.add( st.nextToken().trim() );
    }
  }

  @Override
  public void start( IMap<String, ITag> aTags ) {
    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName( JAVA_SCRIPT_NAME );

    // устанавливаем теги

    for( String tagName : aTags.keys() ) {
      engine.put( replaceDotByDash( tagName ), aTags.getByKey( tagName ) );
    }

    // инициализационный скрипт
    if( initScript != null ) {
      // текущее время
      engine.put( SCRIPT_CURR_TIME_VAR, Long.valueOf( System.currentTimeMillis() ) );

      try {
        engine.eval( initScript );
      }
      catch( ScriptException ex ) {
        logger.error( ex, ERR_MSG_EVENT_INIT_SCRIPT_THREW_EXCEPTION );
        throw new TsIllegalArgumentRtException( ex, ERR_MSG_EVENT_INIT_SCRIPT_THREW_EXCEPTION );
      }
    }
  }

  @Override
  public IStringMap<IAtomicValue> getEventParamValues( long aTime )
      throws TsException {

    IStringMapEdit<IAtomicValue> result = new StringMap<>();

    // текущее время
    engine.put( SCRIPT_CURR_TIME_VAR, Long.valueOf( aTime ) );
    try {
      engine.eval( paramFormerScript );
    }
    catch( ScriptException ex ) {
      logger.error( ex, ERR_MSG_PARAM_FORMER_SCRIPT_THREW_EXCEPTION );
    }

    for( String paramId : eventParamsIds ) {
      IAtomicValue paramValue = (IAtomicValue)engine.get( replaceDotByDash( paramId ) );
      TsIllegalStateRtException.checkFalse( paramValue != null, ERR_MSG_PARAMS_FORMER_SCRIPT_RETURNED_NULL );
      result.put( paramId, paramValue );
    }

    return result;
  }

  /**
   * Тест скрипта.
   *
   * @param a - параметры запуска.
   * @throws InterruptedException
   */
  @SuppressWarnings( { "unchecked", "rawtypes", "javadoc" } )
  public static void main( String[] a )
      throws InterruptedException {
    String tagId = "tag"; //$NON-NLS-1$
    int bitIndex = 5;
    String paramId = "param.mm"; //$NON-NLS-1$

    System.out.println( replaceDotByDash( paramId ) );// .replaceAll( DOT, DASH ) );

    ScriptParamFormer eventParamFormer = new ScriptParamFormer();

    String intValueFormat = "(((%s.get().asInt() >> %d) %% 2) == 1)"; //$NON-NLS-1$
    String boolValueFormat = "%s.get().asBool()"; //$NON-NLS-1$

    String valueStr = bitIndex >= 0 ? String.format( intValueFormat, tagId, Integer.valueOf( bitIndex ) )
        : String.format( boolValueFormat, tagId );
    String ppp = replaceDotByDash( paramId );// paramId.replaceAll( DOT, DASH );
    eventParamFormer.paramFormerScript = String.format(
        "if(tag.get()!=null && tag.get().isAssigned()){var %s=Packages.ru.toxsoft.tslib.datavalue.impl.DvUtils.avBool( %s );}", //$NON-NLS-1$
        ppp, valueStr );

    eventParamFormer.eventParamsIds.add( paramId );

    ITag tag = new TagImpl( "", EKind.R, EAtomicType.BOOLEAN );

    IMapEdit<String, ITag> map = new ElemMap();
    map.put( "tag", tag ); //$NON-NLS-1$

    eventParamFormer.start( map );

    Thread.sleep( 100 );

    IStringMap<IAtomicValue> valuesMap;
    try {
      valuesMap = eventParamFormer.getEventParamValues( System.currentTimeMillis() );
      IAtomicValue value = valuesMap.getByKey( paramId );// .replaceAll( DOT, DASH ) );

      System.out.println( value.toString() );
    }
    catch( TsException ex ) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }

  }

  static String replaceDotByDash( String aInit ) {
    // if( true ) {
    return aInit.replaceAll( DOT, DASH );
    // }
    // if( aInit.indexOf( DOT ) > 0 ) {
    // String res1 = aInit.substring( 0, aInit.indexOf( DOT ) );
    // String res2 = aInit.substring( aInit.indexOf( DOT ) + 1 );
    // return res1 + DASH + res2;
    // }
    // return aInit;
  }

}

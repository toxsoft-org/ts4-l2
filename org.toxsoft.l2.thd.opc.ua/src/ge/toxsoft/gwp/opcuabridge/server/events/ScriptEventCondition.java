package ge.toxsoft.gwp.opcuabridge.server.events;

import static ge.toxsoft.gwp.opcuabridge.server.events.IDlmsBaseConstants.*;
import static ge.toxsoft.gwp.opcuabridge.server.events.IL2Resources.*;

import javax.script.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.thd.opc.ua.milo.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Условие возникновения события, логика которого программируется в скрипте, расположенном в конфигурационном файле.
 *
 * @author max
 */
public class ScriptEventCondition
    implements IOpcTagsCondition {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ScriptEventCondition.class );

  private String conditionScript;

  private String initScript = null;

  private ScriptEngine engine;

  @Override
  public void config( IAvTree aParams ) {
    conditionScript = aParams.fields().getStr( EVENT_COND_SCRIPT );

    if( aParams.fields().hasValue( EVENT_INIT_COND_SCRIPT ) ) {
      initScript = aParams.fields().getStr( EVENT_INIT_COND_SCRIPT );
    }
  }

  @Override
  public void start( IMap<String, ITag> aTags ) {
    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName( JAVA_SCRIPT_NAME );

    // устанавливаем теги

    for( String tagName : aTags.keys() ) {
      engine.put( tagName.replaceAll( DOT, DASH ), aTags.getByKey( tagName ) );
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
  public boolean isEventCondition( long aTime ) {
    // текущее время
    engine.put( SCRIPT_CURR_TIME_VAR, Long.valueOf( aTime ) );
    try {
      engine.eval( conditionScript );

      Boolean result = (Boolean)engine.get( SCRIPT_IS_EVENT_VAR );
      if( result == null ) {
        logger.error( ERR_MSG_IS_EVENT_SCRIPT_RETURNED_NULL );
        return false;
      }
      return result.booleanValue();
    }
    catch( ScriptException ex ) {
      logger.error( ex, ERR_MSG_IS_EVENT_SCRIPT_THREW_EXCEPTION );

    }
    return false;
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
    int bitIndex = 3;

    boolean isOn = true;
    boolean isOff = false;

    ScriptEventCondition eventCond = new ScriptEventCondition();

    String intValueFormat = "(((%s.get().asInt() >> %d) %% 2) == 1)"; //$NON-NLS-1$
    String boolValueFormat = "%s.get().asBool()"; //$NON-NLS-1$
    String isOnEventStr = "(value==true && (prev==null || prev == false))"; //$NON-NLS-1$
    String isOffEventStr = "(value==false && (prev==null || prev == true))"; //$NON-NLS-1$
    String orStr = "||"; //$NON-NLS-1$
    String emptyStr = ""; //$NON-NLS-1$

    String valueStr = bitIndex >= 0 ? String.format( intValueFormat, tagId, Integer.valueOf( bitIndex ) )
        : String.format( boolValueFormat, tagId );

    eventCond.initScript = "var prev = null;"; //$NON-NLS-1$
    eventCond.conditionScript = String.format(
        "if(tag.get()==null || !tag.get().isAssigned()) {var is_event = false;}else{var value=%s; var is_event =%s%s%s;prev = value;}", //$NON-NLS-1$
        valueStr, isOn ? isOnEventStr : emptyStr, (isOn && isOff) ? orStr : emptyStr,
        isOff ? isOffEventStr : emptyStr );

    ITag tag = new TagImpl( "", EKind.R, EAtomicType.BOOLEAN, TsLibUtils.EMPTY_STRING );

    IMapEdit<String, ITag> map = new ElemMap();
    map.put( "tag", tag ); //$NON-NLS-1$

    eventCond.start( map );

    Thread.sleep( 100 );

    System.out.println( eventCond.isEventCondition( System.currentTimeMillis() ) );

    Thread.sleep( 100 );

    System.out.println( eventCond.isEventCondition( System.currentTimeMillis() ) );
  }
}

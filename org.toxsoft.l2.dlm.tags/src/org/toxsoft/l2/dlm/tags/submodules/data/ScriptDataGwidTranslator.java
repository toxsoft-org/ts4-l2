package org.toxsoft.l2.dlm.tags.submodules.data;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;
import static org.toxsoft.l2.dlm.tags.submodules.data.IL2Resources.*;

import javax.script.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.rwkind.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Реализация передатчика, логика которого программируется в скрипте, расположенном в конфигурационном файле. <br>
 * пример скрипта: <br>
 * "var is_transmitted = data0.setDataValue(tag0.get(),curr_time);"
 *
 * @author max
 */
public class ScriptDataGwidTranslator
    implements IDataGwidTranslator {

  /**
   * Журнал работы
   */
  static ILogger logger;

  private String transmitScript;

  private String initScript;

  private ScriptEngine engine;

  @Override
  public boolean translate( long aTime ) {
    // текущее время
    engine.put( SCRIPT_CURR_TIME_VAR, Long.valueOf( aTime ) );
    try {
      engine.eval( transmitScript );

      Boolean result = (Boolean)engine.get( SCRIPT_IS_TRANSMITTED_VAR );
      if( result == null ) {
        logger.error( ERR_MSG_TRANSMIT_SCRIPT_RETURNED_NULL );
        return false;
      }
      return result.booleanValue();
    }
    catch( ScriptException ex ) {
      logger.error( ex, ERR_MSG_TRANSMIT_SCRIPT_THREW_EXCEPTION );

    }
    return false;
  }

  @Override
  public void config( IAvTree aParams ) {
    transmitScript = aParams.fields().getStr( DATA_TRANSMIT_SCRIPT );

    if( aParams.fields().hasValue( DATA_INIT_SCRIPT ) ) {
      initScript = aParams.fields().getStr( DATA_INIT_SCRIPT );
    }
  }

  @Override
  public void start( IGwidValueSetter[] aDataSetindexes, IGwidValueGetter[] aDataGetters, IList<IL2Tag> aTags ) {

    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName( JAVA_SCRIPT_NAME );

    // устанавливаем теги

    for( int i = 0; i < aTags.size(); i++ ) {
      IL2Tag tag = aTags.get( i );
      engine.put( String.format( TAG_NAME_FOR_SCRIPT_FORMAT, Integer.valueOf( i ) ), tag );
    }

    // устанавливаем установщики значений

    for( int i = 0; i < aDataSetindexes.length; i++ ) {
      IGwidValueSetter dataSetter = aDataSetindexes[i];
      engine.put( String.format( DATA_SETTER_NAME_FOR_SCRIPT_FORMAT, Integer.valueOf( i ) ), dataSetter );
    }

    // инициализационный скрипт
    if( initScript != null ) {
      // текущее время
      engine.put( SCRIPT_CURR_TIME_VAR, Long.valueOf( System.currentTimeMillis() ) );

      try {
        engine.eval( initScript );
      }
      catch( ScriptException ex ) {
        logger.error( ex, ERR_MSG_DATA_INIT_SCRIPT_THREW_EXCEPTION );
        throw new TsIllegalArgumentRtException( ex, ERR_MSG_DATA_INIT_SCRIPT_THREW_EXCEPTION );
      }
    }
  }

  /**
   * Тест скрипта.
   *
   * @param a - параметры запуска.
   */
  @SuppressWarnings( { "unchecked", "rawtypes" } )
  public static void main( String[] a ) {
    ScriptDataGwidTranslator testTrans = new ScriptDataGwidTranslator();

    testTrans.transmitScript = "var is_transmitted =  data0.setDataValue(tag0.get(),curr_time);"; //$NON-NLS-1$

    IGwidValueSetter setter = new IGwidValueSetter() {

      @Override
      public boolean setGwidValue( IAtomicValue aValue, long aTime ) {

        return aValue.asInt() > 5;
      }

      @Override
      public void close() {
        // TODO Auto-generated method stub

      }
    };

    IL2Tag tag = new IL2Tag() {

      @Override
      public String id() {

        return null;
      }

      @Override
      public String description() {

        return null;
      }

      @Override
      public void set( IAtomicValue aVal ) {
        // без реализации
      }

      @Override
      public IAtomicValue get() {

        return AvUtils.avInt( 5 );
      }

      @Override
      public String nmName() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public IOptionSet params() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public IDataType dataType() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ERwKind kind() {
        // TODO Auto-generated method stub
        return null;
      }
    };

    testTrans.start( new IGwidValueSetter[] { setter }, new IGwidValueGetter[0], new ElemArrayList<>( tag ) );

    System.out.println( testTrans.translate( System.currentTimeMillis() ) );
  }

}

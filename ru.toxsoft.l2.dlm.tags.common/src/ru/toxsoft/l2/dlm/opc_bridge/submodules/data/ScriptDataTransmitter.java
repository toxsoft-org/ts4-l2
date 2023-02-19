package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.data.IL2Resources.*;

import javax.script.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация передатчика, логика которого программируется в скрипте, расположенном в конфигурационном файле. <br>
 * пример скрипта: <br>
 * "var is_transmitted = data0.setDataValue(tag0.get(),curr_time);"
 *
 * @author max
 * @param <T> - класс дата-сета
 */
public class ScriptDataTransmitter<T extends ISkRtdataChannel>
    implements IDataTransmitter<T> {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ScriptDataTransmitter.class );

  private String transmitScript;

  private String initScript;

  private ScriptEngine engine;

  @Override
  public boolean transmit( long aTime ) {
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
  public void start( IDataSetter[] aDataSetindexes, IList<ITag> aTags, IMap<Gwid, T> aWriteDataSet ) {

    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName( JAVA_SCRIPT_NAME );

    // устанавливаем теги

    for( int i = 0; i < aTags.size(); i++ ) {
      ITag tag = aTags.get( i );
      engine.put( String.format( TAG_NAME_FOR_SCRIPT_FORMAT, Integer.valueOf( i ) ), tag );
    }

    // устанавливаем установщики значений

    for( int i = 0; i < aDataSetindexes.length; i++ ) {
      IDataSetter dataSetter = aDataSetindexes[i];
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
    ScriptDataTransmitter testTrans = new ScriptDataTransmitter<>();

    testTrans.transmitScript = "var is_transmitted =  data0.setDataValue(tag0.get(),curr_time);"; //$NON-NLS-1$

    IDataSetter setter = new IDataSetter() {

      @Override
      public boolean setDataValue( IAtomicValue aValue, long aTime ) {

        return aValue.asInt() > 5;
      }

      @Override
      public void sendOnServer() {
        // TODO Auto-generated method stub

      }

      @Override
      public void close() {
        // TODO Auto-generated method stub

      }
    };

    ITag tag = new ITag() {

      @Override
      public String id() {

        return null;
      }

      @Override
      public String description() {

        return null;
      }

      @Override
      public EAtomicType valueType() {

        return null;
      }

      @Override
      public String tagId() {

        return null;
      }

      @Override
      public void set( IAtomicValue aVal ) {
        // без реализации
      }

      @Override
      public String name() {

        return null;
      }

      @Override
      public EKind kind() {

        return null;
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
      public boolean isDirty() {
        // TODO Auto-generated method stub
        return false;
      }
    };

    testTrans.start( new IDataSetter[] { setter }, new ElemArrayList<>( tag ), null );

    System.out.println( testTrans.transmit( System.currentTimeMillis() ) );
  }

}

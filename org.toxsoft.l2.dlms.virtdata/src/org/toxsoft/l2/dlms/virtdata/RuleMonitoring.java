/**
 *
 */
package org.toxsoft.l2.dlms.virtdata;

import static org.toxsoft.l2.dlms.virtdata.IDlmConstants.*;
import static org.toxsoft.l2.dlms.virtdata.IL2Resources.*;

import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import javax.script.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Мониторинг одного правила
 *
 * @author dima
 */
public class RuleMonitoring
    implements ISkCurrDataChangeListener {

  /**
   * Журнал работы
   */
  private static final ILogger logger = LoggerUtils.errorLogger();

  /**
   * Ключ блокировки для обмена данными с буферными значениями
   */
  private Lock receivedValuesLock = new ReentrantLock();

  /**
   * Каналы для изменения значения выходных данных.
   */
  private IMap<Gwid, ISkWriteCurrDataChannel> writeChannels;

  /**
   * Значения входных параметров правила
   */
  private final IMapEdit<Gwid, IAtomicValue> inputParamValues = new ElemMap<>();
  /**
   * Флаг изменения состояния входных данных правила
   */
  private AtomicBoolean                      inputDataChanged = new AtomicBoolean( false );

  /**
   * Движек для проигрыша JavaScript правила
   */
  private ScriptEngine engine;

  /**
   * Описание правила
   */
  private RuleInfo rule;

  /**
   * Метка времени сработки правила
   */
  private Long fireTimestamp = null;

  /**
   * Старое значение флага
   */
  private boolean outParamOldVal = false;

  private ISkConnection connection;

  /**
   * объект - источник события о сработке правила
   */
  private Skid evSrcSkid;

  /**
   * Конструктор.
   *
   * @param aConnection соединение с сервером
   * @param aRuleInfo описание одного правила
   */
  public RuleMonitoring( ISkConnection aConnection, RuleInfo aRuleInfo ) {
    super();
    connection = aConnection;
    // сразу инициализируем массив значений входных и вЫходных данных
    for( int i = 0; i < aRuleInfo.getInputParams().size(); i++ ) {
      ParamInfo inputParam = aRuleInfo.getInputParams().get( i );
      Gwid inputParamGwid =
          Gwid.createRtdata( inputParam.getClassId(), inputParam.getObjName(), inputParam.getDataId() );
      inputParamValues.put( inputParamGwid, IAtomicValue.NULL );
    }
    rule = aRuleInfo;
    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName( JAVA_SCRIPT_NAME );

  }

  /**
   * Установить каналы для записи выходных данных
   *
   * @param aWriteChannels набор каналов для записи
   */
  public void setOutDataChannels( IMap<Gwid, ISkWriteCurrDataChannel> aWriteChannels ) {
    writeChannels = aWriteChannels;
  }

  //
  // ------------------------------------------------------------------------------------------------
  // Метод, выполняющий основную работу

  /**
   * Выполняет квант работы
   */
  @SuppressWarnings( "nls" )
  public void doJob() {

    // быстрый синхронизированный обмен и очистка
    try {
      receivedValuesLock.lock();
      if( inputDataChanged.get() || fireTimestamp != null ) {
        inputDataChanged.set( false );
        // Изменилось состояние входных данных правила, прогоняем скрипт заново
        // заносим значения входных параметров
        for( ParamInfo inParamInfo : rule.getInputParams() ) {
          String inParam = inParamInfo.getName();
          IAtomicValue paramVal = inputParamValues.getByKey( inParamInfo.gwid() );
          engine.put( inParam, paramVal );
        }
        // прогоняем скрипт
        engine.eval( rule.getJavaScript() );
        // получаем его результат
        String outParam = rule.getOutParam().getName();
        Boolean outParamVal = (Boolean)engine.get( outParam );
        if( outParamVal.booleanValue() ) {
          // Сработало правило
          // Делаем пометку времени сработки
          if( fireTimestamp == null ) {
            fireTimestamp = Long.valueOf( System.currentTimeMillis() );
          }
          // Проверяем вышел ли таймаут
          long currTime = System.currentTimeMillis();
          if( !outParamOldVal && (currTime - fireTimestamp.longValue()) >= rule.getTimeout() ) {
            outParamOldVal = true;
            // обновляем выходные данные
            writeChannels.getByKey( rule.getOutParam().gwid() )
                .setValue( AvUtils.avBool( outParamVal.booleanValue() ) );
            // Генерируем событие с пояснением
            OptionSet evParams = new OptionSet();
            evParams.setValue( EVENT_DESCR_PAR_ID, AvUtils.avStr( rule.getEventText() ) );
            evParams.setValue( EVENT_ON_PAR_ID, AvUtils.avBool( true ) );
            Gwid eventGwid = Gwid.createEvent( evSrcSkid.classId(), evSrcSkid.strid(), ERROR_EVENT_ID );
            SkEvent event = new SkEvent( currTime, eventGwid, evParams );
            connection.coreApi().eventService().fireEvent( event );
            logger.debug( DEBUG_MSG_FIRE_EVENT, evSrcSkid.classId(), evSrcSkid.strid(), "on", rule.getEventText() );
            fireTimestamp = null;
          }
        }
        else {
          // Проверяем и сбрасываем флаги и единожды обновляем сервер
          fireTimestamp = null;
          if( outParamOldVal ) {
            outParamOldVal = false;
            writeChannels.getByKey( rule.getOutParam().gwid() )
                .setValue( AvUtils.avBool( outParamVal.booleanValue() ) );

            // Заносим описание нарушения
            OptionSet evParams = new OptionSet();
            evParams.setValue( EVENT_DESCR_PAR_ID, AvUtils.avStr( rule.getEventText() ) );
            evParams.setValue( EVENT_ON_PAR_ID, AvUtils.avBool( false ) );
            Gwid eventGwid = Gwid.createEvent( evSrcSkid.classId(), evSrcSkid.strid(), ERROR_EVENT_ID );
            SkEvent event = new SkEvent( System.currentTimeMillis(), eventGwid, evParams );
            connection.coreApi().eventService().fireEvent( event );
            logger.debug( DEBUG_MSG_FIRE_EVENT, evSrcSkid.classId(), evSrcSkid.strid(), "off", rule.getEventText() );
          }
        }
      }
    }
    catch( ScriptException ex ) {
      logger.error( ex, ERR_MSG_TRANSMIT_SCRIPT_THREW_EXCEPTION );
    }
    catch( Exception ex ) {
      logger.error( ex.getMessage() );
    }
    finally {
      receivedValuesLock.unlock();
    }
  }

  /**
   * Устновить Skid источника события
   *
   * @param aEvSrcSkid - {@link Skid } id сущности источника события
   */

  void setEvSrcSkid( Skid aEvSrcSkid ) {
    evSrcSkid = aEvSrcSkid;
  }

  @Override
  public void onCurrData( IMap<Gwid, IAtomicValue> aNewValues ) {
    try {
      receivedValuesLock.lock();
      // Обработка изменений входных данных
      for( Gwid gwid : aNewValues.keys() ) {
        // проверяем что это "мои" данные
        if( inputParamValues.hasKey( gwid ) ) {
          IAtomicValue value = aNewValues.getByKey( gwid );
          inputParamValues.put( gwid, value );
          inputDataChanged = new AtomicBoolean( true );
        }
      }
    }
    finally {
      receivedValuesLock.unlock();
    }
  }

}

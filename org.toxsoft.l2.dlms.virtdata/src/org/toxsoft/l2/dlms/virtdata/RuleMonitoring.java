/**
 *
 */
package org.toxsoft.l2.dlms.virtdata;

import static org.toxsoft.l2.dlms.virtdata.IDlmConstants.*;
import static org.toxsoft.l2.dlms.virtdata.IL2Resources.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.script.*;

import org.apache.log4j.Logger;

import ru.toxsoft.s5.client.connection.IS5Connection;
import ru.toxsoft.s5.common.services.currdata.*;
import ru.toxsoft.s5.utils.WrapperLog4jLogger;
import ru.toxsoft.tslib.datavalue.IAtomicValue;
import ru.toxsoft.tslib.datavalue.impl.DvUtils;
import ru.toxsoft.tslib.utils.collections.IIntList;
import ru.toxsoft.tslib.utils.collections.IList;
import ru.toxsoft.tslib.utils.collections.impl.ElemArrayList;
import ru.toxsoft.tslib.utils.collections.impl.StringMap;
import ru.toxsoft.tslib.utils.logs.ILogger;

/**
 * Мониторинг одного правила
 *
 * @author dima
 */
public class RuleMonitoring
    implements IReadCurrDataSetListener {

  /**
   * Журнал работы
   */
  private ILogger logger = new WrapperLog4jLogger( Logger.getLogger( this.getClass().getName() ) );

  /**
   * Ключ блокировки для обмена данными с буферными значениями
   */
  private Lock receivedValuesLock = new ReentrantLock();

  /**
   * Набор текущих данных для изменения значения выходных данных.
   */
  private IWriteCurrDataSet outDataWriteCurrDataSet;

  /**
   * Значения входных параметров правила
   */
  private final ElemArrayList<IAtomicValue> inputParamValues = new ElemArrayList<>();

  /**
   * Флаг изменения состояния входных данных правила
   */
  private AtomicBoolean inputDataChanged = new AtomicBoolean( false );

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

  private IS5Connection connection;

  /**
   * объект - источник события о сработке правила
   */
  private long evSrcObjId;

  /**
   * класс - источник события о сработке правила
   */
  private String evSrcClassId;

  /**
   * объект - источник события о сработке правила
   */
  private String evSrcObjName;

  /**
   * Конструктор.
   *
   * @param aConnection соединение с сервером
   * @param aRuleInfo описание одного правила
   */
  public RuleMonitoring( IS5Connection aConnection, RuleInfo aRuleInfo ) {
    super();
    connection = aConnection;
    // сразу инициализируем массив значений входных и вЫходных данных
    for( int i = 0; i < aRuleInfo.getInputParams().size(); i++ ) {
      inputParamValues.add( IAtomicValue.NULL );
    }
    rule = aRuleInfo;
    ScriptEngineManager mgr = new ScriptEngineManager();
    engine = mgr.getEngineByName( JAVA_SCRIPT_NAME );

  }

  /**
   * Установить набор данных для изменения выходных данных
   *
   * @param aOutDataCdSet набор данных для записи
   */
  public void setOutDataCurrDataSet( IWriteCurrDataSet aOutDataCdSet ) {
    outDataWriteCurrDataSet = aOutDataCdSet;
  }

  //
  // ------------------------------------------------------------------------------------------------
  // Метод, выполняющий основную работу

  /**
   * Выполняет квант работы
   */
  public void doJob() {

    // быстрый синхронизированный обмен и очистка
    try {
      receivedValuesLock.lock();
      if( inputDataChanged.get() != false || fireTimestamp != null ) {
        inputDataChanged.set( false );
        // Изменилось состояние входных данных правила, прогоняем скрипт заново
        // заносим значения входных параметров
        for( int i = 0; i < rule.getInputParams().size(); i++ ) {
          String inParam = rule.getInputParams().get( i ).getName();
          IAtomicValue paramVal = inputParamValues.get( i );
          engine.put( inParam, paramVal );
        }
        // прогоняем скрипт
        engine.eval( rule.getJavaScript() );
        // получаем его результат
        String outParam = rule.getOutParam().getName();
        Boolean outParamVal = (Boolean)engine.get( outParam );
        if( outParamVal.booleanValue() != false ) {
          // Сработало правило
          // Делаем пометку времени сработки
          if( fireTimestamp == null ) {
            fireTimestamp = Long.valueOf( System.currentTimeMillis() );
          }
          // Проверяем вышел ли таймаут
          long currTime = System.currentTimeMillis();
          if( outParamOldVal == false && (currTime - fireTimestamp.longValue()) >= rule.getTimeout() ) {
            outParamOldVal = true;
            // обновляем набор данных
            outDataWriteCurrDataSet.set( 0, DvUtils.avBool( outParamVal.booleanValue() ), System.currentTimeMillis() );
            outDataWriteCurrDataSet.write();
            // Генерируем событие с пояснением
            StringMap<IAtomicValue> paramValues = new StringMap<>();
            // Заносим описание нарушения
            paramValues.put( EVENT_DESCR_PAR_ID, DvUtils.avStr( rule.getEventText() ) );
            // Заносим флаг выставлено
            paramValues.put( EVENT_ON_PAR_ID, DvUtils.avBool( true ) );
            connection.serverApi().eventService().fireEvent( evSrcObjId, ERROR_EVENT_ID, paramValues,
                System.currentTimeMillis() );
            logger.debug( DEBUG_MSG_FIRE_EVENT, evSrcClassId, evSrcObjName, "on", rule.getEventText() );
            fireTimestamp = null;
          }
        }
        else {
          // Проверяем и сбрасываем флаги и единожды обновляем сервер
          fireTimestamp = null;
          if( outParamOldVal != false ) {
            outParamOldVal = false;
            outDataWriteCurrDataSet.set( 0, DvUtils.avBool( outParamVal.booleanValue() ), System.currentTimeMillis() );
            outDataWriteCurrDataSet.write();
            StringMap<IAtomicValue> paramValues = new StringMap<>();
            // Заносим описание нарушения
            paramValues.put( EVENT_DESCR_PAR_ID, DvUtils.avStr( rule.getEventText() ) );
            // Заносим флаг сброшено
            paramValues.put( EVENT_ON_PAR_ID, DvUtils.avBool( false ) );
            connection.serverApi().eventService().fireEvent( evSrcObjId, ERROR_EVENT_ID, paramValues,
                System.currentTimeMillis() );
            logger.debug( DEBUG_MSG_FIRE_EVENT, evSrcClassId, evSrcObjName, "off", rule.getEventText() );
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

  @Override
  public void onCurrDataValuesChanged( IReadCurrDataSet aCurrDataSet, IIntList aIndexes, IList<IAtomicValue> aValues ) {
    // получение изменённых значений и помещение их в некий буфер
    try {
      receivedValuesLock.lock();
      // Обработка изменений входных данных
      for( Integer index : aIndexes ) {
        IAtomicValue val = aCurrDataSet.values().get( index.intValue() );
        inputParamValues.set( index.intValue(), val );
        inputDataChanged = new AtomicBoolean( true );
      }
    }
    finally {
      receivedValuesLock.unlock();
    }
  }

  @Override
  public void onReady( IReadCurrDataSet aCurrDataSet ) {
    // nop
  }

  @Override
  public void onDataSetCodsChanged( IRtDataSet<?> aRtDataSet ) {
    // nop
  }

  @Override
  public void onBeforeClose( IRtDataSet<?> aRtDataSet ) {
    // nop
  }

  /**
   * Устновить ObjId источника события
   *
   * @param aEvSrcObjId
   */
  void setEvSrcObjId( long aEvSrcObjId ) {
    evSrcObjId = aEvSrcObjId;
  }

  void setDebugInfo( String aEvSrcClassId, String aEvSrcObjName ) {
    evSrcClassId = aEvSrcClassId;
    evSrcObjName = aEvSrcObjName;
  }

}

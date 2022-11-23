package ru.toxsoft.l2.core.reserve.impl;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Реализация слушателя здоровья шкафа.
 *
 * @author max
 */
public class HealthListener
    extends WorkerComponentBase
    implements IHealthListener {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  private static final String CRITICAL_HEALTH_LEVEL_PARAM_ID = "critical.health.level"; //$NON-NLS-1$

  /**
   * Компонент работы с железом
   */
  private IHal hal;

  /**
   * Контекст
   */
  private GlobalContext globalContext;

  /**
   * Параметры настройки резервирования.
   */
  private final IOptionSet reserveOps;

  /**
   * Порог критической потери функциональности
   */
  private int criticalLevel = 90;

  /**
   * Текущее значение качества функциональности
   */
  private int currHealth = 100;

  // private long testTimeout = 60000;
  // private long startTime = 0;

  /**
   * КОнструктор по глобальному контесту и набору настроечных параметров.
   *
   * @param aGlobalContext GlobalContext - глобальный контекст
   * @param aReserveOps IOptionSet - набор параметров
   */
  public HealthListener( GlobalContext aGlobalContext, IOptionSet aReserveOps ) {
    globalContext = aGlobalContext;
    reserveOps = aReserveOps;

    criticalLevel = reserveOps.getInt( CRITICAL_HEALTH_LEVEL_PARAM_ID, criticalLevel );
  }

  @Override
  protected void doStartComponent() {
    hal = globalContext.hal();

    // TEST
    // startTime = System.currentTimeMillis();
  }

  @Override
  public void doJob() {
    currHealth = hal.getHealth();

    // TEST
    // if( System.currentTimeMillis() - startTime > testTimeout ) {
    // currHealth = 50;
    // }
  }

  @Override
  public int getHealth() {
    return currHealth;
  }

  @Override
  public boolean isSick() {
    logger.debug( "Sick detection currH < level: %d < %d", Integer.valueOf( currHealth ), //$NON-NLS-1$
        Integer.valueOf( criticalLevel ) );
    return currHealth < criticalLevel;
  }

  @Override
  protected boolean doQueryStop() {
    return true;
  }

  @Override
  protected boolean doStopStep() {
    return true;
  }

}

package ru.toxsoft.l2.core.reserve.impl;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.reserve.*;

/**
 * Реализация резервирования при полном отсутствии резервирования.
 *
 * @author Max
 */
public class EmptyReserveImpl
    extends AbstractL2Component
    implements IReserveComponent {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст
   */
  private GlobalContext globalContext;

  /**
   * Менеджер dlm-s
   */
  private IDlmManagerComponent dlmManager;

  /**
   * Признак того, что загружаемые модули запущены и работают
   */
  private boolean isDlmsRun = false;

  /**
   * Конструктор по контексту.
   *
   * @param aContext GlobalContext - контекст.
   */
  public EmptyReserveImpl( GlobalContext aContext ) {
    super( aContext );
    globalContext = aContext;
  }

  @Override
  public boolean isDLMsRun() {
    return isDlmsRun;
  }

  @Override
  protected void processStart() {
    logger.info( "Dlm manager is going to start (in empty reserve)" ); //$NON-NLS-1$
    dlmManager = (IDlmManagerComponent)globalContext.dlmManager();
    dlmManager.start();
    isDlmsRun = true;
    logger.info( "Dlm manager has started (in empty reserve)" ); //$NON-NLS-1$
  }

  @Override
  protected void processRunStep() {
    // без реализации
  }

  @Override
  protected boolean processStopQuery() {
    return dlmManager.queryStop();
  }

  @Override
  protected boolean processStopStep() {
    return dlmManager.isStopped();
  }

  @Override
  protected void processDestroy() {
    dlmManager.destroy();
  }

}

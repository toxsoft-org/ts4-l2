package ru.toxsoft.l2.core.reserve.impl;

import static ru.toxsoft.l2.core.reserve.impl.IReserveParams.*;

import org.toxsoft.core.tslib.av.opset.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Асинхронный контроллер запуска-останова модулей (НУ продолжает работать во время запуска или останова модулей)
 *
 * @author max
 */
public class DlmsAsynchController
    extends WorkerComponentBase
    implements IDlmsController {

  /**
   * Менеджер dlm-s
   */
  private IDlmManagerComponent dlmManager;

  /**
   * Контекст
   */
  private GlobalContext globalContext;

  /**
   * Параметры настройки резервирования.
   */
  private final IOptionSet reserveOps;

  /**
   * Тайм-аут останова модулей. После тайм-аута длм должны завершиться аварийно.
   */
  private int stopTimeout = 3000;// Integer.MAX_VALUE;

  /**
   * Признак того, что загружаемые модули запущены и работают
   */
  private boolean isDlmsRun = false;

  private long startStoppingTime = 0L;

  /**
   * Конструктор по глобальному контексту и набору параметров.
   *
   * @param aGlobalContext GlobalContext - глобальный контекст.
   * @param aReserveOps IOptionSet - набор параметров.
   */
  public DlmsAsynchController( GlobalContext aGlobalContext, IOptionSet aReserveOps ) {
    super();
    globalContext = aGlobalContext;
    this.reserveOps = aReserveOps;
  }

  @Override
  public void doJob() {
    if( startStoppingTime > 0 ) {
      if( dlmManager.isStopped() ) {
        startStoppingTime = 0;
        isDlmsRun = false;
      }
      else {
        if( System.currentTimeMillis() - startStoppingTime > stopTimeout ) {
          dlmManager.destroy();
          startStoppingTime = 0;
          isDlmsRun = false;
        }
      }
    }
  }

  @Override
  protected void doStartComponent() {
    dlmManager = (IDlmManagerComponent)globalContext.dlmManager();
    stopTimeout = OP_DLMS_STOP_TIMEOUT.getValue( reserveOps ).asInt();

    // stopTimeout = 2500;
  }

  @Override
  public void startDLMs() {
    if( startStoppingTime > 0 ) {
      return;
    }
    dlmManager.start();
    isDlmsRun = true;
  }

  @Override
  public void stopDlms() {
    startStoppingTime = System.currentTimeMillis();
    dlmManager.queryStop();
    if( dlmManager.isStopped() ) {
      startStoppingTime = 0;
      isDlmsRun = false;
    }
  }

  @Override
  public boolean isDLMsRun() {
    return isDlmsRun;
  }

}

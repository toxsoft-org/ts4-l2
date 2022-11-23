package ru.toxsoft.l2.core.reserve.impl;

import static ru.toxsoft.l2.core.reserve.impl.IReserveParams.*;

import org.toxsoft.core.tslib.av.opset.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Синхронный контроллер запуска-останова модулей (НУ останавливается целиком на время запуска останова модулей)
 *
 * @author max
 */
public class DlmsSynchController
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
  private int stopTimeout = Integer.MAX_VALUE;

  /**
   * Признак того, что загружаемые модули запущены и работают
   */
  private boolean isDlmsRun = false;

  /**
   * Конструктор по глобальному контексту и набору параметров.
   *
   * @param aGlobalContext GlobalContext - глобальный контекст.
   * @param aReserveOps IOptionSet - набор параметров.
   */
  public DlmsSynchController( GlobalContext aGlobalContext, IOptionSet aReserveOps ) {
    super();
    globalContext = aGlobalContext;
    this.reserveOps = aReserveOps;
  }

  @Override
  public void doJob() {
    //
  }

  @Override
  protected void doStartComponent() {
    dlmManager = (IDlmManagerComponent)globalContext.dlmManager();
    stopTimeout = OP_DLMS_STOP_TIMEOUT.getValue( reserveOps ).asInt();
  }

  @Override
  public void startDLMs() {
    dlmManager.start();
    isDlmsRun = true;
  }

  @Override
  public void stopDlms() {
    if( !dlmManager.queryStop() ) {
      long t0 = System.currentTimeMillis();
      while( !dlmManager.isStopped() ) {
        long t1 = System.currentTimeMillis();
        if( t1 - t0 > stopTimeout ) {
          dlmManager.destroy();
          break;
        }
      }
    }
    isDlmsRun = false;
  }

  @Override
  public boolean isDLMsRun() {
    return isDlmsRun;
  }

}

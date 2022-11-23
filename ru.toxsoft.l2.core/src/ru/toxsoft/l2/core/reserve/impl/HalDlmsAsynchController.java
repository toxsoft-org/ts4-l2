package ru.toxsoft.l2.core.reserve.impl;

import static ru.toxsoft.l2.core.reserve.impl.IReserveParams.*;

import org.toxsoft.core.tslib.av.opset.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Контроллер запуска и останова необходимых элементов Hal и модулей Dlm
 *
 * @author max
 */
public class HalDlmsAsynchController
    extends WorkerComponentBase
    implements IDlmsController {

  /**
   * Начальник драйверов
   */
  private IHal hal;

  /**
   * Контроллер dlm-s
   */
  private IDlmsController dlmController;

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
  private int stopTimeout = 2 * 3000;// Integer.MAX_VALUE;

  /**
   * Признак того, что загружаемые модули запущены и работают
   */
  private boolean isHalDlmsRun = false;

  private long startStoppingTime = 0L;

  private long startHalStoppingTime = 0L;

  /**
   * Конструктор по контексту, параметрам и контроллеру Длм-ов
   *
   * @param aGlobalContext GlobalContext - глобальный контекст.
   * @param aReserveOps IOptionSet - набор параметров.
   * @param aDlmsController IDlmsController - контроллер Длм-ов
   */
  public HalDlmsAsynchController( GlobalContext aGlobalContext, IOptionSet aReserveOps,
      IDlmsController aDlmsController ) {
    super();
    globalContext = aGlobalContext;
    reserveOps = aReserveOps;
    dlmController = aDlmsController;
  }

  @Override
  public void doJob() {
    dlmController.doJob();
    if( startStoppingTime > 0 ) {
      if( startHalStoppingTime > 0 ) {
        checkHalIsReserved();
      }
      else {
        if( !dlmController.isDLMsRun() ) {
          hal.queryReserve();
          checkHalIsReserved();
        }
      }
      if( isHalDlmsRun && System.currentTimeMillis() - startStoppingTime > stopTimeout ) {
        // hal.destroy();TODO logging
        startStoppingTime = 0;
        startHalStoppingTime = 0;
        isHalDlmsRun = false;
      }
    }
  }

  @Override
  protected void doStartComponent() {
    dlmController.start();
    hal = globalContext.hal();
    stopTimeout = 2 * OP_DLMS_STOP_TIMEOUT.getValue( reserveOps ).asInt();
  }

  @Override
  public void startDLMs() {
    if( startStoppingTime > 0 ) {
      return;
    }
    hal.queryMain();
    dlmController.startDLMs();
    isHalDlmsRun = true;
  }

  @Override
  public void stopDlms() {
    startStoppingTime = System.currentTimeMillis();
    dlmController.stopDlms();
    if( !dlmController.isDLMsRun() ) {
      startHalStoppingTime = System.currentTimeMillis();
      hal.queryReserve();
      checkHalIsReserved();
    }
  }

  @Override
  protected boolean doQueryStop() {
    return dlmController.queryStop();
  }

  @Override
  protected boolean doStopStep() {
    return dlmController.isStopped();
  }

  @Override
  protected void doDestrоyComponent() {
    dlmController.destroy();
  }

  @Override
  public boolean isDLMsRun() {
    // if( startStoppingTime > 0 ) {
    // if( startHalStoppingTime > 0 ) {
    // checkHalIsReserved();
    // }
    // else {
    // if( !dlmController.isDLMsRun() ) {
    // hal.queryReserve();
    // checkHalIsReserved();
    // }
    // }
    // }
    return isHalDlmsRun;
  }

  private void checkHalIsReserved() {
    if( hal.isWorkAsReserve() ) {
      startStoppingTime = 0;
      startHalStoppingTime = 0;
      isHalDlmsRun = false;
    }
  }

}

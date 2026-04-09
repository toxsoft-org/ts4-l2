package org.toxsoft.l2.lib.dlm;

import org.toxsoft.core.tslib.bricks.*;

/**
 * Компонента менеджера управления модулями.
 * <p>
 * Компонента расширяет интерфейс {@link IDlmManager} методами управления жизненым циклом.
 *
 * @author goga
 */
public interface IDlmManagerComponent
    extends ICooperativeWorkerComponent, IDlmManager {

  // TODO получение списка текущих модулей

}

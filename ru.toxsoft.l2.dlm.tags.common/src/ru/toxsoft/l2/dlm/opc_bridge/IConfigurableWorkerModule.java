package ru.toxsoft.l2.dlm.opc_bridge;

import org.toxsoft.core.tslib.bricks.*;

import ru.toxsoft.l2.core.cfg.*;

/**
 * Интерфейс конфигурируемого модуля (подмодуля) , выполняющего свою работу под управлением контейнера.
 *
 * @author MAX
 */
public interface IConfigurableWorkerModule
    extends IConfigurableUnit, ICooperativeWorkerComponent {
  // не добавляет новых методов
}

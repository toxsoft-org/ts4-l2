package ru.toxsoft.l2.core.app;

import org.toxsoft.core.tslib.bricks.*;

import ru.toxsoft.l2.core.invokable.*;

/**
 * Компанента доступа к проектно-зависимому функционалу нижнего уровня APP. Наследуется интерфейс {@link IInvokable} -
 * для реализации работы с глобальными переменными.
 *
 * @author max
 */
public interface IAppComponent
    extends ICooperativeWorkerComponent, IInvokable, IInvokableInfo, IApp {

  // пока пусто
}

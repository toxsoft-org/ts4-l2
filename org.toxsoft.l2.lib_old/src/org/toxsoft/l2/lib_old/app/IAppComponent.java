package org.toxsoft.l2.lib_old.app;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.l2.lib_old.hal.*;
import org.toxsoft.l2.lib_old.invokable.*;

/**
 * Компанента доступа к проектно-зависимому функционалу нижнего уровня APP. Наследуется интерфейс {@link IInvokable} -
 * для реализации работы с глобальными переменными.
 *
 * @author max
 */
public interface IAppComponent
    extends ICooperativeWorkerComponent, IInvokable, IInvokableInfo, IHalErrorProcessor {

  // nop

}

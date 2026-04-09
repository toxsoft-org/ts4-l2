package org.toxsoft.l2.lib.app;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.invokable.*;

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

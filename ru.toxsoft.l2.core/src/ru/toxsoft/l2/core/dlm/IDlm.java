package ru.toxsoft.l2.core.dlm;

import org.toxsoft.core.tslib.bricks.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.invokable.*;

/**
 * Базовый интерфейс точки входа динамически загружаемых модулей (DLM). Наследуется интерфейс {@link IInvokable} - для
 * реализации возможности вызова методов (и изменения переменных) другими модулями (через вызов методов контекста
 * {@link IDlmContext} ).
 *
 * @author goga
 */
public interface IDlm
    extends ICooperativeWorkerComponent, IInvokable, IConfigurableUnit {

  /**
   * Возвращает идентификационную информацию о модуле.
   * <p>
   * Это та же информация, которую возвращает фабрика этого модуле в методе {@link IDlmFactory#info()}.
   *
   * @return {@link IDlmInfo} - информация о модуле.
   */
  IDlmInfo info();

  /**
   * Возвращает информацию о возможности вызова у этого модуля методов и установки переменных (объект link
   * {@link IInvokable}).
   *
   * @return {@link IInvokableInfo} - информация об объекте с методами и переменными
   */
  IInvokableInfo invokableInfo();

}

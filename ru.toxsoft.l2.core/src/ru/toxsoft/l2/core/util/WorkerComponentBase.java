package ru.toxsoft.l2.core.util;

import org.toxsoft.core.tslib.bricks.*;

/**
 * Базовая реализация компоненты, управлемой контейнером.
 *
 * @author goga
 * @version $id$
 */
public class WorkerComponentBase
    implements IWorkerComponent {

  /**
   * Флаг того, что компонента остановлена. Флаг означает:
   * <ul>
   * <li>false - компонента работает (запущен в работу методом {@link #start()}, в том числе, находится в процессе
   * остановки (инициированной методом {@link #queryStop()}).</li>
   * <li>true - компонента либо еще не приступала к работе, либо уже остановлен.</li>
   * </ul>
   * Флаг выставлентся в:
   * <ul>
   * <li>false методом {@link #start()}</li>
   * <li>в true либо при успешном завершении {@link #doQueryStop()} в методе {@link #queryStop()} или в процессе
   * остановки наследиником (возвращаемым из метода {@link #doStopStep()} значением).</li>
   * </ul>
   */
  private volatile boolean stopped = true;

  /**
   * Признак того, что работает процесс остановки, то есть, {@link #queryStop()} был вызван.
   * <p>
   * Устанавливается в true в методе {@link #queryStop()}, в false при остановке. Также сбрасывается в начальное
   * состояние (false) в методе {@link #destroy()}.
   */
  private volatile boolean stopping = false;

  /**
   * Признак рабочего состояния компоненты.
   * <p>
   * Рабочее сосотяние компоненты это строго после вызова {@link #start()} и до вызова {@link #queryStop()}.
   */
  private volatile boolean working = false;

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IWorkerComponent
  //

  @Override
  final public void start() {
    stopped = false;
    doStartComponent();
    working = true;
  }

  @Override
  final public boolean queryStop() {
    working = false;
    stopped = doQueryStop();
    stopping = !stopped;
    return stopped;
  }

  @Override
  final public boolean isStopped() {
    stopped = doStopStep();
    stopping = !stopped;
    return stopped;
  }

  @Override
  final public void destroy() {
    working = false;
    stopped = true;
    stopping = false;
    doDestrоyComponent();
  }

  // --------------------------------------------------------------------------
  // Методы для использования наследниками
  //

  /**
   * Возвращает признак того, что работает процесс остановки, то есть, {@link #queryStop()} был вызван.
   * <p>
   * Устанавливается в true в методе {@link #queryStop()}. Сбрасывается в начальное состояние (false) в методе
   * {@link #start()}.
   *
   * @return <b>true</b> - компонента останавлиается или уже остановлена, не нужно еще раз вызывать {@link #queryStop()}
   *         ;<br>
   *         <b>false</b> - компонента работает.
   */
  final protected boolean isStopping() {
    return stopping;
  }

  /**
   * Возвращает признак рабочего состояния компоненты.
   *
   * @return boolean - признак рабочего состояния компоненты<br>
   *         <b>true</b> - компоненты работает, можно вызываеть методы интерфейса предметной области;<br>
   *         <b>false</b> - .
   */
  final protected boolean isWorking() {
    return working;
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //

  /**
   * Произвести запуск работы компоненты (Вызывается из {@link #start()}).
   * <p>
   * По умолчанию ничего не делает, и если нет необходимости, можно не переопределять метод.
   */
  protected void doStartComponent() {
    // ничего не делает
  }

  /**
   * Завершить работу компоненты, или для длительного процесса остановки - начать останов.
   * <p>
   * После начала длительного процесса остановки существует единственный способ известить этот базовый класс о том, что
   * процесс остановки завершен - вернуть true из метода {@link #doStopStep()}.
   * <p>
   * Вызывается из {@link #queryStop()}.
   * <p>
   * По умолчанию возвращает true, и если нет необходимости в специальной процедуре завершения, можно не переопределять
   * метод.
   *
   * @return <b>true</b> - работа компоненты успешно остановлена;<br>
   *         <b>false</b> - начат процесс остановки компоненты.
   */
  protected boolean doQueryStop() {
    return true;
  }

  /**
   * Осуществить очередной шаг для остановки компоненты.
   * <p>
   * Вызывается из метода {@link #isStopped()}, который в свою очередь периодически вызывает контейнер компонент после
   * запроса остановки методом {@link #queryStop()}.
   *
   * @return <b>true</b> - работа компоненты успешно остановлена;<br>
   *         <b>false</b> - процесс остановки компоненты продолжается.
   */
  protected boolean doStopStep() {
    return false;
  }

  /**
   * Произвести действия перед уничтожением компоненты.
   * <p>
   * Вызывается из {@link #destroy()};
   * <p>
   * По умолчанию ничего не делает, и если нет необходимости, можно не переопределять метод.
   */
  protected void doDestrоyComponent() {
    // ничего не делает
  }

}

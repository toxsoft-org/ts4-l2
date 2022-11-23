package ru.toxsoft.l2.core.main.impl;

import static ru.toxsoft.l2.core.main.impl.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.main.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Базовый класс всех компонент, создаваемых в {@link L2CoreMain#main(String[])}.
 *
 * @author goga
 */
public abstract class AbstractL2Component
    extends WorkerComponentBase
    implements ICooperativeWorkerComponent {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст в котором работает компонента.
   */
  protected final IGlobalContext context;

  /**
   * Консруктор для наследников.
   *
   * @param aContext {@link IGlobalContext} - глобальный контекст работы компоненты
   */
  protected AbstractL2Component( IGlobalContext aContext ) {
    context = TsNullArgumentRtException.checkNull( aContext );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ICooperativeMultiTaskable
  //

  @Override
  final public void doJob() {
    processRunStep();
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ICooperativeWorkerComponent
  //

  @Override
  final public void doStartComponent() {
    logger.info( MSG_GLOBAL_COMONENT_STARTING, this.getClass().getSimpleName() );
    processStart();
    logger.info( MSG_GLOBAL_COMONENT_STARTED, this.getClass().getSimpleName() );
  }

  @Override
  final public boolean doQueryStop() {
    logger.info( MSG_GLOBAL_COMONENT_STOPPING, this.getClass().getSimpleName() );
    return processStopQuery();
  }

  @Override
  final public boolean doStopStep() {
    return processStopStep();
  }

  @Override
  final public void doDestrоyComponent() {
    processDestroy();
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //

  protected boolean processStopStep() {
    return true;
  }

  protected void processDestroy() {
    // nop
  }

  abstract protected void processStart();

  abstract protected void processRunStep();

  abstract protected boolean processStopQuery();

}

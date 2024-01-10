package ru.toxsoft.l2.dlm.opc_bridge;

import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Базовая реализация конфигурируемого модуля, управляемого контейнером.
 *
 * @author MAX
 */
public class ConfigurableWorkerModuleBase
    extends WorkerComponentBase
    implements IConfigurableWorkerModule {

  /**
   * Признак сконфигурированного состояния модуля.
   * <p>
   * Сконфигурированное состояние компоненты это строго после вызова {@link #configYourself()}.
   */
  private volatile boolean configured = false;

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IConfigurableWorkerModule
  //

  @Override
  final public void configYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {
    // если запущен - не проводить конфигурацию
    if( isWorking() ) {
      return;
    }
    doConfigYourself( aConfig );
    configured = true;
  }

  @Override
  final public void doJob() {
    // если не запущен - не проводить работу
    if( !isWorking() ) {
      return;
    }

    doDoJob();
  }

  // --------------------------------------------------------------------------
  // Методы для использования наследниками
  //

  /**
   * Возвращает признак сконфигурированного состояния модуля.
   *
   * @return boolean - признак сконфигурированного состояния модуля<br>
   *         <b>true</b> - модуль сконфигурирован, можно вызывать метод запуска модуля;<br>
   *         <b>false</b> - .
   */
  final protected boolean isConfigured() {
    return configured;
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //

  /**
   * Произвести часть работы модуля. Метод вызывается из метода {@link #doJob()} только в рабочем состоянии модуля
   * (метод {@link #isWorking()} возвращает <b>true</b>). Бизнес-метод, не имеющий реализации в базовом классе.
   */
  protected void doDoJob() {
    // нет реализации

  }

  /**
   * Произвести конфигурацию модуля. При нормальном завершении метода (без исключений) - модуль считается
   * сконфигурированным и в дальнейшем метод {@link #isConfigured()} возвращает <b>true</b>. В случае возникновения
   * исключения модуль не считается сконфигурированным. Метод вызывается из метода {@link #configYourself(IUnitConfig)}
   * только в нерабочем состоянии модуля (метод {@link #isWorking()} возвращает <b>false</b>). Не имеет реализации в
   * базовом классе.
   *
   * @param aConfig IUnitConfig - параметры конфигурации модуля.
   */
  @SuppressWarnings( "unused" )
  protected void doConfigYourself( IUnitConfig aConfig ) {
    // нет реализации

  }

  @Override
  protected boolean doStopStep() {
    return true;
  }
}

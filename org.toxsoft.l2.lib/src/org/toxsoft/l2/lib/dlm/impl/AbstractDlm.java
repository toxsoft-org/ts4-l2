package org.toxsoft.l2.lib.dlm.impl;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.l2.lib.cfg.*;
import org.toxsoft.l2.lib.dlm.*;
import org.toxsoft.l2.lib.invokable.*;
import org.toxsoft.l2.lib.util.*;

/**
 * Базовая реализация все динамически загружаемых модулей.
 *
 * @author goga
 */
public abstract class AbstractDlm
    extends WorkerComponentBase
    implements IDlm {

  private final IDlmInfo      info;
  protected final IDlmContext context;

  /**
   * Конуструктор для наследников.
   *
   * @param aInfo {@link IDlmInfo} - информация о модуле, создаваемое в фабрике из {@link IPluginInfo}
   * @param aContext {@link IDlmContext} - контекст выполнения модулей
   */
  protected AbstractDlm( IDlmInfo aInfo, IDlmContext aContext ) {
    TsNullArgumentRtException.checkNulls( aInfo, aContext );
    info = aInfo;
    context = aContext;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDlm
  //

  @Override
  public IDlmInfo info() {
    return info;
  }

  @Override
  public IInvokableInfo invokableInfo() {
    return null;
  }

  //
  // ------------------------------------------------------------------------
  // IInvokable

  @Override
  public IAtomicValue getVar( String aVarId ) {
    return null;
  }

  @Override
  public void setVar( String aVarId, IAtomicValue aValue ) {
    // пустая реализация. переопределяется наследниками в случае необходимости
  }

  @Override
  public ECallStatus call( String aMethodId, IOptionSet aArgsValues ) {
    return null;
  }

  @Override
  public IOptionSet getStructVar( String aStructId )
      throws TsIllegalArgumentRtException {
    return null;
  }

  //
  // -----------------------------------------------------------------------
  // IConfigurableUnit
  @Override
  public void configYourself( IUnitConfig aConfig ) {
    // пустая реализация. переопределяется наследниками в случае необходимости конфигурации модулей
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ICooperativeWorkerComponent
  //

  @Override
  public abstract void doJob();

  @Override
  protected void doStartComponent() {
    // TsTestUtils.pl( info().moduleName() + " " + context.dlmRealm().id() + " doStartComponent()" );
  }

  @Override
  protected boolean doQueryStop() {
    // TsTestUtils.pl( info().moduleName() + " " + context.dlmRealm().id() + " doQueryStop()" );
    return super.doQueryStop();
  }

  @Override
  protected boolean doStopStep() {
    // TsTestUtils.pl( info().moduleName() + " " + context.dlmRealm().id() + " doStopStep()" );
    return super.doStopStep();
  }

  @Override
  protected void doDestrоyComponent() {
    // TsTestUtils.pl( info().moduleName() + " " + context.dlmRealm().id() + " doDestrоyComponent()" );
  }

}

package ru.toxsoft.l2.core.main.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.app.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.main.*;
import ru.toxsoft.l2.core.net.*;

/**
 * Реализация {@link IGlobalContext}.
 * <p>
 * Каждая компонента в конструкторе добавляет себя в контекст.
 *
 * @author goga
 */
public class GlobalContext
    implements IGlobalContext {

  private final IOptionSet globalOps;
  private final ILogger    logger;
  private final long       startTime;
  private IHal             hal        = null;
  private INetwork         network    = null;
  private IDlmManager      dlmManager = null;
  private IApp             appApi     = null;

  /**
   * Конструктор со всеми инвариантами.
   *
   * @param aGlobalOps {@link IOptionSet} - глобальные параметры программы
   * @param aLogger {@link ILogger} - общий логер
   * @param aStartTime long - момент старта программы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public GlobalContext( IOptionSet aGlobalOps, ILogger aLogger, long aStartTime ) {
    TsNullArgumentRtException.checkNulls( aGlobalOps, aLogger );
    globalOps = aGlobalOps;
    logger = aLogger;
    startTime = aStartTime;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IGlobalContext
  //

  @Override
  public IOptionSet globalOps() {
    return globalOps;
  }

  @Deprecated
  @Override
  public ILogger logger() {
    return logger;
  }

  @Override
  public long startTime() {
    return startTime;
  }

  @Override
  public IHal hal() {
    return hal;
  }

  @Override
  public INetwork network() {
    return network;
  }

  @Override
  public IDlmManager dlmManager() {
    return dlmManager;
  }

  @Override
  public IApp appApi() {
    return appApi;
  }

  // ------------------------------------------------------------------------------------
  // API класса
  //

  /**
   * Добавляет компоненту в контекст.
   *
   * @param aAppApi {@link IAppComponent} - дотуп к специфичному для проекта функционалу
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setAppApi( IAppComponent aAppApi ) {
    appApi = TsNullArgumentRtException.checkNull( aAppApi );
  }

  /**
   * Добавляет компоненту в контекст.
   *
   * @param aHal {@link IHal} - дотуп к железу БУ
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setHal( IHal aHal ) {
    hal = TsNullArgumentRtException.checkNull( aHal );
  }

  /**
   * Добавляет компоненту в контекст.
   *
   * @param aDlmManager {@link IDlmManager} - управление загружаемыми модулями
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setDlmManager( IDlmManager aDlmManager ) {
    dlmManager = TsNullArgumentRtException.checkNull( aDlmManager );
  }

  /**
   * Добавляет компоненту в контекст.
   *
   * @param aNetwork {@link INetwork} - доступ в ВУ
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setNetwork( INetwork aNetwork ) {
    network = TsNullArgumentRtException.checkNull( aNetwork );
  }

}

package ru.toxsoft.l2.core.dlm.impl;

import java.io.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.main.*;
import ru.toxsoft.l2.core.main.impl.*;

/**
 * Реализация интерфейса {@link IDlmManagerComponent} - менеджера управления жизненым циклом загружаемых модулей.
 *
 * @author goga
 */
class DlmManager
    extends AbstractL2Component
    implements IDlmManagerComponent {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * "Держатели" для каждой области работы загружаемых модулй.
   */
  private final DlmHolder dlmHolder;

  /**
   * Интервали между проверками обновлений плагинов.
   * <p>
   * Интервал проверок модулей задается в параметре {@link EGlobalOps#PLUGINS_RESCAN_INTERVAL_SECS}. То есть, с момента,
   * как файл плагина поменялся, должно пройти не более заданого количества секунд до обновления соответствующего модуля
   * в памяти. Если у нас несколько директорий с плагинами, то распределим проверку каждой директории равномерно по
   * заданному интервалу.
   * <p>
   * Поэтому, интервал между проверками будет {@link EGlobalOps#PLUGINS_RESCAN_INTERVAL_SECS} / (кол-во директорий ).
   */
  private final long dlmHolderCheckInterval;

  /**
   * Метка времени, когда в последный раз делалась проверка модулей.
   */
  private long lastModulesCheckTime = 0;

  /**
   * Создает компоненту для работы в контексте.
   *
   * @param aContext {@link GlobalContext} - глобальный контекст работы компоненты
   */
  DlmManager( GlobalContext aContext ) {
    super( aContext );
    aContext.setDlmManager( this );

    dlmHolderCheckInterval = 1000 * context.globalOps().getInt( EGlobalOps.PLUGINS_RESCAN_INTERVAL_SECS );
    File dir = new File( context.globalOps().getStr( EGlobalOps.PLUGINS_DIR ) );

    dlmHolder = new DlmHolder( context, dir );
  }

  // ------------------------------------------------------------------------------------
  // Внутренные методы
  //

  // ------------------------------------------------------------------------------------
  // Реализация методов жизненного цикла AbstractT7Component
  //

  @Override
  protected void processStart() {
    dlmHolder.start();

  }

  @Override
  protected void processRunStep() {
    // выполним методы doJob() каждого модуля
    dlmHolder.doJob();

    checkPluginChanges();
  }

  private void checkPluginChanges() {
    // проверим модули на измененя в плагинах
    if( (System.currentTimeMillis() - lastModulesCheckTime) > dlmHolderCheckInterval ) {

      // TODO - возвращает ещё не остановленные моудли - сейчас с ними ничегон не делается,
      // но дожно делаться

      // by Max 2015.04.01 - отлов ошибки
      try {
        dlmHolder.checkPluginChanges();
      }
      catch( Exception e ) {
        logger.error( e, "Error during dlms change checking" ); //$NON-NLS-1$
      }

      lastModulesCheckTime = System.currentTimeMillis();
    }
  }

  @Override
  protected boolean processStopQuery() {
    boolean isStopped = true;
    logger.info( "processing Stop Query Dlm manager" ); //$NON-NLS-1$
    if( !dlmHolder.queryStop() ) {
      isStopped = false;
    }

    logger.info( "process Stop Query Dlm manager with result: %s", String.valueOf( isStopped ) ); //$NON-NLS-1$

    return isStopped;
  }

  @Override
  protected boolean processStopStep() {
    boolean isStopped = true;
    logger.info( "processing Stop Step Dlm manager" ); //$NON-NLS-1$
    if( !dlmHolder.isStopped() ) {
      isStopped = false;
    }

    logger.info( "process Stop Step Dlm manager with result: %s", String.valueOf( isStopped ) ); //$NON-NLS-1$

    return isStopped;
  }

  @Override
  protected void processDestroy() {
    logger.info( "processing Destroy Dlm manager" ); //$NON-NLS-1$
    dlmHolder.destroy();
    logger.info( "destroyed Dlm manager" ); //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDlmManager
  //

  // @Override
  // public IDlm findByModuleId( String aModuleId )
  // throws TsItemNotFoundRtException {
  // return dlmHolder.findByModuleId( aModuleId );
  // }

  @Override
  public IList<IDlm> modules() {
    return dlmHolder.modules();
  }
}

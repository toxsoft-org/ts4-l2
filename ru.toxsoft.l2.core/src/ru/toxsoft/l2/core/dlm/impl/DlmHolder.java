package ru.toxsoft.l2.core.dlm.impl;

import static ru.toxsoft.l2.core.dlm.impl.IL2Resources.*;
import static ru.toxsoft.l2.core.main.IL2HardConstants.*;

import java.io.File;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.bricks.ICooperativeWorkerComponent;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsVersion;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.core.tslib.utils.plugins.IChangedPluginsInfo.IChangedPluginInfo;
import org.toxsoft.core.tslib.utils.plugins.impl.PluginUtils;

import ru.toxsoft.l2.core.cfg.IUnitConfig;
import ru.toxsoft.l2.core.cfg.impl.DefaultUnitConfigLoader;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.main.EGlobalOps;
import ru.toxsoft.l2.core.main.IGlobalContext;
import ru.toxsoft.l2.core.util.WorkerComponentBase;

/**
 * Внутренный для {@link DlmManager} класс, работающий с динамической загрузкой модулей.
 *
 * @author goga
 */
final class DlmHolder
    extends WorkerComponentBase
    implements ICooperativeWorkerComponent {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Общий контекст программы.
   */
  private final IGlobalContext globalContext;

  private final IPluginsStorage storage = PluginUtils.createPluginStorage( DLM_PLUGIN_TYPE_ID );
  private final IListEdit<IDlm> dlms    = new ElemArrayList<>();

  private final IDlmContext dlmContext;

  /**
   * Расширение имени файла с конфигурацией DLM.
   */
  public static final String DLM_CFG_FILE_EXTENSION = "dlmcfg"; //$NON-NLS-1$

  /**
   * Ключевое слово, с которого начинается кофигурация DLM.
   */
  public static final String KEYWORD_DLMCFG = "DlmConfig"; //$NON-NLS-1$

  /**
   * Загрузчик конфигураций DLM - TODO - продумать способ обновлений конфигураций при обновлении DLM
   */
  private DefaultUnitConfigLoader dlmConfigLoader =
      new DefaultUnitConfigLoader( DLM_CFG_FILE_EXTENSION, KEYWORD_DLMCFG );

  /**
   * Директория где лежат конфигурации DLM
   */
  private final File dlmConfigDir;

  DlmHolder( IGlobalContext aContext, File aPluginsDir ) {
    globalContext = aContext;

    dlmConfigDir = new File( globalContext.globalOps().getStr( EGlobalOps.DLM_CONFIG_DIR ) );

    dlmContext = new DlmContext( aContext );

    storage.addPluginJarPath( aPluginsDir, false );
    // загрузим плагины, доступные на данный момент
    // for( IPluginInfo i : storage.listPlugins() ) {
    // dlms.add( createPlugin( i ) );
    // }
  }

  // ------------------------------------------------------------------------------------
  // Внутренные методы
  //

  private IDlm createPlugin( IPluginInfo aPi ) {
    try {
      IDlmFactory dlmFactory = storage.loadPlugin( aPi.pluginId() ).instance( IDlmFactory.class );
      dlmFactory.setPluginIngfo( aPi );
      for( IDlm d : dlms ) {
        if( d.info().moduleId().equals( dlmFactory.info().moduleId() ) ) {
          throw new TsItemAlreadyExistsRtException( MSG_ERR_DLM_ALREADY_EXISTS, d.info().moduleId(),
              d.info().moduleName() );
        }
      }
      IDlm dlm = dlmFactory.createDlm( dlmContext );
      TsInternalErrorRtException.checkNull( dlm );
      return dlm;
    }
    catch( Exception e ) {
      logger.error( e, MSG_ERR_CANT_INIT_PLUGIN, aPi.pluginId(), aPi.pluginJarFileName() );
      return null;
    }
  }

  /**
   * Находит загруженный модуль по описанию плагина - источника модуля и удаляет его из внутреннего списка.
   * <p>
   * Модуль идентифицируется строкой идентификатора {@link IDlmInfo#moduleId()}.
   * <p>
   * Внимание: поиск НЕ учитывает версию плагина, ведь метод служит для посика существующих модулей, при появлении
   * плагина с модулем более новой версии.
   *
   * @param aPi {@link IPluginInfo} - описания плагина, загрузивего модуль
   * @return {@link IDlm} - удаленный из списка модуль
   */
  private IDlm removeDlmForPlugin( IPluginInfo aPi ) {
    String moduleId = aPi.pluginId();
    for( int i = 0, n = dlms.size(); i < n; i++ ) {
      IDlm m = dlms.get( i );
      if( m.info().moduleId().equals( moduleId ) ) {
        dlms.remove( m );
        return m;
      }
    }
    // нельзя тут оказаться, модуль обязательно должен быть в списке!
    throw new TsInternalErrorRtException();
  }

  /**
   * Стартует указанный модуль, от {@link IDlm#start()} отличается только обработкой исключений.
   *
   * @param aDlm {@link IDlm} - запускаемый модуль
   * @return boolean - признак, что модуль успешно стартовал (т.е. не было исключений)
   */
  private boolean startDlm( IDlm aDlm ) {
    // Получаем конфигурацию запускаемого DLM
    IUnitConfig config = dlmConfigLoader.getUnitConfig( aDlm.info().moduleId() );

    try {
      // если конфигурация существует - делаем попытку конфигурировать модуль
      if( config != null ) {
        logger.info( "configuration DLM %s", aDlm.info().moduleId() ); //$NON-NLS-1$
        aDlm.configYourself( config );
        logger.info( "starting DLM %s", aDlm.info().moduleId() ); //$NON-NLS-1$
        aDlm.start();
        logger.info( "started DLM %s", aDlm.info().moduleId() ); //$NON-NLS-1$
        return true;
      }

      logger.error( "xml-config file of DLM %s is not found", aDlm.info().moduleId() ); //$NON-NLS-1$
      return false;

    }
    catch( Exception e ) {
      logger.error( e, MSG_ERR_DLM_CANT_START, aDlm.info().moduleName() );
      return false;
    }
  }

  /**
   * Запрашивает остановку модуля, от {@link IDlm#queryStop()} отличается только обработкой исключений.
   * <p>
   * Метод возвращает признак успешной остановки. Успешная считается остановка, если {@link IDlm#queryStop()} вернул
   * true, а безуспешным - когда вернул false. В случае, если возникло исключение при остановке модуля, метод тоже
   * вернет <code>true</code>, поскольку дальнейшие попытки "достучаться" до модуля считаются бессымысленными. Остается
   * только вызвать {@link IDlm#destroy()}.
   *
   * @param aDlm {@link IDlm} - останавливаемый модуль
   * @return boolean - признак, что модуль успешно остановился
   */
  private boolean queryDlmStop( IDlm aDlm ) {
    try {
      logger.info( "query stopping DLM %s", aDlm.info().moduleId() ); //$NON-NLS-1$
      boolean queryStop = aDlm.queryStop();
      logger.info( "query stop DLM %s - with result: %s", aDlm.info().moduleId(), String.valueOf( queryStop ) ); //$NON-NLS-1$

      return queryStop;
    }
    catch( Exception e ) {
      logger.error( e, MSG_ERR_EXCEPTION_DURING_DLM_STOP_QUERY, aDlm.info().moduleName() );
      return true;
    }
  }

  /**
   * Запрашивает состояние остановки модуля, от {@link IDlm#isStopped()} отличается только обработкой исключений.
   * <p>
   * Метод возвращает признак успешной остановки. Успешная считается остановка, если {@link IDlm#isStopped()} вернул
   * true, а безуспешным - когда вернул false. В случае, если возникло исключение в процессе остановки модуля, метод
   * тоже вернет <code>true</code>, поскольку дальнейшие попытки "достучаться" до модуля считаются бессымысленными.
   * Остается только вызвать {@link IDlm#destroy()}.
   *
   * @param aDlm {@link IDlm} - останавливаемый модуль
   * @return boolean - признак, что модуль успешно остановился
   */
  private boolean isDlmStopped( IDlm aDlm ) {
    try {
      return aDlm.isStopped();
    }
    catch( Exception e ) {
      logger.error( e, MSG_ERR_EXCEPTION_DURING_DLM_STOPPING, aDlm.info().moduleName() );
      return true;
    }
  }

  /**
   * Уничтожает модуль, от {@link IDlm#destroy()} отличается только обработкой исключений.
   *
   * @param aDlm {@link IDlm} - уничтожаемый модуль
   */
  private void destroyDlm( IDlm aDlm ) {
    try {
      logger.info( "destroying DLM %s", aDlm.info().moduleId() ); //$NON-NLS-1$
      aDlm.destroy();
      logger.info( "destroyed DLM %s", aDlm.info().moduleId() ); //$NON-NLS-1$
    }
    catch( Exception e ) {
      logger.error( e, MSG_ERR_EXCEPTION_DURING_DLM_DESTROY, aDlm.info().moduleName() );
    }
  }

  /**
   * Дает поработать модулю, от {@link IDlm#doJob()} отличается только обработкой исключений.
   *
   * @param aDlm {@link IDlm} - работающий модekm модуль
   */
  private void doDlmJob( IDlm aDlm ) {
    try {
      aDlm.doJob();
    }
    catch( Exception e ) {
      logger.error( e, MSG_ERR_EXCEPTION_DURING_DLM_DO_JOB, aDlm.info().moduleName() );
    }
  }

  private static String pi2str( IPluginBasicInfo aPi ) {
    return aPi.pluginId() + '-' + TsVersion.getVersionNumber( aPi.pluginVersion() );
  }

  /**
   * Логирует информацию о смене плагинов.
   *
   * @param aCpi {@link IChangedPluginsInfo} - изменения в лпгаинах
   */
  private void logChanges( IChangedPluginsInfo aCpi ) {
    // ILogger logger = globalContext.logger();
    for( int i = 0; i < aCpi.listAddedPlugins().size(); i++ ) {
      IPluginBasicInfo pi = aCpi.listAddedPlugins().get( i );
      logger.info( MSG_INFO_NEW_PLUGIN, pi2str( pi ) );
    }
    for( int i = 0; i < aCpi.listChangedPlugins().size(); i++ ) {
      IChangedPluginInfo cpi = aCpi.listChangedPlugins().get( i );
      logger.info( MSG_INFO_CHANGED_PLUGIN, pi2str( cpi.pluginInfo() ),
          TsVersion.getVersionNumber( cpi.oldVersion() ) );
    }
    for( int i = 0; i < aCpi.listRemovedPlugins().size(); i++ ) {
      IPluginBasicInfo pi = aCpi.listRemovedPlugins().get( i );
      logger.info( MSG_INFO_PLUGIN_REMOVED, pi2str( pi ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса WorkerComponentBase
  //

  @Override
  protected void doStartComponent() {
    logger.info( "List of jar-files that are DLMs (they contain all essential dlm information):" ); //$NON-NLS-1$
    // загрузим плагины, доступные на данный момент
    for( IPluginInfo i : storage.listPlugins() ) {
      logger.info( "jar: %s, DLM: %s", i.pluginJarFileName(), i.pluginId() ); //$NON-NLS-1$
      IDlm dlm = createPlugin( i );
      if( dlm != null ) {
        dlms.add( dlm );
        logger.info( "added successfully" ); //$NON-NLS-1$
      }
    }

    // загрузить конфигурации DLM
    if( dlms.size() > 0 ) {
      dlmConfigLoader.loadConfig( dlmConfigDir );
    }

    for( int i = 0, n = dlms.size(); i < n; i++ ) {
      startDlm( dlms.get( i ) );
    }
  }

  @Override
  protected boolean doQueryStop() {
    boolean isStopped = true;
    for( int i = 0, n = dlms.size(); i < n; i++ ) {
      if( !queryDlmStop( dlms.get( i ) ) ) {
        isStopped = false;
      }
    }

    if( isStopped ) {
      dlms.clear();
    }
    return isStopped;
  }

  @Override
  protected boolean doStopStep() {
    boolean isStopped = true;
    for( int i = 0, n = dlms.size(); i < n; i++ ) {
      if( !isDlmStopped( dlms.get( i ) ) ) {
        isStopped = false;
      }
    }

    if( isStopped ) {
      dlms.clear();
    }
    return isStopped;
  }

  @Override
  protected void doDestrоyComponent() {
    for( int i = 0, n = dlms.size(); i < n; i++ ) {
      destroyDlm( dlms.get( i ) );
    }
    dlms.clear();
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IWorkerComponent
  //

  @Override
  public void doJob() {
    for( int i = 0, n = dlms.size(); i < n; i++ ) {
      doDlmJob( dlms.get( i ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // API класса (дополнительно к ICooperativeWorkerComponent)
  //

  public IList<IDlm> dlms() {
    return dlms;
  }

  /**
   * Проверят, и при необходимости перегружает модули по изменениям в плагинах
   * <p>
   * Метод возвращает перечень модулей, которые были удалены из внутреннего списка и которые еще не остановлены. При
   * обнаружении более новой версии (или просто при удалении плагина-источника), старый модуль удалется из внутреннего
   * списка, и его метод {@link IDlm#doJob()} перестает вызываться. Кроме того, сразу же, в этом методу вызвается
   * {@link IDlm#queryStop()}. Если модуль остановился не сразу, то он будет включен в возвращаемый список.
   * <p>
   * TODO сейчас еще не решено, что делать для корректного завершения работы таких модулей: вызвать сразу
   * {@link IDlm#destroy()}? или в менджере модулей организовать отдельный цикл вызовов {@link IDlm#isStopped()} до
   * таймаута? При этом, ведь новые версии модулей уже будут работать?
   * <p>
   * TODO <b>ВНИМАНИЕ:</b> кто-то обязан вызвать для этих модулей {@link IDlm#destroy()} !!!
   *
   * @return IList&lt;{@link IDlm}&gt; - удаленные из внутреннего списка устаревшие еще не остановленные модули
   */
  public IList<IDlm> checkPluginChanges() {

    IListEdit<IDlm> result = IList.EMPTY;
    storage.checkChanges();
    IChangedPluginsInfo cpi = storage.getChanges();
    if( !cpi.isChanges() ) { // нет изменений в составе плагинов, выходим
      return result;
    }
    logChanges( cpi );
    IListEdit<IPluginInfo> newPlugins = new ElemArrayList<>( cpi.listAddedPlugins() );
    IListEdit<IPluginInfo> oldPlugins = new ElemArrayList<>( cpi.listRemovedPlugins() );
    IList<IChangedPluginInfo> changedPlugins = cpi.listChangedPlugins();
    for( int i = 0, n = changedPlugins.size(); i < n; i++ ) {
      IChangedPluginInfo pi = changedPlugins.get( i );
      oldPlugins.add( pi.pluginInfo() ); // списк удаляемых, поскольку старую вырсию надо
                                         // выгрузить
      newPlugins.add( pi.pluginInfo() ); // список добавляемых, поскольку текущую версию
                                         // надо загрузить
    }
    // сначала удалим старые версии
    for( int i = 0, n = oldPlugins.size(); i < n; i++ ) {
      IDlm removedDlm = removeDlmForPlugin( oldPlugins.get( i ) );
      TsInternalErrorRtException.checkNull( removedDlm );
      if( removedDlm.queryStop() ) { // модуль успешно остановлен (или было исключение)
        destroyDlm( removedDlm );
      }
      else { // модуль не остановился сразу
        if( result == IList.EMPTY ) { // создадим список только если действительно есть
                                      // что туда класть
          result = new ElemArrayList<>();
        }
        result.add( removedDlm );
      }
    }

    // если есть новые DLM - следует обновить конфигурации - загрузить их заново
    if( newPlugins.size() > 0 ) {
      dlmConfigLoader.loadConfig( dlmConfigDir );
    }

    // потом добавим новые версии
    for( int i = 0, n = newPlugins.size(); i < n; i++ ) {
      IDlm newDlm = createPlugin( newPlugins.get( i ) );
      if( startDlm( newDlm ) ) {
        dlms.add( newDlm );
      }
    }
    return result;
  }

  /**
   * Находит модуль по его идентификатору.
   *
   * @param aModuleId - идентификатор модуля.
   * @return модуль.
   * @throws TsItemNotFoundRtException - в случае отсутствия модуля с таким идентификатором.
   */
  IDlm findByModuleId( String aModuleId )
      throws TsItemNotFoundRtException {
    for( IDlm dlm : dlms ) {
      if( dlm.info().moduleId().equals( aModuleId ) ) {
        return dlm;
      }
    }
    throw new TsItemNotFoundRtException( MSG_ERR_DLM_NOT_FOUND, aModuleId );
  }

  public IList<IDlm> modules() {
    return dlms;
  }

}

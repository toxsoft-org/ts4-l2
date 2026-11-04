package org.toxsoft.l2.lib.api.module.hal;

import static org.toxsoft.l2.lib.IL2HardConstants.*;
import static org.toxsoft.l2.lib.api.app.IL2ApplicationArguments.*;
import static org.toxsoft.l2.lib.api.module.IL2ModuleConfigConstants.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import java.util.concurrent.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.math.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.devel.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * {@link IL2HalDriver} implementation base.
 *
 * @author hazard157
 */
public abstract class L2AbstractHalDriver
    extends L2AbstractModule
    implements IL2HalDriver {

  private static final IntRange PRIORITY_RANGE = new IntRange( Thread.MIN_PRIORITY, Thread.MAX_PRIORITY );

  private final L2Hal                      owner;
  private final IStridablesListEdit<L2Tag> tagsList = new StridablesList<>();

  /**
   * Lower half is created by the subclass in {@link #init(ITsContextRo)}.
   */
  private L2AbstractHDLH lowerHalf = null;

  /**
   * Hardware interaction is performed in this thread.<br>
   * Initialized and started in {@link #doStart()}.
   */
  private Thread hardwareIoThread = null;

  /**
   * Constructor.
   *
   * @param aOwner {@link L2AbstractComponent} - the owner component creating the module
   * @param aConfig {@link L2ModuleConfig} - the module configuration
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException owner is not {@link L2Hal}
   */
  public L2AbstractHalDriver( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig ) {
    super( aOwner, aConfig );
    owner = L2Hal.class.cast( aOwner );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Loads all {@link L2TagInfo} from the config node {@link IL2ModuleConfigConstants#L2_HAL_MODCFG_NODEID_TAGINFOS}.
   *
   * @return {@link IList}&lt;{@link L2TagInfo}&gt; - all declared tags for this module
   */
  private IList<L2TagInfo> getTagInfos() {
    String nodeId = L2_HAL_MODCFG_NODEID_TAGINFOS;
    IAvTree tagInfosNode = moduleConfig().cfg().nodes().findByKey( nodeId );
    TsItemNotFoundRtException.checkNull( tagInfosNode, FMT_ERR_NO_TAGS_NODE_IN_CFG, id(), nodeId );
    TsIllegalStateRtException.checkTrue( tagInfosNode.isArray(), FMT_ERR_TAGS_NODE_IS_ARRAY, id(), nodeId );
    IListEdit<L2TagInfo> llInfos = new ElemArrayList<>( tagInfosNode.fields().size() );
    for( String fieldId : tagInfosNode.fields().keys() ) {
      String tagInfoKtor = tagInfosNode.fields().getStr( fieldId );
      L2TagInfo tagInfo;
      try {
        tagInfo = L2TagInfo.KEEPER.str2ent( tagInfoKtor );
      }
      catch( Exception ex ) {
        throw new TsIoRtException( ex, FMT_ERR_CFG_FIELD_NOT_TAGINFO, id(), nodeId, fieldId, L2TagInfo.KEEPER_ID );
      }
      if( !fieldId.equals( tagInfo.id() ) ) {
        throw new TsIoRtException( FMT_ERR_FIELD_ID_NOT_TAGINFO_ID, id(), nodeId, fieldId, tagInfo.id() );
      }
      llInfos.add( tagInfo );
    }
    return llInfos;
  }

  /**
   * Creates {@link L2AbstractHDLH} using {@link #doCreateLowerHalf()} with all necessary checks.
   *
   * @return {@link L2AbstractHDLH} - created and correct instance
   * @throws TsInternalErrorRtException lower part is not correctly created
   */
  private L2AbstractHDLH createLowerHalf() {
    L2AbstractHDLH lh = doCreateLowerHalf();
    TsInternalErrorRtException.checkNull( lh );
    TsInternalErrorRtException.checkTrue( lh.owner() != this );
    return lh;
  }

  /**
   * Determines {@link #hardwareIoThread} priority from applcation and module options.
   *
   * @return int - thread priority checked in range {@link #PRIORITY_RANGE}.
   */
  private int getHardIoThreadPriority() {
    int p = OPDEF_HAL_HARDIO_THREAD_DEFAULT_PRIORITY.getValue( compInitArgs().params() ).asInt();
    p = params().getInt( OPID_HALDRV_HARD_IO_THREAD_PRIORITY, p );
    return PRIORITY_RANGE.inRange( p );
  }

  private void runHardwareIoThread() {
    // initialize hardware
    ValidationResult vr;
    try {
      vr = lowerHalf.initHardware();
    }
    catch( Exception ex ) {
      logger().error( ex );
      vr = ValidationResult.error( ex );
    }
    vr.logTo( logger() ); // log only non-OK
    // hardware initialization error make driver unusable
    if( vr.isError() ) {
      setDoJobCallable( false ); // exclude driver from working loop
      logger().error( FMT_ERR_HALDRV_IGNORED_ON_INIT_FAIL, vr.message() );
      return; // finish the thread
    }
    // main loop
    mainLoop();
    // normally reset hardware
    try {
      lowerHalf.closeHardware();
    }
    catch( Exception ex ) {
      logger().error( ex );
    }
  }

  private void mainLoop() {

    // TODO L2AbstractHalDriver.runHardwareIoLoop()

    if( compState() == ETsCoopCompState.STOPPING ) {
      // TODO ???
      return;
    }
    if( Thread.interrupted() ) {
      // TODO log it
      return;
    }

  }

  // ------------------------------------------------------------------------------------
  // L2AbstractModule
  //

  @Override
  final public L2Hal owner() {
    return owner;
  }

  @Override
  final protected ValidationResult doInit( ITsContextRo aArgs ) {
    // create tag instances
    IList<L2TagInfo> llInfos = getTagInfos();
    for( L2TagInfo tinf : llInfos ) {
      L2Tag tag = new L2Tag( this, tinf.id(), tinf.kind(), tinf.atomicType(), tinf.params() );
      tagsList.add( tag );
    }
    // lower half creation
    lowerHalf = createLowerHalf();
    // subclass initialization
    return doDoInit( aArgs );
  }

  @Override
  final protected void doStart() {
    // create and start hardware I/O thread
    hardwareIoThread = new Thread( this::runHardwareIoThread );
    String threadName = String.format( L2_HALDRV_HARDIO_THREAD_NAME_FORMAT, id() );
    hardwareIoThread.setName( threadName );
    hardwareIoThread.setPriority( getHardIoThreadPriority() );
    hardwareIoThread.start();
    logger().debug( FMT_DBG_HALDRV_HARD_IO_THREAD_STARTED, threadName );
  }

  @Override
  protected abstract void doDoJob();

  @Override
  protected abstract boolean doQueryStop();

  @Override
  protected abstract boolean doStopping();

  @Override
  final protected void doDestroy() {
    // forcibly finish the hardware I/O thread
    if( hardwareIoThread != null ) {
      hardwareIoThread.interrupt();
      hardwareIoThread = null;
    }
    doDoDestroy();
  }

  // ------------------------------------------------------------------------------------
  // IL2HalDriver
  //

  @Override
  public IStridablesList<L2Tag> tags() {
    return tagsList;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  /**
   * Subclass must create the driver lower half.
   *
   * @return {@link L2AbstractHDLH} - created instance
   */
  protected abstract L2AbstractHDLH doCreateLowerHalf();

  /**
   * Subclass must perform initialization, usually prepare the hardware.
   * <p>
   * Called from {@link #init(ITsContextRo)} after all declared tags and a n HDLH were created successfully.
   * <p>
   * Note: do <b>not</b> work with the hardware in this method!
   *
   * @param aArgs {@link ITsContextRo} - the initialization arguments
   * @return {@link ValidationResult} - determines the success of the initialization
   */
  protected abstract ValidationResult doDoInit( ITsContextRo aArgs );

  /**
   * Subclass may perform additional cluec-up when module is destroyed.
   * <p>
   * Note: do <b>not</b> work with the hardware in this method!
   * <p>
   * Does nothing in base class, there is no need to call superclass method when overriding.
   */
  protected void doDoDestroy() {
    // nop
  }

  // --------------------------------------------------------------------
  // by Max - наброски
  //

  protected enum ENextMethod {
    READ_DEVICE,
    WRITE_VALUES,
    READ_VALUES,
    ANY
  }

  /**
   * Указание на то, какой метод должен быть вызван контейнером. вызов другого метода будет проигнорирован.
   */
  protected ENextMethod nextMethod = ENextMethod.ANY;

  /**
   * Сервис управления вспомогательным потоком записи-чтения с физ уровня.
   */
  private ExecutorService executor;

  /**
   * Результат выполнения текущей задачи обмена данными с устройством.
   */
  private Future<?> result;

  /**
   * Запускает сервис управления вспомогательным потоком записи-чтения с физ уровня.
   */
  public void startExecutor() {
    executor = Executors.newSingleThreadExecutor();
  }

  /**
   * Начинает процесс закрытия (освобождения ресурсов). Должен забирать управление на короткое время.
   */
  public void closeExecutor() {
    // TODO - надо ли проверять состояние закрытия, т.е. периодически проверять closeResult
    Future<?> closeResult = executor.submit( () -> {

      lowerHalf.closeHardware();

    } );

    executor.shutdown();
  }

  /**
   * Небезопасное завершение работы драйвера. Не гарантирует остановки драйвера, сообщая, что драйвер завершил работу.
   */
  public void destroyExecutor() {
    executor.shutdownNow();
  }

  /**
   * Проверяет закрытие (освобождение ресурсов). Должен забирать управление на короткое время.
   *
   * @return boolean - <b>true</b> - спец оборудование закрыто,
   *         <p>
   *         <b>false</b> - спец оборудование не закрыто.
   */
  public boolean isExecutorClosed() {
    return executor.isShutdown();
  }

  /**
   * Записывает текущие значения в буфер и инициирует процесс записи знчений из буфера на физ. уровень. Если выполнение
   * метода невозможно в силу того, что предыдущий процесс записи или чтения не был завершён, будет вызван метод
   * {@link #writeValuesWrongMode()}.
   */
  public void writeValues() {
    if( result == null || checkIsDone() ) {
      if( nextMethod == ENextMethod.ANY || nextMethod == ENextMethod.WRITE_VALUES ) {
        nextMethod = ENextMethod.ANY;

        // TODO - сюда нужно передать теги
        lowerHalf.getFromTagsToInternalBuffer( null );
        result = executor.submit( () -> lowerHalf.sendBufferValuesToHardware() );
      }
    }
    else {
      if( nextMethod == ENextMethod.ANY ) {
        nextMethod = ENextMethod.WRITE_VALUES;
      }

      writeValuesWrongMode();
    }
  }

  /**
   * Инициирует процесс чтения значений с физ. уровня. Если выполнение метода невозможно в силу того, что предыдущий
   * процесс записи или чтения не был завершён, будет вызван метод {@link #readDevicesWrongMode()}.
   */
  public void readDevices() {
    if( result == null || checkIsDone() ) {
      if( nextMethod == ENextMethod.ANY || nextMethod == ENextMethod.READ_DEVICE ) {
        nextMethod = ENextMethod.ANY;

        result = executor.submit( () -> lowerHalf.receiveBufferValuesFromHardware() );
      }
    }
    else {
      if( nextMethod == ENextMethod.ANY ) {
        nextMethod = ENextMethod.READ_DEVICE;
      }

      readDevicesWrongMode();
    }
  }

  /**
   * Читает значения из буфера. Если выполнение метода невозможно в силу того, что предыдущий процесс записи или чтения
   * не был завершён, будет вызван метод {@link #readValuesWrongMode()}.
   */
  public void readValues() {
    if( result == null || checkIsDone() ) {
      if( nextMethod == ENextMethod.ANY || nextMethod == ENextMethod.READ_VALUES ) {
        nextMethod = ENextMethod.ANY;

        // TODO - сюда нужно передать теги
        lowerHalf.setToTagsFromInternalBuffer( null );
      }
    }
    else {
      if( nextMethod == ENextMethod.ANY ) {
        nextMethod = ENextMethod.READ_VALUES;
      }

      readValuesWrongMode();
    }
  }

  /**
   * Проверяем, что задача, выполняемая в отдельном потоке завершена. И если завершена с ошибкой - передать ошибку на
   * обработку
   *
   * @return boolean - задача в отдельном потоке завершена (можно начинать следующую)
   */
  private boolean checkIsDone() {
    if( result != null && result.isDone() ) {
      try {
        try {
          result.get();
        }
        catch( InterruptedException ex ) {
          // TODO - что то надо сделать
          ex.printStackTrace();
        }
      }
      catch( ExecutionException e ) {
        // отправка ошибки задачи на обработку
        onError( e.getCause() );
      }
      return true;

    }
    return false;
  }

  /**
   * Метод обработки ошибки обмена с физ уровнем
   *
   * @param aError Throwable - ошибка обмена с физ уровнем.
   */
  protected void onError( Throwable aError ) {
    // TODO
  }

  //
  // ----------------------------------------------------------
  // методы обработки исключительных ситуаций, появляющихся в конкретных методах

  /**
   * Метод обработки исключения: метод {@link #writeValues()} вызван в тот момент, когда не был завершён предыдущий
   * процесс чтения (записи) с физ. уровня.
   */

  protected void writeValuesWrongMode() {
    // по мере необходимости реализуется в наследниках
  }

  /**
   * Метод обработки исключения: метод {@link #readDevices()} вызван в тот момент, когда не был завершён предыдущий
   * процесс чтения (записи) с физ. уровня.
   */
  protected void readDevicesWrongMode() {
    // по мере необходимости реализуется в наследниках
  }

  /**
   * Метод обработки исключения: метод {@link #readValues()} вызван в тот момент, когда не был завершён предыдущий
   * процесс чтения (записи) с физ. уровня.
   */
  protected void readValuesWrongMode() {
    // по мере необходимости реализуется в наследниках
  }

}

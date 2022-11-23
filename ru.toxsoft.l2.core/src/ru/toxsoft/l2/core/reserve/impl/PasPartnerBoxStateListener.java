package ru.toxsoft.l2.core.reserve.impl;

import static org.toxsoft.core.pas.server.IPasServerParams.*;
import static ru.toxsoft.l2.core.reserve.impl.IReserveParams.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.pas.json.*;
import org.toxsoft.core.pas.server.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.reserve.impl.pas.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Реализация слушателя изменения состояния шкафа напарника с помощью библиотеки PAS
 *
 * @author max
 */
public class PasPartnerBoxStateListener
    extends WorkerComponentBase
    implements IPartnerBoxStateListener {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( PasPartnerBoxStateListener.class.getName() );

  private PasServer<?> hotSwapReader = null;

  private Object hotSwapReaderLocker = new Object();

  private EPartnerBoxReserveState state = EPartnerBoxReserveState.UNKNOWN;

  /**
   * Время последнего получения состояния напарника
   */
  private long lastUpdate = Long.MAX_VALUE;

  /**
   * Время задержки получения состояния партнёра - если за это время не получено состояние напарника - считать его
   * состояние неизвестным
   */
  private long updateTimeout = 3000;

  /**
   * Промежуток между попытками зарегистрировать pas сервер на прослушивание
   */
  private long pasServerConnectionTryPeriod = 3000;

  private final ITsContext context;

  private volatile boolean stopping = false;

  private volatile boolean stopped = false;

  /**
   * Поток регистрации сервера на прослушивание
   */
  private Thread passRegThread;

  /**
   * Конструктор по контексту
   *
   * @param aContext ITsContext - контекст.
   */
  public PasPartnerBoxStateListener( ITsContext aContext ) {
    context = aContext;
  }

  @Override
  protected void doStartComponent() {
    context.put( PasPartnerBoxStateListener.class, this );
    updateTimeout = OP_PARTNER_MESSAGES_TIMEOUT.getValue( context.params() ).asInt();
    pasServerConnectionTryPeriod = OP_PAS_SERVER_REG_TRY_PERIOD.getValue( context.params() ).asInt();
    // создание и запуск читателя HotSwap
    // hotSwapReader = startHotSwapReader( context );

    passRegThread = new Thread( () -> {
      while( !stopping ) {
        PasServer<?> hsr = startHotSwapReader( context );
        if( hsr != null ) {
          if( stopping ) {
            hsr.close();
            stopped = true;
            // logger.debug( "Pas server Extra stopped" ); //$NON-NLS-1$
            return;
          }
          setHotSwapReader( hsr );
          // logger.debug( "Pas server registered" ); //$NON-NLS-1$
          return;
        }

        try {
          Thread.sleep( pasServerConnectionTryPeriod );
        }
        catch( InterruptedException e ) {
          logger.debug( "Pas server reg interrapted (stopped=%b, stopping=%b)", stopped, stopping ); //$NON-NLS-1$
          if( stopping ) {
            stopped = true;
          }
          logger.error( e );
          return;
        }
      }
      // logger.debug( "Pas server stopped by stopping" ); //$NON-NLS-1$
      stopped = true;
    } );

    passRegThread.start();
  }

  private PasServer<?> getHotSwapReader() {
    synchronized (hotSwapReaderLocker) {
      return hotSwapReader;
    }
  }

  private void setHotSwapReader( PasServer<?> hotSwapReader ) {
    synchronized (hotSwapReaderLocker) {
      this.hotSwapReader = hotSwapReader;
    }
  }

  @Override
  protected boolean doQueryStop() {
    PasServer<?> hsr = getHotSwapReader();
    if( hsr != null ) {
      Thread t = new Thread( () -> {
        hsr.close();
        // logger.debug( "Pas server stopped odinary" ); //$NON-NLS-1$
        stopped = true;
      } );
      t.start();
    }
    else {
      stopping = true;
      stopped = !passRegThread.isAlive();
    }
    // logger.debug( "Pas server doQueryStop %b (stopping = %b)", stopped, stopping ); //$NON-NLS-1$
    return stopped;
  }

  @Override
  protected boolean doStopStep() {
    // logger.info( "!! Pas server doStopStep %b (stopping = %b)", stopped, stopping ); //$NON-NLS-1$
    return stopped;
  }

  @Override
  public void doJob() {
    if( System.currentTimeMillis() - lastUpdate > updateTimeout ) {
      setPartnerBoxState( EPartnerBoxReserveState.UNKNOWN );
    }

  }

  @Override
  public synchronized EPartnerBoxReserveState getPartnerBoxState() {
    return state;
  }

  private synchronized void setPartnerBoxState( EPartnerBoxReserveState aState ) {
    state = aState;
    lastUpdate = System.currentTimeMillis();
  }

  /**
   * Обработка события: завершение работы (или разрыв) канала приема состояний
   *
   * @param aChannel {@link HotSwapReaderChannel} канал приема состояний
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void onCloseReaderChannel( HotSwapReaderChannel aChannel ) {
    setPartnerBoxState( EPartnerBoxReserveState.UNKNOWN );
  }

  /**
   * Запуск читателя HotSwap
   *
   * @param aContext {@link ITsContext} контекст приложения
   * @return {@link PasServer} сервер HotSwap
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static PasServer<?> startHotSwapReader( ITsContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    PasPartnerBoxStateListener controller = aContext.get( PasPartnerBoxStateListener.class );
    PasServer<HotSwapReaderChannel> retValue = null;
    try {
      ITsContext ctx = new TsContext();
      ctx.put( PasPartnerBoxStateListener.class, controller );
      OP_PAS_SERVER_ADDRESS.setValue( ctx.params(), OP_LOCAL_ADDRESS.getValue( aContext.params() ) );
      OP_PAS_SERVER_PORT.setValue( ctx.params(), OP_LOCAL_PORT.getValue( aContext.params() ) );
      retValue = new PasServer<>( ctx, HotSwapReaderChannel.CREATOR, false, logger );
      retValue.init();
      // Регистрация обработчиков уведомлений
      retValue.registerNotificationHandler( NODE_STATE_METHOD, new HotSwapNodeStateNotice( controller ) );
      // retValue.run();
      Thread thread = new Thread( retValue, "hotSwapReader" ); //$NON-NLS-1$
      thread.start();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
      if( retValue != null ) {
        retValue.close();
        retValue = null;
      }
      // nl();
      // pl( MSG_FAIL );
      // nl();
      // System.exit( 666 );// выход из программы при невозможности встать на прослушивание
    }
    return retValue;
  }

  /**
   * Метод обработки нотификаций сервера
   *
   * @author max
   */
  static class HotSwapNodeStateNotice
      implements IJSONNotificationHandler<HotSwapReaderChannel> {

    /**
     * Контроллер
     */
    private final PasPartnerBoxStateListener controller;

    private String debugState = "";

    /**
     * Конструктор
     *
     * @param aController {@link IHotSwapController} контроллер
     * @throws TsNullArgumentRtException аргумент = null
     */
    public HotSwapNodeStateNotice( PasPartnerBoxStateListener aController ) {// IHotSwapController aController ) {
      controller = TsNullArgumentRtException.checkNull( aController );
    }

    // ------------------------------------------------------------------------------------
    // Открытые методы
    //

    // ------------------------------------------------------------------------------------
    // Реализация IJSONNotificationHandler
    //
    @Override
    public void notify( HotSwapReaderChannel aChannel, IJSONNotification aNotification ) {
      TsNullArgumentRtException.checkNull( aNotification );
      if( !aNotification.method().equals( NODE_STATE_METHOD ) ) {
        // Уведомление игнорировано
        return;
      }

      EPartnerBoxReserveState state =
          EPartnerBoxReserveState.findById( aNotification.params().getByKey( STATE ).asString() );

      if( !debugState.equals( state.name() ) ) {
        logger.error( "NOT ERROR: Partner state received: %s", state.name() );
      }
      debugState = state.name();
      // в потокобезопасный обмен
      controller.setPartnerBoxState( state );
    }

  }

}

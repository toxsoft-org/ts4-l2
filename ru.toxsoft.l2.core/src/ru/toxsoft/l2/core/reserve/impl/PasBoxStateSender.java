package ru.toxsoft.l2.core.reserve.impl;

import static org.toxsoft.core.pas.server.IPasServerParams.*;
import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.core.tslib.utils.TsTestUtils.*;
import static ru.toxsoft.l2.core.reserve.impl.IReserveParams.*;
import static ru.toxsoft.l2.core.reserve.impl.ISiResources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.pas.client.*;
import org.toxsoft.core.pas.tj.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.reserve.impl.pas.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Реализация класса отправки состояния шкафа с помощью библиотеки PAS
 *
 * @author max
 */
public class PasBoxStateSender
    extends WorkerComponentBase
    implements IBoxStateSender {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( PasPartnerBoxStateListener.class.getName() );

  private EPartnerBoxReserveState state;

  PasClient<?> hotSwapWriter;

  private final ITsContext context;

  private long lastSendTime = 0L;

  private long sendPeriod = 500L;

  public PasBoxStateSender( ITsContext aContext ) {
    context = aContext;
  }

  @Override
  protected void doStartComponent() {

    context.put( PasBoxStateSender.class, this );
    hotSwapWriter = startHotSwapWriter( context );
    super.doStartComponent();
  }

  @Override
  protected boolean doQueryStop() {
    hotSwapWriter.close();
    return true;
  }

  private String debugState = "";

  @Override
  public void doJob() {
    // через период
    long currTime = System.currentTimeMillis();
    if( currTime - lastSendTime > sendPeriod ) {
      sendState();
      lastSendTime = currTime;
    }

  }

  @Override
  public void setReserveSignalOwnState( EPartnerBoxReserveState aState ) {
    state = aState;
    // Попытка мгновенной отправки состояния
    sendState();
  }

  /**
   * Обработка события: завершение работы (или разрыв) канала передатчика состояний
   *
   * @param aChannel {@link HotSwapWriterChannel} канал
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void onCloseWriterChannel( HotSwapWriterChannel aChannel ) {
    hotSwapWriter.close();
    hotSwapWriter = startHotSwapWriter( context );
  }

  /**
   * Отправляет состояние партнёру
   */
  private synchronized void sendState() {
    // Список состояний в формате ITjValue
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    // notifyParams.put( ID, createString( aId ) );
    notifyParams.put( STATE, createString( state.id() ) );
    // notifyParams.put( WEIGHT, createNumber( aWeight ) );
    PasClientChannel clientChannel = hotSwapWriter.getChannelOrNull();
    if( clientChannel != null ) {
      if( !debugState.equals( state.name() ) ) {
        logger.error( "NOT ERROR: Own State to be sended: %s", state.name() );
      }
      debugState = state.name();
      clientChannel.sendNotification( NODE_STATE_METHOD, notifyParams );
    }
  }

  /**
   * Запуск писателя HotSwap
   *
   * @param aContext {@link ITsContext} контекст приложения
   * @return {@link PasClient} писатель HotSwap
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static PasClient<?> startHotSwapWriter( ITsContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    PasBoxStateSender controller = aContext.get( PasBoxStateSender.class );
    PasClient<?> retValue = null;
    try {
      ITsContext ctx = new TsContext();
      ctx.put( PasBoxStateSender.class, controller );
      OP_PAS_SERVER_ADDRESS.setValue( ctx.params(), OP_REMOTE_ADDRESS.getValue( aContext.params() ) );
      OP_PAS_SERVER_PORT.setValue( ctx.params(), OP_REMOTE_PORT.getValue( aContext.params() ) );
      retValue = new PasClient<>( ctx, HotSwapWriterChannel.CREATOR, false, logger );
      retValue.init();
      // retValue.run();
      Thread thread = new Thread( retValue, "hotSwapWriter" ); //$NON-NLS-1$
      thread.start();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
      if( retValue != null ) {
        retValue.close();
      }
      nl();
      pl( MSG_FAIL );
      nl();
      System.exit( 2 );
    }
    return retValue;
  }

}

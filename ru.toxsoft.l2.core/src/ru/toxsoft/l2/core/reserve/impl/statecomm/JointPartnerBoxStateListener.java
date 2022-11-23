package ru.toxsoft.l2.core.reserve.impl.statecomm;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Реализация слушателя изменения состояния шкафа напарника с помощью нескольких разноприоритетных слушателей.
 *
 * @author Max
 */
public class JointPartnerBoxStateListener
    extends WorkerComponentBase
    implements IPartnerBoxStateListener {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( JointPartnerBoxStateListener.class.getName() );

  /**
   * Текущее состояние шкафа партнёра.
   */
  private EPartnerBoxReserveState state = EPartnerBoxReserveState.UNKNOWN;

  /**
   * Слушатели состояния шкафов-партнёров, расставленных по приоритеут (сначала приоритетные)
   */
  private IList<IPartnerBoxStateListener> listeners = new ElemArrayList<>();

  /**
   * Слушатели, которые не остановились сразу после запроса.
   */
  private IListEdit<IWorkerComponent> componentsToStop = new ElemArrayList<>();

  /**
   * Устанавливает слушатели состояния шкафов-партнёров, расставленных по приоритеут (сначала приоритетные)
   *
   * @param aListeners IList - слушатели состояния шкафов-партнёров, расставленных по приоритеут (сначала приоритетные)
   */
  public JointPartnerBoxStateListener( IList<IPartnerBoxStateListener> aListeners ) {
    super();
    listeners = aListeners;
  }

  @Override
  protected void doStartComponent() {
    for( int i = 0; i < listeners.size(); i++ ) {
      IPartnerBoxStateListener l = listeners.get( i );
      l.start();
      logger.error( "partner state listener number=%d of %d started", Integer.valueOf( i ),
          Integer.valueOf( listeners.size() ) );
    }
  }

  @Override
  protected boolean doQueryStop() {
    boolean result = true;
    for( int i = 0; i < listeners.size(); i++ ) {
      IPartnerBoxStateListener l = listeners.get( i );
      boolean oneCompQueryStop = l.queryStop();
      if( !oneCompQueryStop ) {
        componentsToStop.add( l );
      }
      result = result && oneCompQueryStop;
    }

    return result;
  }

  @Override
  protected boolean doStopStep() {
    boolean result = true;

    for( int i = 0; i < componentsToStop.size(); i++ ) {
      IWorkerComponent component = componentsToStop.get( i );
      boolean isStopped = component.isStopped();
      // logger.debug( "component %d.isStopped()=%b", i, isStopped );
      result = result && isStopped;
    }

    return result;
  }

  @Override
  protected void doDestrоyComponent() {
    for( int i = 0; i < listeners.size(); i++ ) {
      IPartnerBoxStateListener l = listeners.get( i );
      l.destroy();
    }
  }

  @Override
  public void doJob() {
    EPartnerBoxReserveState result = EPartnerBoxReserveState.UNKNOWN;
    for( int i = 0; i < listeners.size(); i++ ) {
      IPartnerBoxStateListener l = listeners.get( i );
      l.doJob();

      EPartnerBoxReserveState lState = l.getPartnerBoxState();

      // для debug
      // если более приоритетный слушатель уже принял решение - то остальные просто для диагностики
      if( result != EPartnerBoxReserveState.UNKNOWN ) {
        if( lState != result ) {
          logger.error( "partner state listener number=%d, wrong state=%s, priority state=%s", Integer.valueOf( i ),
              lState.id(), result.id() );
        }
      }

      if( lState != EPartnerBoxReserveState.UNKNOWN ) {
        result = lState;
      }
    }
    setPartnerBoxState( result );
  }

  @Override
  public synchronized EPartnerBoxReserveState getPartnerBoxState() {
    return state;
  }

  private synchronized void setPartnerBoxState( EPartnerBoxReserveState aState ) {
    state = aState;
  }
}

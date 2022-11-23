package ru.toxsoft.l2.core.reserve.impl.statecomm;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;

import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Реализация класса отправки состояния шкафа с помощью нескольких передатчиков.
 *
 * @author max
 */
public class JointBoxStateSender
    extends WorkerComponentBase
    implements IBoxStateSender {

  /**
   * Журнал работы
   */
  // private static ILogger logger = Logger.getLogger( PasPartnerBoxStateListener.class.getName() );

  /**
   * Текущее состояние шкафа.
   */
  private EPartnerBoxReserveState state = EPartnerBoxReserveState.UNKNOWN;

  /**
   * Передатчики состояния шкафа
   */
  private IList<IBoxStateSender> senders = new ElemArrayList<>();

  /**
   * Передатчики, которые не остановились сразу после запроса.
   */
  private IListEdit<IWorkerComponent> componentsToStop = new ElemArrayList<>();

  /**
   * Устанавливает Передатчики состояния шкафа
   *
   * @param aSenders IList - Передатчики состояния шкафа
   */
  public JointBoxStateSender( IList<IBoxStateSender> aSenders ) {
    super();
    senders = aSenders;
  }

  @Override
  protected void doStartComponent() {
    for( int i = 0; i < senders.size(); i++ ) {
      IBoxStateSender s = senders.get( i );
      s.start();
    }
  }

  @Override
  protected boolean doQueryStop() {
    boolean result = true;
    for( int i = 0; i < senders.size(); i++ ) {
      IBoxStateSender s = senders.get( i );
      boolean oneCompQueryStop = s.queryStop();
      if( !oneCompQueryStop ) {
        componentsToStop.add( s );
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
    for( int i = 0; i < senders.size(); i++ ) {
      IBoxStateSender s = senders.get( i );
      s.destroy();
    }
  }

  @Override
  public void doJob() {
    for( int i = 0; i < senders.size(); i++ ) {
      IBoxStateSender s = senders.get( i );
      s.setReserveSignalOwnState( state );

      s.doJob();
    }
  }

  @Override
  public void setReserveSignalOwnState( EPartnerBoxReserveState aState ) {
    state = aState;
  }

}

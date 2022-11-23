package ru.toxsoft.l2.core.reserve.impl.statecomm;

import static ru.toxsoft.l2.core.reserve.impl.IReserveParams.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.main.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.util.*;

/**
 * Слушатель состояния партнера через контроллер.
 *
 * @author Maz
 */
public class ControllerPartnerBoxStateListener
    extends WorkerComponentBase
    implements IPartnerBoxStateListener {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( ControllerPartnerBoxStateListener.class.getName() );

  /**
   * Текущее состояние шкафа партнёра.
   */
  private EPartnerBoxReserveState state = EPartnerBoxReserveState.UNKNOWN;

  /**
   * Определитель меандра
   */
  private MeanderDetector detector;

  /**
   * Текущее значение входа меандра {1, 2, 3}
   */
  protected IAtomicValue value = IAtomicValue.NULL; // значение для теста TODO

  /**
   * Минимальное количество тактов на пол периода меандра
   */
  private int reserveMeanderMinCount;

  /**
   * Идентификатор входного меандрового пина резервирования.
   */
  private String reserveMeanderPinId;

  /**
   * Входной меандровый пин резервирования.
   */
  protected IDIPin reserveMeanderPin = IDIPin.STUB_DI_PIN;

  /**
   * Контекст резервирования.
   */
  private final ITsContext reserveContext;

  /**
   * Контекст глобальный.
   */
  private IGlobalContext globalContext;

  /**
   * Конструктор по контекстам
   *
   * @param aReserveContext ITsContext - контекст резервирования.
   * @param aGlobalContext IGlobalContext - глобальный контекст.
   */
  public ControllerPartnerBoxStateListener( ITsContext aReserveContext, IGlobalContext aGlobalContext ) {
    super();
    reserveContext = aReserveContext;
    globalContext = aGlobalContext;
    reserveMeanderPinId = OP_RESERVE_MEANDER_PIN.getValue( reserveContext.params() ).asString();
    long reserveMeanderHalfPeriod = OP_RESERVE_MEANDER_HALH_PERIOD.getValue( reserveContext.params() ).asLong();
    reserveMeanderMinCount = OP_RESERVE_MEANDER_MIN_COUNT.getValue( reserveContext.params() ).asInt();

    long period = 2L * reserveMeanderHalfPeriod;
    int minPointCount = 2 * reserveMeanderMinCount;

    detector = new MeanderDetector( period, minPointCount );
    logger.warning( "Reserve meander detector is created period=%d, minPointCount=%d", Long.valueOf( period ),
        Integer.valueOf( minPointCount ) );
  }

  @Override
  protected void doStartComponent() {
    if( globalContext.hal().listDIPins().hasKey( reserveMeanderPinId ) ) {
      reserveMeanderPin = globalContext.hal().listDIPins().getByKey( reserveMeanderPinId );
      logger.info( "DI Reserve meander pin: %s - is found", reserveMeanderPinId );
    }
    else {
      logger.warning( "DI Reserve meander pin: %s - is not available", reserveMeanderPinId );
    }
  }

  @Override
  public void doJob() {
    long aTime = System.currentTimeMillis();
    Boolean measure = reserveMeanderPin.getDI();

    IAtomicValue newVal = IAtomicValue.NULL;
    if( measure != null ) {
      int meanderVal = detector.getValue( aTime, measure.booleanValue() );
      if( meanderVal >= 0 ) {
        newVal = AvUtils.avInt( meanderVal );
      }
    }

    boolean result = value == null || !newVal.equals( value );

    if( result ) {
      if( newVal == IAtomicValue.NULL ) {
        setPartnerBoxState( EPartnerBoxReserveState.UNKNOWN );
      }
      else {
        int stateInt = newVal.asInt();
        if( stateInt == 0 ) {
          setPartnerBoxState( EPartnerBoxReserveState.UNKNOWN );
        }
        else
          if( stateInt == 1 ) {
            setPartnerBoxState( EPartnerBoxReserveState.RESERVE );
          }
          else {
            setPartnerBoxState( EPartnerBoxReserveState.MAIN );
          }
      }
    }

    // debug meander
    logger.debug( "meander debug pin_val=%s,   m_val = %d", (measure == null ? "null" : measure.toString()),
        Integer.valueOf( newVal.isAssigned() ? newVal.asInt() : -1 ) );

    value = newVal;
  }

  @Override
  public synchronized EPartnerBoxReserveState getPartnerBoxState() {
    return state;
  }

  private synchronized void setPartnerBoxState( EPartnerBoxReserveState aState ) {
    // для теста TODO - раскоментировал основной код
    state = aState;
  }
}

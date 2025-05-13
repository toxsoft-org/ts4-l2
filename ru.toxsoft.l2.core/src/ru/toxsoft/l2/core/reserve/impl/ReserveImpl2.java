package ru.toxsoft.l2.core.reserve.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.core.reserve.IReserveHardConstants.*;
import static ru.toxsoft.l2.core.reserve.impl.IL2Resources.*;
import static ru.toxsoft.l2.core.reserve.impl.IReserveParams.*;

import java.io.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.progargs.*;

import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.reserve.*;
import ru.toxsoft.l2.core.reserve.impl.statecomm.*;

/**
 * Реализация работы с резервированием.
 *
 * @author max
 */
public class ReserveImpl2
    extends AbstractL2Component
    implements IReserveComponent {

  private static final String PAS_OFF_PARAM_ID = "pas.off";

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст;
   */
  private ITsContext ctx;

  /**
   * Отправляет собственное состояние шкафа напарнику.
   */
  private IBoxStateSender boxStateSender;

  /**
   * Слушатель изменения состояния шкафа-напарника
   */
  private IPartnerBoxStateListener pairedBoxStateListener;

  /**
   * Контроллер включения-выключения подключаемых модулей
   */
  private IDlmsController dLMsController;

  /**
   * Слушатель функционального состояния шкафа (здоровья)
   */
  private IHealthListener healthListener;

  /**
   * Отправляет данные и события на сервер.
   */
  private INetworkDataSender networkDataSender;

  /**
   * Слушатель команд с сервера и состояния соединения с сервером.
   */
  private INetworkListener networkListener;

  /**
   * Параметры настройки резервирования.
   */
  // private final OptionSet reserveOps;

  /**
   * Таблица проверяемых правил
   */
  private IList<IRule> rules;

  /**
   * Текущее действующее незавершённое правило
   */
  private IRule ruleInProcess;

  /**
   * Время старта
   */
  private long startTime = Long.MAX_VALUE;

  /**
   * Настроечный параметр - интервал запуска - в это время работает только отправка и получение данных, алгоритмы не
   * работают
   */
  private long startInterval = 500;

  /**
   * Настроечный параметр - тайм-аут в течение которого шкаф не станет главным (только по команде оператора)
   */
  private long becomeMainTimeout = 5000;

  /**
   * Настроечный параметр, указывающий что этот шкаф - единственный в системе (в этом случае шкаф стартует после
   * таймаута)
   */
  private boolean isTheOnlyBox = false;

  //
  // ---------------------------------------------------------------
  // Текущие параметры шкафы

  /**
   * Текущее состояние шкафа в части резервирования
   */
  private EReserveState currState = EReserveState.RESERVE_AUTO;

  /**
   * Текущее состояние шкафа напарника в части резервирования
   */
  private EPartnerBoxReserveState currPartnerBoxState;

  /**
   * Текущее функциональное состояние шкафа.
   * <p>
   * Начальное состояние - false - пришлось ввести - т.к. начальное значение RESERVE_AUTO
   */
  private Boolean currSick = Boolean.FALSE;

  /**
   * Текущее состояние соединения с сервером.
   * <p>
   * Начальное состояние - false - пришлось ввести - т.к. начальное значение RESERVE_AUTO
   */
  private Boolean currConnectionBreak = Boolean.FALSE;

  private IDOPin mainControlPin = IDOPin.STUB_DO_PIN;

  private String mainControlPinId;

  /**
   * Главное автоматическое правило перехода - после вхождения в состояние резерв-авто - нужно включать тайм-аут
   */
  private ReserveAutoPartnerReserveToMainRule mainAutoRule;

  /**
   * Конструктор реализации работы с резервированием по глобальному контексту.
   *
   * @param aGlobalContext GlobalContext - глобальный контекст.
   */
  public ReserveImpl2( GlobalContext aGlobalContext ) {
    super( aGlobalContext );

    // инициализация контекста по файлу конфигурации
    ctx = new TsContext();
    prepareContextParams( ctx, L2_RESERVE_CFG_FILE_NAME );

    startInterval = OP_RESERVE_START_INTERVAL.getValue( ctx.params() ).asInt();
    becomeMainTimeout = OP_BECOME_MAIN_INTERVAL.getValue( ctx.params() ).asInt();
    isTheOnlyBox = OP_IS_ONLY_BOX.getValue( ctx.params() ).asBool();
    mainControlPinId = OP_MAIN_CONTROL_PIN.getValue( ctx.params() ).asString();
    boolean isDefault = OP_IS_DEFAULT.getValue( ctx.params() ).asBool();

    // компоненты резервирования
    boxStateSender = new PasBoxStateSender( ctx );
    // IBoxStateSender.EMPTY_REALISATION;

    // формирование слушателя состояния партнёра
    if( ctx.params().hasValue( OP_RESERVE_MEANDER_PIN ) ) {
      IListEdit<IPartnerBoxStateListener> partnerStateListeners = new ElemArrayList<>();
      // слушатель по эзернету через pas

      // для возможности теста
      if( !ctx.params().hasValue( PAS_OFF_PARAM_ID ) || !ctx.params().getBool( PAS_OFF_PARAM_ID ) ) {
        partnerStateListeners.add( new PasPartnerBoxStateListener( ctx ) );
      }

      // слушатель по котроллеру - через меандровый пин
      partnerStateListeners.add( new ControllerPartnerBoxStateListener( ctx, aGlobalContext ) );
      // объединённый слушатель с приоритетами по порядку
      pairedBoxStateListener = new JointPartnerBoxStateListener( partnerStateListeners );
    }
    else {
      // IPartnerBoxStateListener.EMPTY_REALISATION;
      pairedBoxStateListener = new PasPartnerBoxStateListener( ctx );
    }

    dLMsController = new HalDlmsAsynchController( aGlobalContext, ctx.params(),
        new DlmsAsynchController( aGlobalContext, ctx.params() ) );
    healthListener = new HealthListener( aGlobalContext, ctx.params() );
    networkDataSender = new NetworkDataSender( aGlobalContext, ctx.params() );
    networkListener = new NetworkListener( aGlobalContext, ctx.params() );

    // правила резеревирования
    rules = new ElemArrayList<>();

    ((ElemArrayList<IRule>)rules).add( new ReserveToReserveAutoRule() );
    ((ElemArrayList<IRule>)rules).add( new ReserveCmdMainToReserveRule() );
    ((ElemArrayList<IRule>)rules).add( new ReserveCmdMainInvalidRule() );
    ((ElemArrayList<IRule>)rules).add( new ReserveConLostSickRule() );
    ((ElemArrayList<IRule>)rules).add( new ReservePairStateChangeRule() );

    // ((ElemArrayList<IRule>)rules).add( new ReserveAutoTimeoutToMaindRuleOld() );
    ((ElemArrayList<IRule>)rules).add( new ReserveAutoCmdMainRule() );
    ((ElemArrayList<IRule>)rules).add( new ReserveAutoToReserveRule() );
    mainAutoRule = new ReserveAutoPartnerReserveToMainRule();
    ((ElemArrayList<IRule>)rules).add( mainAutoRule );
    ((ElemArrayList<IRule>)rules).add( new ReserveAutoPairStateInvalidRule() );

    ((ElemArrayList<IRule>)rules).add( new MainCmdMainInvalidToMainInvalidRule() );
    ((ElemArrayList<IRule>)rules).add( new MainCmdReserveToReserveAutoRule() );
    ((ElemArrayList<IRule>)rules).add( new MainToReserveRule() );
    ((ElemArrayList<IRule>)rules).add( new MainPairStateMainToReserveAutoRule( !isDefault ) );
    ((ElemArrayList<IRule>)rules).add( new MainPairStateInvalidToMainRule() );

    ((ElemArrayList<IRule>)rules).add( new MainInvalidCmdMainToMainRule() );
    ((ElemArrayList<IRule>)rules).add( new MainInvalidCmdReserveToReserveMainRule() );
    ((ElemArrayList<IRule>)rules).add( new MainInvalidConnLostToReserveMainRule() );
    ((ElemArrayList<IRule>)rules).add( new MainInvalidPairStateMainToReserveRule() );
    ((ElemArrayList<IRule>)rules).add( new MainInvalidPairStateInvalidToMainInvalidRule() );
    ((ElemArrayList<IRule>)rules).add( new MainInvalidSickToMainInvalidRule() );

  }

  @Override
  protected void processStart() {
    // запуск компонент резервирования
    pairedBoxStateListener.start();
    healthListener.start();
    networkListener.start();

    dLMsController.start();
    boxStateSender.start();
    networkDataSender.start();

    if( context.hal().listDOPins().hasKey( mainControlPinId ) ) {
      mainControlPin = context.hal().listDOPins().getByKey( mainControlPinId );
    }
    else {
      logger.warning( "Master control pin: %s - is not available", mainControlPinId );
    }

    setCurrState( EReserveState.RESERVE_AUTO, "State on start" );

    // пришлось ввести и записывать - т.к. начальное значение RESERVE_AUTO
    if( currSick != null ) {
      networkDataSender.setSick( currSick.booleanValue() );
    }
    if( currConnectionBreak != null ) {
      networkDataSender.setConnectionBreak( currConnectionBreak.booleanValue() );
    }
    startTime = System.currentTimeMillis();
  }

  private long runPeriod = 500L;// 1000L;

  private long lastRun = 0L;

  @Override
  protected void processRunStep() {
    // запус шага с не чаще чем раз за указанный интервал или когда есть действующее правило

    // слушатель состояния шкафа-партнёра (должен работать на каждом такте)
    pairedBoxStateListener.doJob();

    if( ruleInProcess == null && System.currentTimeMillis() - lastRun < runPeriod ) {
      return;
    }

    lastRun = System.currentTimeMillis();// на всяк случай

    // на каждом рабочем шагу работают все компоненты резервирования в части чтения данных

    healthListener.doJob();
    networkListener.doJob();

    // при наличии действующего правила - по возможности завершить
    if( ruleInProcess != null ) {
      if( ruleInProcess.isDone() ) {
        // currState = ruleInProcess.newState();
        ruleInProcess = null;
      }
    }

    // при отсутствии действующего правила - в каждое правило установить параметры и состояния для проверки
    if( ruleInProcess == null ) {
      if( System.currentTimeMillis() - startTime > startInterval ) {
        // получение значений параметров и состояния, влияющих на резервирование
        // новое состояние шкафа напарника
        EPartnerBoxReserveState newPairedBoxState = pairedBoxStateListener.getPartnerBoxState();
        // новое значение параметра критической нефункциональности
        boolean newSick = healthListener.isSick();
        // новое значение параметра отсутствия соединения с сервером
        boolean newConnectionBreak = networkListener.isConnectionBreak();

        // Dima, 24.03.20 подозрительное место, иногда этот модуль перестает реагировать на поступающие команды
        // команда с сервера (при её наличии ессессвенно)
        // ReserveCommandOnExecuting command =
        // networkListener.isCommandReceived() ? networkListener.getReceivedCommand() : null;
        ReserveCommandOnExecuting command = networkListener.getReceivedCommand();

        if( command != null ) {
          logger.info( "ReserveImpl2 get command: %s", command.getCmdId() );
        }

        // проверка изменения значений параметров и состояний
        boolean partnerBoxStateChanged =
            currPartnerBoxState != null && !currPartnerBoxState.equals( newPairedBoxState );

        if( partnerBoxStateChanged ) {
          logger.info( "Partner state changed: %s", newPairedBoxState.name() );
        }

        boolean sickChanged = currSick != null && currSick.booleanValue() != newSick;

        if( sickChanged ) {
          logger.info( "Sick changed: %s", String.valueOf( newSick ) );
        }

        boolean connectionBreakChanged =
            currConnectionBreak != null && currConnectionBreak.booleanValue() != newConnectionBreak;

        // передача новых значений параметров и состояний для отправки на сервер
        if( partnerBoxStateChanged || currPartnerBoxState == null ) {
          networkDataSender.setPartnerState( newPairedBoxState );
        }
        if( sickChanged || currSick == null ) {
          networkDataSender.setSick( newSick );
        }
        if( connectionBreakChanged || currConnectionBreak == null ) {
          networkDataSender.setConnectionBreak( newConnectionBreak );
        }

        // после того, как интервал запуска прошёл - работа алгоритмов
        // перенесено выше - т.к. изменение параметров может произойти раньше и уже не учтётся
        // if( System.currentTimeMillis() - startTime > startInterval ) {

        for( IRule rule : rules ) {
          rule.setCommand( command );
          rule.setConnectionBreak( newConnectionBreak, connectionBreakChanged );
          rule.setSick( newSick, sickChanged );
          rule.setPartnerBoxState( newPairedBoxState, partnerBoxStateChanged );
          rule.setState( currState );

          // если правило сработало - оно начало выполняться и должно быть установлено как действующее (если только
          // сразу
          // не выполнилось)
          if( rule.checkAndStart() ) {
            if( !rule.isDone() ) {
              ruleInProcess = rule;
            }
            break;
          }
        }

        // сохранение новых значений параметров как текущих
        currPartnerBoxState = newPairedBoxState;
        currSick = Boolean.valueOf( newSick );
        currConnectionBreak = Boolean.valueOf( newConnectionBreak );
      }
    }

    // на каждом рабочем шагу работают все компоненты резервирования в части записи данных
    dLMsController.doJob();
    boxStateSender.doJob();
    networkDataSender.doJob();

    lastRun = System.currentTimeMillis();// ещё раз
  }

  private IListEdit<IWorkerComponent> componentsToStop = new ElemArrayList<>();

  @Override
  protected boolean processStopQuery() {
    boolean result = true;
    boolean oneCompQueryStop = pairedBoxStateListener.queryStop();
    if( !oneCompQueryStop ) {
      componentsToStop.add( pairedBoxStateListener );
      // logger.debug( "pairedBoxStateListener.queryStop()=%b", oneCompQueryStop );
    }
    result = result && oneCompQueryStop;

    oneCompQueryStop = healthListener.queryStop();
    if( !oneCompQueryStop ) {
      componentsToStop.add( healthListener );
      // logger.debug( "healthListener.queryStop()=%b", oneCompQueryStop );
    }
    result = result && oneCompQueryStop;

    oneCompQueryStop = networkListener.queryStop();
    if( !oneCompQueryStop ) {
      componentsToStop.add( networkListener );
      // logger.debug( "networkListener.queryStop()=%b", oneCompQueryStop );
    }
    result = result && oneCompQueryStop;

    oneCompQueryStop = dLMsController.queryStop();
    if( !oneCompQueryStop ) {
      componentsToStop.add( dLMsController );
      // logger.debug( "dLMsController.queryStop()=%b", oneCompQueryStop );
    }
    result = result && oneCompQueryStop;

    oneCompQueryStop = boxStateSender.queryStop();
    if( !oneCompQueryStop ) {
      componentsToStop.add( boxStateSender );
      // logger.debug( "boxStateSender.queryStop()=%b", oneCompQueryStop );
    }
    result = result && oneCompQueryStop;

    oneCompQueryStop = networkDataSender.queryStop();
    if( !oneCompQueryStop ) {
      componentsToStop.add( networkDataSender );
      // logger.debug( "networkDataSender.queryStop()=%b", oneCompQueryStop );
    }
    result = result && oneCompQueryStop;

    return result;
  }

  @Override
  protected boolean processStopStep() {
    boolean result = true;

    for( int i = 0; i < componentsToStop.size(); i++ ) {
      IWorkerComponent component = componentsToStop.get( i );
      boolean isStopped = component.isStopped();
      // logger.debug( "component %d.isStopped()=%b", i, isStopped );
      result = result && isStopped;
    }

    return result;
  }

  /**
   * Возвращает состояние работы DLM модулей.
   *
   * @return true - модули работают,
   *         <p>
   *         false - модули не работают.
   */
  @Override
  public boolean isDLMsRun() {
    return dLMsController.isDLMsRun();
  }

  //
  // -----------------------------------------------------------------------
  // Внутренние методы

  /**
   * Установка текущего состояния.
   *
   * @param aCurrState EReserveState - состояние резервирования
   * @param aReason String - причина изменения состояния
   */
  private void setCurrState( EReserveState aCurrState, String aReason ) {
    currState = aCurrState;
    logger.info( "State= %s, Reason= %s", aCurrState.name(), aReason );

    EPartnerBoxReserveState reserveState = EPartnerBoxReserveState.UNKNOWN;

    if( aCurrState == EReserveState.MAIN || aCurrState == EReserveState.MAIN_INVALID ) {
      reserveState = EPartnerBoxReserveState.MAIN;
    }

    if( aCurrState == EReserveState.RESERVE || aCurrState == EReserveState.RESERVE_AUTO ) {
      reserveState = EPartnerBoxReserveState.RESERVE;
    }

    if( aCurrState == EReserveState.RESERVE_AUTO ) {
      mainAutoRule.reinit();
    }

    boxStateSender.setReserveSignalOwnState( reserveState );
    networkDataSender.setState( aCurrState, aReason );
    mainControlPin
        .setDO( Boolean.valueOf( aCurrState == EReserveState.MAIN || aCurrState == EReserveState.MAIN_INVALID ) );
  }

  /**
   * Правило резервирования, состоящее в проверке состояний и параметров и принятии решения на смену состояния, отправки
   * сообщений и т.д.
   *
   * @author max
   */
  interface IRule {

    /**
     * Устанавливает текущее состояние шкафа
     *
     * @param aState EReserveState
     */
    void setState( EReserveState aState );

    /**
     * Устанавливает текущее состояние шкафа напарника и пометку, что оно поменялось
     *
     * @param aPartnerBoxState EPartnerBoxReserveState - екущее состояние шкафа напарника
     * @param aIsPartnerBoxStateChanged true - состояние шкафа напарника поменялось, false - состояние не поменялось
     */
    void setPartnerBoxState( EPartnerBoxReserveState aPartnerBoxState, boolean aIsPartnerBoxStateChanged );

    /**
     * Устанавливает текущее значение признака критической потери функциональности и пометку, что оно поменялось.
     *
     * @param aSick boolean текущее значение признака критической потери функциональности
     * @param aIsSickChanged boolean true - значение признака критической потери функциональности поменялось, false - не
     *          поменялось
     */
    void setSick( boolean aSick, boolean aIsSickChanged );

    /**
     * Устанавливает текущее значение признака потери связи с сервером и пометку, что оно поменялось.
     *
     * @param aConnectionBreak boolean текущее значение признака потери связи с сервером
     * @param aConnectionBreakChanged boolean true - значение признака потери связи с сервером поменялось, false - не
     *          поменялось
     */
    void setConnectionBreak( boolean aConnectionBreak, boolean aConnectionBreakChanged );

    /**
     * Устанавливает команду с сервера (или null - если нет команды)
     *
     * @param aCommand ReserveCommandOnExecuting - команда
     */
    void setCommand( ReserveCommandOnExecuting aCommand );

    /**
     * Проверяет предустановленные параметры на соответствие условиям и инициирует выполнение правила.
     *
     * @return true - если входные параметры соответствуют условиям, false - не соответствуют
     */
    boolean checkAndStart();

    /**
     * Проверяет что правило выполнение.
     *
     * @return true - правило выполнени, false - правило не закончило выполнение.
     */
    boolean isDone();
  }

  /**
   * Правило перехода из состояния в состояние со всеми сопутствующими процессами: отправка событий, вкл-выкл длмс и
   * т.д.
   *
   * @author max
   */
  abstract class AbstractRule
      implements IRule {

    protected final EReserveState newState;

    protected final EReserveState stateForRule;

    protected final EReserveCommand checkingCommand;

    protected EReserveState state;

    protected EPartnerBoxReserveState partnerBoxState;

    protected boolean partnerBoxStateChanged = false;

    protected boolean sick;

    protected boolean sickChanged = false;

    protected boolean connectionBreak;

    protected boolean connectionBreakChanged = false;

    protected ReserveCommandOnExecuting command;

    /**
     * Конструктор по начальному состоянию и конечному состоянию (по умолчанию)
     *
     * @param aStateForRule EReserveState - состояние, необходимое для применения правила
     * @param aNewState EReserveState - новое состояние по умолчанию
     */
    public AbstractRule( EReserveState aStateForRule, EReserveState aNewState ) {
      super();
      stateForRule = aStateForRule;
      newState = aNewState;
      checkingCommand = null;
    }

    /**
     * Конструктор по начальному состоянию и конечному состоянию (по умолчанию)
     *
     * @param aStateForRule EReserveState - состояние, необходимое для применения правила
     * @param aNewState EReserveState - новое состояние по умолчанию
     * @param aCheckingCommand EReserveCommand - проверяемая команда
     */
    public AbstractRule( EReserveState aStateForRule, EReserveState aNewState, EReserveCommand aCheckingCommand ) {
      super();
      stateForRule = aStateForRule;
      newState = aNewState;
      checkingCommand = aCheckingCommand;
    }

    @Override
    public void setState( EReserveState aState ) {
      state = aState;
    }

    @Override
    public void setPartnerBoxState( EPartnerBoxReserveState aNewPairBoxState, boolean aIsPartnerBoxStateChanged ) {
      partnerBoxState = aNewPairBoxState;
      partnerBoxStateChanged = aIsPartnerBoxStateChanged;
    }

    @Override
    public void setSick( boolean aNewSick, boolean aIsSickChanged ) {
      sick = aNewSick;
      sickChanged = aIsSickChanged;
    }

    @Override
    public void setConnectionBreak( boolean aNewConnectionBreak, boolean aConnectionBreakChanged ) {
      connectionBreak = aNewConnectionBreak;
      connectionBreakChanged = aConnectionBreakChanged;
    }

    @Override
    public void setCommand( ReserveCommandOnExecuting aCommand ) {
      command = aCommand;
    }

    @Override
    public final boolean checkAndStart() {
      if( state != stateForRule ) {
        return false;
      }

      if( checkingCommand != null && (command == null || command.getCommandType() != checkingCommand) ) {
        return false;
      }

      return doCheckAndStart();
    }

    protected abstract boolean doCheckAndStart();

    @Override
    public final boolean isDone() {

      boolean result = isDoneImpl();

      if( result ) {
        networkDataSender.setFromReserveToMainTransition( false );
        networkDataSender.setFromMainToReserveTransition( false );
      }

      if( result && command != null ) {
        networkListener.commandHasBeenDone( command );
      }

      return result;
    }

    protected abstract boolean isDoneImpl();

  }

  /**
   * Переход из резерва в авторезерв после устранения проблем со связью, со здоровьем
   *
   * @author max
   */
  class ReserveToReserveAutoRule
      extends AbstractRule {

    public ReserveToReserveAutoRule() {
      super( EReserveState.RESERVE, EReserveState.RESERVE_AUTO );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( !connectionBreak && !sick ) {
        // Перешел из Reserve в ReserveAuto после устранения проблем со связью
        if( connectionBreakChanged ) {
          setCurrState( EReserveState.RESERVE_AUTO, "Connection is established" );
          return true;
        }
        // Перешел из Reserve в ReserveAuto после устранения проблем со здоровьем
        if( sickChanged ) {
          setCurrState( EReserveState.RESERVE_AUTO, "Health is repaired" );
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }

  }

  /**
   * Переход из резерва в резерв при поступлении команды стать главным
   *
   * @author max
   */
  class ReserveCmdMainToReserveRule
      extends AbstractRule {

    public ReserveCmdMainToReserveRule() {
      super( EReserveState.RESERVE, EReserveState.RESERVE, EReserveCommand.CDM_MAIN );
    }

    @Override
    protected boolean doCheckAndStart() {

      // сообщение в зависимости от значения
      if( partnerBoxState == EPartnerBoxReserveState.MAIN ) {
        // не могу перейти в Main по причине meanderMain у соседа
        networkDataSender.sendEvent( EReserveEvent.CANT_CHANGE,
            new ElemArrayList<>( avStr( EReserveState.RESERVE.id() ), avStr(
                "Шкаф не может стать главным по причине того, что шкаф-напарник находится в состоянии главного" ) ) );
      }
      else {
        // Сообщение с параметрами здоровья и значением меандра от соседа с требованием подтвердить намерение все
        // равное перевести комплект в MainInvalid. При подтверждении формируется команда cmdMainInvalid
        networkDataSender.sendEvent( EReserveEvent.CONFIRM_MAIN,
            new ElemArrayList<>( avBool( sick ), avStr( partnerBoxState.id() ) ) );
      }

      return true;

    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }

  }

  /**
   * Переход из резерва при поступлении команды стать главным инвалидом
   *
   * @author max
   */
  class ReserveCmdMainInvalidRule
      extends AbstractRule {

    public ReserveCmdMainInvalidRule() {
      super( EReserveState.RESERVE, EReserveState.RESERVE, EReserveCommand.CMD_MAIN_INVALID );
    }

    @Override
    protected boolean doCheckAndStart() {

      // сообщение в зависимости от значения

      if( partnerBoxState == EPartnerBoxReserveState.MAIN ) {
        // Не могу перевести из Reserve в MainInvalid по причине meanderMain у соседа
        networkDataSender.sendEvent( EReserveEvent.CANT_CHANGE,
            new ElemArrayList<>( avStr( EReserveState.RESERVE.id() ), avStr(
                "Шкаф не может стать главным с ограничениями по причине того, что шкаф-напарник находится в состоянии главного" ) ) );
      }
      else {
        // newState = EReserveState.MAIN_INVALID;
        // Перешел из Reserve в MainInvalid по команде от оператора имярек.
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.MAIN );
        setCurrState( EReserveState.MAIN_INVALID, "By operator command" );
        // networkDataSender.setFromReserveToMainTransition( true );
        startTransmitionFromReserveToMain();
      }

      return true;

    }

    @Override
    public boolean isDoneImpl() {
      return newState == EReserveState.RESERVE || dLMsController.isDLMsRun();
    }

  }

  /**
   * Переход из резерва при потере связи с сервером или потерей здоровья
   *
   * @author max
   */
  class ReserveConLostSickRule
      extends AbstractRule {

    public ReserveConLostSickRule() {
      super( EReserveState.RESERVE, EReserveState.RESERVE );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( connectionBreakChanged && connectionBreak ) {
        // потеряна связь с локальным сервером
        return true;
      }
      if( sickChanged && sick ) {
        // поплохело
        return true;
      }
      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }
  }

  /**
   * Переход из резерва в резерв при изменении режима парного шкафа
   *
   * @author max
   */
  class ReservePairStateChangeRule
      extends AbstractRule {

    public ReservePairStateChangeRule() {
      super( EReserveState.RESERVE, EReserveState.RESERVE );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected boolean doCheckAndStart() {
      if( partnerBoxStateChanged ) {
        if( partnerBoxState == EPartnerBoxReserveState.UNKNOWN ) {
          // Сосед умер (meanderInvalid), возможно, что в системе отсутствует Main!!!
          networkDataSender.sendEvent( EReserveEvent.NO_MAIN, IList.EMPTY );
          return true;
        }
        if( partnerBoxState == EPartnerBoxReserveState.RESERVE ) {
          // В системе отсутствует Main!!!
          networkDataSender.sendEvent( EReserveEvent.NO_MAIN, IList.EMPTY );
          return true;
        }
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }
  }

  /**
   * Переход из резерв-авто в главный по тайм-ауту
   *
   * @author max
   * @deprecated
   */
  @Deprecated
  class ReserveAutoTimeoutToMaindRuleOld
      extends AbstractRule {

    private long startTime = Long.MAX_VALUE;

    private static final long timeout = 5000;

    private boolean done = false;

    public ReserveAutoTimeoutToMaindRuleOld() {
      super( EReserveState.RESERVE_AUTO, EReserveState.MAIN );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( partnerBoxState == EPartnerBoxReserveState.RESERVE && !done
          && System.currentTimeMillis() - startTime > timeout ) {
        done = true;
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.MAIN );
        setCurrState( EReserveState.MAIN, "By timeout on start" );
        startTransmitionFromReserveToMain();

        return true;
      }
      if( startTime > System.currentTimeMillis() && !done ) {
        startTime = System.currentTimeMillis();
      }
      if( !done && partnerBoxStateChanged && partnerBoxState != EPartnerBoxReserveState.RESERVE ) {
        done = true;
      }
      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return dLMsController.isDLMsRun();
    }

  }

  /**
   * Самый главный переход из резерв-авто в главный - автоматом при уходе парного шкафа в резерв
   *
   * @author max
   */
  class ReserveAutoPartnerReserveToMainRule
      extends AbstractRule {

    private long startTimeInRule = Long.MAX_VALUE;

    private boolean started = false;

    private EPartnerBoxReserveState checkingPartnerBoxState = EPartnerBoxReserveState.RESERVE;

    private String messageTimeOut = "By timeout on start";

    private String messageAutomat = "By automatic algorithm";

    public ReserveAutoPartnerReserveToMainRule() {
      super( EReserveState.RESERVE_AUTO, EReserveState.MAIN );

      if( isTheOnlyBox ) {
        checkingPartnerBoxState = EPartnerBoxReserveState.UNKNOWN;
      }
    }

    @Override
    protected boolean doCheckAndStart() {

      long currTime = System.currentTimeMillis();
      if( partnerBoxState == checkingPartnerBoxState && currTime - startTimeInRule > becomeMainTimeout
          && (partnerBoxStateChanged || !started || isTheOnlyBox) ) {

        logger.info( "ReserveAutoPartnerReserveToMainRule doCheckAndStart: partnerBoxStateChanged= %b, started= %b",
            Boolean.valueOf( partnerBoxStateChanged ), Boolean.valueOf( started ) );

        // Перешел из ReserveAuto в Main автоматически (или по тайм-ауту при запуске)
        setCurrState( EReserveState.MAIN, started ? messageAutomat : messageTimeOut );
        startTransmitionFromReserveToMain();
        started = true;

        return true;
      }
      // на старте - зафиксировать время старта
      if( currTime < startTimeInRule && !started ) {
        logger.info( "ReserveAutoPartnerReserveToMainRule doCheckAndStart: startTimeInRule" );
        startTimeInRule = currTime;
      }

      // если до времени тайм-аута напарник стал главным (или неопределённым) - забыть про тайм-аут
      if( !started && partnerBoxStateChanged && partnerBoxState != EPartnerBoxReserveState.RESERVE ) {
        logger.info( "ReserveAutoPartnerReserveToMainRule doCheckAndStart: started" );
        started = true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      boolean result = dLMsController.isDLMsRun();
      logger.info( "ReserveAutoPartnerReserveToMainRule isDoneImpl: isDone= %b", Boolean.valueOf( result ) );
      return result;
    }

    public void reinit() {
      logger.info( "ReserveAutoPartnerReserveToMainRule reinit" );
      startTimeInRule = Long.MAX_VALUE;
      started = false;
      messageTimeOut = "By timeout from reserve auto";
    }

  }

  /**
   * Самый главный переход из резерв-авто в главный - автоматом при уходе парного шкафа в резерв
   *
   * @author max
   * @deprecated
   */
  @Deprecated
  class ReserveAutoPartnerReserveToMainRuleOld
      extends AbstractRule {

    public ReserveAutoPartnerReserveToMainRuleOld() {
      super( EReserveState.RESERVE_AUTO, EReserveState.MAIN );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( partnerBoxStateChanged && partnerBoxState == EPartnerBoxReserveState.RESERVE ) {
        // Перешел из ReserveAuto в Main автоматически
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.MAIN );
        setCurrState( EReserveState.MAIN, "By automatic algorithm" );
        startTransmitionFromReserveToMain();

        return true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return dLMsController.isDLMsRun();
    }

  }

  /**
   * Переход из резерв-авто в главный по команде стать главным
   *
   * @author max
   */
  class ReserveAutoCmdMainRule
      extends AbstractRule {

    public ReserveAutoCmdMainRule() {
      super( EReserveState.RESERVE_AUTO, EReserveState.RESERVE_AUTO, EReserveCommand.CDM_MAIN );
    }

    @Override
    protected boolean doCheckAndStart() {

      // сообщение в зависимости от значения
      if( partnerBoxState == EPartnerBoxReserveState.MAIN ) {
        // не могу перейти в Main по причине meanderMain у соседа
        networkDataSender.sendEvent( EReserveEvent.CANT_CHANGE,
            new ElemArrayList<>( avStr( EReserveState.RESERVE_AUTO.id() ), avStr(
                "Шкаф не может стать главным по причине того, что шкаф-напарник находится в состоянии главного" ) ) );
      }
      else {
        // Dima, 25.03.20
        // Вот здесь не нужно менять состояние, потому что в следующий раз, при сраблотке верхнего if программа уже не
        // выйдет из этого правила
        // newState = EReserveState.MAIN;
        setCurrState( EReserveState.MAIN, "By operator command" );
        // setCurrState( newState, "By operator command" );

        startTransmitionFromReserveToMain();
      }

      return true;

    }

    @Override
    public boolean isDoneImpl() {
      return newState == EReserveState.RESERVE_AUTO || dLMsController.isDLMsRun();
    }

  }

  /**
   * Переход из резерв-авто в резерв по потере сервера или здоровья
   *
   * @author max
   */
  class ReserveAutoToReserveRule
      extends AbstractRule {

    public ReserveAutoToReserveRule() {
      super( EReserveState.RESERVE_AUTO, EReserveState.RESERVE );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( connectionBreakChanged && connectionBreak ) {
        // потеряна связь с локальным сервером
        setCurrState( EReserveState.RESERVE, "By server connection break" );
        return true;
      }
      if( sickChanged && sick ) {
        // поплохело
        setCurrState( EReserveState.RESERVE, "By critical health state" );
        return true;
      }
      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }

  }

  /**
   * Переход из резерв-авто в резерв-авто при потере связи с парным шкафом
   *
   * @author max
   */
  class ReserveAutoPairStateInvalidRule
      extends AbstractRule {

    public ReserveAutoPairStateInvalidRule() {
      super( EReserveState.RESERVE_AUTO, EReserveState.RESERVE_AUTO );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected boolean doCheckAndStart() {
      if( partnerBoxStateChanged && partnerBoxState == EPartnerBoxReserveState.UNKNOWN ) {
        // Сосед умер (meanderInvalid), возможно, что в системе отсутствует Main!!!

        networkDataSender.sendEvent( EReserveEvent.NO_MAIN, IList.EMPTY );
        return true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }
  }

  /**
   * Переход из главного в главного-инвалид по команде
   *
   * @author max
   */
  class MainCmdMainInvalidToMainInvalidRule
      extends AbstractRule {

    public MainCmdMainInvalidToMainInvalidRule() {
      super( EReserveState.MAIN, EReserveState.MAIN_INVALID, EReserveCommand.CMD_MAIN_INVALID );
    }

    @Override
    protected boolean doCheckAndStart() {

      // Перешел из Main в MainInvalid по команде от оператора имярек.
      setCurrState( EReserveState.MAIN_INVALID, "By operator command" );
      return true;

    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }
  }

  /**
   * Переход из главного в резерв-авто по команде
   *
   * @author max
   */
  class MainCmdReserveToReserveAutoRule
      extends AbstractRule {

    public MainCmdReserveToReserveAutoRule() {
      super( EReserveState.MAIN, EReserveState.RESERVE_AUTO, EReserveCommand.CMD_RESERVE );
    }

    @Override
    protected boolean doCheckAndStart() {

      // Перешел из из Main в ReserveAuto по команде оператора имярек
      startTransmitionFromMainToReserve();
      return true;

    }

    @Override
    public boolean isDoneImpl() {
      boolean isDone = !dLMsController.isDLMsRun();
      // Перешел из из Main в ReserveAuto по команде оператора имярек
      if( isDone ) {
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.RESERVE );
        setCurrState( EReserveState.RESERVE_AUTO, "By operator command" );
      }
      return isDone;
    }
  }

  /**
   * Переход из главного в резерв по потере сервера или здоровья
   *
   * @author max
   */
  class MainToReserveRule
      extends AbstractRule {

    public MainToReserveRule() {
      super( EReserveState.MAIN, EReserveState.RESERVE );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( connectionBreakChanged && connectionBreak ) {
        // потеряна связь с локальным сервером
        startTransmitionFromMainToReserve();
        return true;
      }
      if( sickChanged && sick ) {
        // поплохело
        startTransmitionFromMainToReserve();
        return true;
      }
      return false;
    }

    @Override
    public boolean isDoneImpl() {
      boolean isDone = !dLMsController.isDLMsRun();
      if( isDone ) {
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.RESERVE );
        setCurrState( EReserveState.RESERVE, "By sick or connection break" );
      }
      return isDone;
    }

  }

  /**
   * Переход из главного в резерв-авто аварийно по меандру главный от парного шкафа
   *
   * @author max
   */
  class MainPairStateMainToReserveAutoRule
      extends AbstractRule {

    private boolean strictReserve = false;

    /**
     * Конструктор с признаком того, что переход строго в состояние резерва (не авто).
     *
     * @param aStrictReserve признак того, что переход строго в состояние резерва (не авто)
     */
    public MainPairStateMainToReserveAutoRule( boolean aStrictReserve ) {
      super( EReserveState.MAIN, aStrictReserve ? EReserveState.RESERVE : EReserveState.RESERVE_AUTO );
      strictReserve = aStrictReserve;
    }

    public MainPairStateMainToReserveAutoRule() {
      super( EReserveState.MAIN, EReserveState.RESERVE_AUTO );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( partnerBoxStateChanged && partnerBoxState == EPartnerBoxReserveState.MAIN ) {
        // Аварийно перешел из Main в ReserveAuto по причине meanderMain на соседнем шкафу
        startTransmitionFromMainToReserve();
        return true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      boolean isDone = !dLMsController.isDLMsRun();
      if( isDone ) {
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.RESERVE );
        setCurrState( strictReserve ? EReserveState.RESERVE : EReserveState.RESERVE_AUTO,
            "By alarm main partner state" );
      }
      return isDone;
    }
  }

  /**
   * Переход из главного в главный при пропаже парного шкафа
   *
   * @author max
   */
  class MainPairStateInvalidToMainRule
      extends AbstractRule {

    public MainPairStateInvalidToMainRule() {
      super( EReserveState.MAIN, EReserveState.MAIN );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected boolean doCheckAndStart() {
      if( partnerBoxStateChanged && partnerBoxState == EPartnerBoxReserveState.UNKNOWN ) {
        // Сосед умер (meanderInvalid)
        networkDataSender.sendEvent( EReserveEvent.PARTNER_UNDEF, IList.EMPTY );
        return true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }
  }

  /**
   * Переход из главного-инвалид в главный по команде
   *
   * @author max
   */
  class MainInvalidCmdMainToMainRule
      extends AbstractRule {

    public MainInvalidCmdMainToMainRule() {
      super( EReserveState.MAIN_INVALID, EReserveState.MAIN, EReserveCommand.CDM_MAIN );
    }

    @Override
    protected boolean doCheckAndStart() {

      if( sick ) {
        // не могу перейти в Main по причине sick
        networkDataSender.sendEvent( EReserveEvent.CANT_CHANGE,
            new ElemArrayList<>( avStr( EReserveState.MAIN_INVALID.id() ),
                avStr( "Шкаф не может стать главным по причине критической потери функциональности" ) ) );
        // newState = EReserveState.MAIN_INVALID;
      }
      else {
        // Перешел из MainInvalid в Main по команде от оператора имярек
        setCurrState( EReserveState.MAIN, "By operator command" );
      }
      return true;

    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }
  }

  /**
   * Переход из главного-инвалид в резерв по команде
   *
   * @author max
   */
  class MainInvalidCmdReserveToReserveMainRule
      extends AbstractRule {

    public MainInvalidCmdReserveToReserveMainRule() {
      super( EReserveState.MAIN_INVALID, EReserveState.RESERVE, EReserveCommand.CMD_RESERVE );
    }

    @Override
    protected boolean doCheckAndStart() {

      // Перешел из MainInvalid в Reserve по команде от оператора имярек
      startTransmitionFromMainToReserve();
      return true;

    }

    @Override
    public boolean isDoneImpl() {
      boolean isDone = !dLMsController.isDLMsRun();
      if( isDone ) {
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.RESERVE );
        setCurrState( EReserveState.RESERVE, "By operator command" );
      }
      return isDone;
    }
  }

  /**
   * Переход из главного-инвалид в резерв по по потере соединения с сервером
   *
   * @author max
   */
  class MainInvalidConnLostToReserveMainRule
      extends AbstractRule {

    public MainInvalidConnLostToReserveMainRule() {
      super( EReserveState.MAIN_INVALID, EReserveState.RESERVE );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( connectionBreakChanged && connectionBreak ) {
        // потеряна связь с локальным сервером
        startTransmitionFromMainToReserve();
        return true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      boolean isDone = !dLMsController.isDLMsRun();
      if( isDone ) {
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.RESERVE );
        setCurrState( EReserveState.RESERVE, "By server connection break" );
      }
      return isDone;
    }
  }

  /**
   * Переход из главного-инвалид в резерв аварийно по меандру главный от парного шкафа
   *
   * @author max
   */
  class MainInvalidPairStateMainToReserveRule
      extends AbstractRule {

    public MainInvalidPairStateMainToReserveRule() {
      super( EReserveState.MAIN_INVALID, EReserveState.RESERVE );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( partnerBoxStateChanged && partnerBoxState == EPartnerBoxReserveState.MAIN ) {
        // Аварийно перешел из MainInvalid в Reserve по причине meanderMain на соседнем шкафу
        startTransmitionFromMainToReserve();
        return true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      boolean isDone = !dLMsController.isDLMsRun();
      if( isDone ) {
        // boxStateSender.setReserveSignalOwnState( EPartnerBoxReserveState.RESERVE );
        setCurrState( EReserveState.RESERVE, "By alarm main partner state" );
      }
      return isDone;
    }
  }

  /**
   * Переход из главного-инвалид в главный-инвалид при пропаже парного шкафа
   *
   * @author max
   */
  class MainInvalidPairStateInvalidToMainInvalidRule
      extends AbstractRule {

    public MainInvalidPairStateInvalidToMainInvalidRule() {
      super( EReserveState.MAIN_INVALID, EReserveState.MAIN_INVALID );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected boolean doCheckAndStart() {
      if( partnerBoxStateChanged && partnerBoxState == EPartnerBoxReserveState.UNKNOWN ) {
        // Сосед умер (meanderInvalid)
        networkDataSender.sendEvent( EReserveEvent.PARTNER_UNDEF, IList.EMPTY );
        return true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }
  }

  /**
   * Переход из главного-инвалид в главный-инвалид при пропаже здоровья
   *
   * @author max
   */
  class MainInvalidSickToMainInvalidRule
      extends AbstractRule {

    public MainInvalidSickToMainInvalidRule() {
      super( EReserveState.MAIN_INVALID, EReserveState.MAIN_INVALID );
    }

    @Override
    protected boolean doCheckAndStart() {
      if( sickChanged && sick ) {
        // Здоровье стало sick
        return true;
      }

      return false;
    }

    @Override
    public boolean isDoneImpl() {
      return true;
    }
  }

  private void startTransmitionFromReserveToMain() {
    networkDataSender.setFromReserveToMainTransition( true );
    dLMsController.startDLMs();
  }

  private void startTransmitionFromMainToReserve() {
    networkDataSender.setFromMainToReserveTransition( true );
    dLMsController.stopDlms();
  }

  /**
   * Заносит в контекст aContext параметры из командной стори и файла конфигурации.
   *
   * @param aContext {@link ITsContext} - подготавливамый контекст
   * @param aConfigFileName String - файл конфигурации
   */
  private static void prepareContextParams( ITsContext aContext, String aConfigFileName ) {
    ProgramArgs args = new ProgramArgs( new String[0] );
    PasUtils.prepareContextParams( aContext, args, aConfigFileName );
  }

  //
  // -----------------------------------------------------------------------
  // Утилитные методв (неплохо было бы перенести в утилитные классы)

  /**
   * Считывает набор параметров из файла конфигурации.
   * <p>
   * Формат файла конфигурации соответствует {@link IDvWriter#writeTypeConstraints(IOptionSet)} и не допускает никаких
   * комментариев или других данных.
   * <p>
   * Метод не выбрасывает исключений, все исключения ловятся и логируются. В случае ошибки возвращает пустой список
   * параметров.
   *
   * @param aFile {@link File} - файл для чтения конфигурации
   * @return {@link IOptionSetEdit} - счтанный или пустой набор параметров
   */
  private IOptionSet readConfigFile( File aFile ) {
    try {
      IOptionSet result = OptionSetKeeper.KEEPER.read( aFile );
      logger.info( INFO_MSG_CFG_FILE_READ, aFile.getAbsolutePath() );
      return result;
    }
    catch( Exception e ) {
      logger.warning( e, ERR_MSG_ERR_READING_CFG_FILE, aFile.getAbsolutePath() );
      return IOptionSet.NULL;
    }
  }
}

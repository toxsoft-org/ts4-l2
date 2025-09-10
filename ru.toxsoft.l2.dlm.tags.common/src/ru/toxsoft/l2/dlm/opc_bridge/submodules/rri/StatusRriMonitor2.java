package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.IStatusRriMonitorConsts.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.evserv.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.IComplexTag.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Монитор статуса НСИ OPC UA сервер. Инкапсулирует весь фунционал мониторинга и загрузки НСИ с сервера USkat. Его
 * задача "поймать" фронт флага "статус НСИ" в ноль и начать процесс выгрузки НСИ с USkat сервера. Кроме этого дает
 * возможность внешнему коду установить/сбросить флаг статуса НСИ.
 *
 * @author dima
 */
public class StatusRriMonitor2 {

  String EVPRMID_COMMENT = "errComment"; //$NON-NLS-1$

  /**
   * Контекст..
   */
  private IDlmContext context;

  private static final int ERR_COUNTER = 3;

  /**
   * Событие о сбое загрузки
   */
  private Gwid eventGwid;

  /**
   * Этап работы монитора
   */
  public enum MonitorRunningStage {

    /**
     * просто мониторим состояние тега "дай мне НСИ", ловим фронт 1 -> 0
     */
    MONITORING(),

    /**
     * начало процесса загрузки с сервера, читаем первый тег с сервера, пишем его в драйвер и выходим из такта
     */
    STARTING_DOWNLOAD(),

    /**
     * очередной шаг процесса загрузки с сервера, читаем следующий тег с сервера, пишем его в драйвер и выходим из такта
     */
    DOWNLOADING(),

    /**
     * заканчиваем процесс загрузки с сервера, пишем в комплексный тег команду "НСИ в норме" и ждем когда он успешно
     * отработается
     */
    END_DOWNLOADING();

  }

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( StatusRriMonitor2.class.getName() );

  // предыдущее состояние флага "НСИ в норме"
  protected IAtomicValue prevRriOk = IAtomicValue.NULL;

  /**
   * ITag для чтения статуса
   */
  protected ITag rStatusRriWS;

  /**
   * IComplexTag для записи статуса.
   */
  protected IComplexTag wStatusRri;

  /**
   * текущее состояние процесса мониторинга.
   */
  protected MonitorRunningStage currState = MonitorRunningStage.MONITORING;

  /**
   * id device
   */
  protected IAtomicValue deviceId = AvUtils.avStr( "opc2s5.bridge.collection.id" ); //$NON-NLS-1$

  /**
   * id тега на чтение
   */
  protected IAtomicValue rStatusRriWSNodeId = AvUtils.avStr( "ns=32769;i=4955" ); //$NON-NLS-1$

  /**
   * index in WS
   */
  protected IAtomicValue indexStatus = AvUtils.avInt( 12 );

  /**
   * id тега на запись
   */
  protected IAtomicValue complextStatusRriNodeId = AvUtils.avStr( "synthetic_ns_2_i_1832" ); //$NON-NLS-1$

  /**
   * cmd set
   */
  protected IAtomicValue cmdSetStatus = AvUtils.avInt( 24 );

  /**
   * cmd reset
   */
  protected IAtomicValue cmdResetStatus = AvUtils.avInt( 23 );

  /**
   * индекс текущего передатчика в процессе USkat -> OPC UA
   */
  private int currTransmitterIndex = 0;

  /**
   * Набор описаний данных, полученный из конфиг информации.
   */
  private IList<IRriDataTransmitter> pinRriDataTransmitters;

  /**
   * кол-во ошибок при передаче текуще записи USkat -> OPC UA
   */
  private int transferErrorCounter = 0;

  /**
   * метка времени установки статуса
   */
  private long setStatusTimestamp = 0;

  /**
   * метка времени начала текущей операции записи очередно параметра
   */
  private long currParamStartWritingTimestamp = 0;

  /**
   * флаг корректной конфигурации
   */
  private boolean configured = false;

  /**
   * флаг наличия ошибок в процессе загрузки НСИ
   */
  private boolean errInProcess = false;

  /**
   * маска для получения статуса НСИ из WS
   */
  private int mask;

  /**
   * Вызывается на этапе конфигурации
   *
   * @param aParams дерево описания параметров конфигурации модуля
   */
  @SuppressWarnings( "nls" )
  public void config( IAvTree aParams ) {
    // читаем описание конфигурации монитора
    IOptionSet rriMonitorParams = aParams.fields();
    if( rriMonitorParams.hasValue( RRI_STATUS_DEVICE_ID ) ) {
      deviceId = rriMonitorParams.getValue( RRI_STATUS_DEVICE_ID );
      if( rriMonitorParams.hasValue( RRI_STATUS_WS_READ_NODE_ID ) ) {
        rStatusRriWSNodeId = rriMonitorParams.getValue( RRI_STATUS_WS_READ_NODE_ID );
        indexStatus = rriMonitorParams.getValue( RRI_STATUS_WS_INDEX );
        mask = 0x1 << indexStatus.asInt();
        if( rriMonitorParams.hasValue( RRI_STATUS_COMPLEX_NODE_ID ) ) {
          complextStatusRriNodeId = rriMonitorParams.getValue( RRI_STATUS_COMPLEX_NODE_ID );
          if( rriMonitorParams.hasValue( RRI_STATUS_CMD_SET_ID ) ) {
            cmdSetStatus = rriMonitorParams.getValue( RRI_STATUS_CMD_SET_ID );
            if( rriMonitorParams.hasValue( RRI_STATUS_CMD_RESET_ID ) ) {
              cmdResetStatus = rriMonitorParams.getValue( RRI_STATUS_CMD_RESET_ID );
              // и здесь же прочитаем, если есть, описание события о сбое загрузки
              String classId = aParams.fields().getStr( CLASS_ID, "USkat_OPC_UA_Bridge" );
              String objName = aParams.fields().getStr( OBJ_NAME, "bridge" );
              String eventId = aParams.fields().getStr( EVENT_ID, "evtDownloadError" );
              eventGwid = Gwid.createEvent( classId, objName, eventId );
              configured = true;
            }
          }
        }
      }
    }
  }

  /**
   * Запускается поле конфигурирования, один раз перед обычной работой
   *
   * @param aContext - контекст приложения
   * @param aComplexTagsContainer - контейнер комплексных тегов
   * @param aPinRriDataTransmitters - передатчики
   */
  public void start( IDlmContext aContext, IComplexTagsContainer aComplexTagsContainer,
      IList<IRriDataTransmitter> aPinRriDataTransmitters ) {
    if( configured ) {
      context = aContext;
      // получаем нужные теги от драйвера OPC UA
      ITsOpc tagsDevice = (ITsOpc)aContext.hal().listSpecificDevices().getByKey( deviceId.asString() );
      rStatusRriWS = tagsDevice.tag( rStatusRriWSNodeId.asString() );
      // тут комплексный тег
      wStatusRri = aComplexTagsContainer.getComplexTagById( complextStatusRriNodeId.asString() );
      pinRriDataTransmitters = aPinRriDataTransmitters;
      prevRriOk = getStatus();
      // на старте оказалось что контроллер требует НСИ, сразу запускаем процесс
      if( !prevRriOk.asBool() ) {
        currState = MonitorRunningStage.STARTING_DOWNLOAD;
      }
    }
  }

  private IAtomicValue getStatus() {
    IAtomicValue wsValue = rStatusRriWS.get();
    boolean rriStatusBit = (wsValue.asInt() & mask) != 0;
    return AvUtils.avBool( rriStatusBit );
  }

  long setStatus() {
    IAtomicValue newStatus = AV_1;
    if( errInProcess ) {
      newStatus = AvUtils.avInt( 2 );
    }
    return wStatusRri.setValue( cmdSetStatus.asInt(), newStatus );
  }

  /**
   * Начало процесса загрузки НСИ с сервера uSkat
   */
  private void startDownload() {
    currTransmitterIndex = 0;
    errInProcess = false;
    IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
    transmitter.transmitUskat2OPC();
  }

  /**
   * Квант времени в потоке DLM'ов чтобы "подышать" коду мониторинга
   */
  public void doDoJob() {
    switch( currState ) {
      case DOWNLOADING:
        // проверяем что очередной параметр записался корректно и переходим к следующему или завершаем загрузку
        checkCurrentNdGoNext();
        break;
      case END_DOWNLOADING:
        finishTransfer();
        break;
      case MONITORING:
        // ловим переход 1 или 2 -> 0
        IAtomicValue currRriOk = getStatus();
        if( prevRriOk.asBool() && !currRriOk.asBool() ) {
          // поймали переход из было "НСИ норма", стало "НСИ нужно"
          prevRriOk = currRriOk;
          // переходим в загрузку
          currState = MonitorRunningStage.STARTING_DOWNLOAD;
        }
        break;
      case STARTING_DOWNLOAD:
        // пишем первый параметр
        startDownload();
        // переводим состояние в "загружаем"
        currState = MonitorRunningStage.DOWNLOADING;
        break;
      default:
        break;
    }
  }

  @SuppressWarnings( { "nls" } )
  private void checkCurrentNdGoNext() {
    // проверяем состояние передачи
    IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
    IComplexTag.EComplexTagState transferState = transmitter.getOpcCmdState();
    switch( transferState ) {
      case DONE: {
        // очередное значение записалось успешно, переходим к следующему
        logger.debug( "Done to writing %s, index %d,  go next", transmitter.gwid2Section().keys().first(),
            Integer.valueOf( currTransmitterIndex ) );
        anywayProcessNext();
        break;
      }
      case ERROR:
        // произошла ошибка записи
        logger.error( "Error in writing Gwid: %s", transmitter.gwid2Section().keys().first().canonicalString() );
        transferErrorCounter++;
        if( transferErrorCounter > ERR_COUNTER ) {
          // логируем проблему и переходим к следующему параметру
          logError( "Fail to write Gwid: %s, go to the next...",
              transmitter.gwid2Section().keys().first().canonicalString() );
          anywayProcessNext();
        }
        else {
          // делаем еще одну попытку c тем же параметром
          transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
          transmitter.transmitUskat2OPC();
          return;
        }
        break;
      case PROCESS:
        // запись в процессе выполнения
        logger.debug( "Writing Gwid %s in process, curr index %d",
            transmitter.gwid2Section().keys().first().canonicalString(), Integer.valueOf( currTransmitterIndex ) );
        // первый цикл записи этого параметра, запоминаем время начала
        // if( currParamStartWritingTimestamp == 0 ) {
        // currParamStartWritingTimestamp = System.currentTimeMillis();
        // return;
        // }
        // ставим проверку на зацикливание, если более 3-х секунд то логируем и переходим на следующий параметр
        // if( (System.currentTimeMillis() - currParamStartWritingTimestamp) <= 3000L ) {
        // return;
        // }
        // logError( "Writing operation too long. Fail to write Gwid: %s, go to the next...",
        // transmitter.gwid2Section().keys().first().canonicalString() );
        // anywayProcessNext();
        break;
      case TIMEOUT:
        // возник таймаут при записи, логируем и пробуем еще 2 раза
        logger.warning( "Timeout in process of writing Gwid: %s ",
            transmitter.gwid2Section().keys().first().canonicalString() );
        transferErrorCounter++;
        if( transferErrorCounter > ERR_COUNTER ) {
          logError( "Timeout in write Gwid: %s, go to the next",
              transmitter.gwid2Section().keys().first().canonicalString() );
          anywayProcessNext();
        }
        break;
      case UNKNOWN:
        logError( "UNKNOWN state in process of writing Gwid: %s, go to the next",
            transmitter.gwid2Section().keys().first().canonicalString() );
        anywayProcessNext();
        break;
      default:
        break;
    }
    // проверяем что еще есть незаписанные параметры
    if( hasMoreDownload() ) {
      transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
      transmitter.transmitUskat2OPC();
    }
    else {
      // переходим к окончанию процесса загрузки
      currState = MonitorRunningStage.END_DOWNLOADING;
    }
  }

  /**
   * Набор действий при переходе к записи следующего параметра совершаемый в любом случае
   */
  private void anywayProcessNext() {
    // переходим к следующей записи
    currTransmitterIndex++;
    // сбрасываем текущий счетчик ошибок
    transferErrorCounter = 0;
    // сбрасываем время начала записи очередного параметра
    currParamStartWritingTimestamp = 0;
  }

  private void logError( String aFmtString, String aGwidStr ) {
    // логируем
    logger.error( aFmtString, aGwidStr );
    // формируем событие об ошибке в процессе загрузки
    String evtStr = String.format( aFmtString, aGwidStr );
    IOptionSetEdit params = new OptionSet();
    params.setStr( EVPRMID_COMMENT, evtStr );
    SkEvent ev = new SkEvent( System.currentTimeMillis(), eventGwid, params );
    context.network().getSkConnection().coreApi().eventService().fireEvent( ev );
    errInProcess = true;
    transferErrorCounter = 0;
  }

  private boolean hasMoreDownload() {
    return currTransmitterIndex < pinRriDataTransmitters.size();
    // for debug
    // return currTransmitterIndex + 1 < 5;
  }

  @SuppressWarnings( "nls" )
  private void finishTransfer() {
    // все НСИ записали, ставим флаг "НСИ Ok"
    if( setStatusTimestamp == 0 && !wStatusRri.isBusy() ) {
      // только в случае если флаг еще не пытались установить
      setStatusTimestamp = setStatus();
    }
    else {
      // здесь мы в процессе установки флага "НСИ контроллера OK"
      EComplexTagState flagState = wStatusRri.getState( setStatusTimestamp, false );
      switch( flagState ) {
        case DONE:
          logger.debug( "Process dowloading RRI Uskat -> OPC UA completed." );
          setStatusTimestamp = 0;
          csMonitoring();
          break;
        case ERROR:
          // произошла ошибка записи, повторяем
          logger.error( "Error dowloading RRI Uskat -> OPC UA" );
          transferErrorCounter++;
          if( transferErrorCounter > ERR_COUNTER ) {
            logger.error( "Fail dowloading RRI Uskat -> OPC UA. There were %d try",
                Integer.valueOf( currTransmitterIndex ) );
            transferErrorCounter = 0;
            setStatusTimestamp = 0;
            csMonitoring();
          }
          else {
            setStatusTimestamp = setStatus();
          }
          break;
        case PROCESS:
          // nop
          break;
        case TIMEOUT:
          // возник таймаут при записи, логируем и пробуем еще 2 раза
          logger.warning( "Timeout dowloading RRI Uskat -> OPC UA" );
          transferErrorCounter++;
          if( transferErrorCounter > ERR_COUNTER ) {
            logger.error( "Fail dowloading RRI Uskat -> OPC UA. There were %d try",
                Integer.valueOf( currTransmitterIndex ) );
            transferErrorCounter = 0;
            setStatusTimestamp = 0;
            csMonitoring();
          }
          else {
            setStatusTimestamp = setStatus();
          }
          break;
        case UNKNOWN:
          logger.error( "UNKNOWN state in process set StatusRRI" );
          setStatusTimestamp = 0;
          csMonitoring();
          break;
        default:
          break;
      }
    }
  }

  /**
   * переключения состояния алгоритма в режим мониторинга
   */
  private void csMonitoring() {
    // переходим в режим мониторинга
    currState = MonitorRunningStage.MONITORING;
    prevRriOk = getStatus();
  }

  /**
   * @return true - module correctly configured, else - false
   */
  public boolean isConfigured() {
    return configured;
  }

}

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
  protected ITag rStatusRri;

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
  protected IAtomicValue rStatusRriNodeId = AvUtils.avStr( "ns=32769;i=4955" ); //$NON-NLS-1$

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
   * флаг корректной конфигурации
   */
  private boolean configured = false;

  /**
   * флаг наличия ошибок в процессе загрузки НСИ
   */
  private boolean errInProcess = false;

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
      if( rriMonitorParams.hasValue( RRI_STATUS_READ_NODE_ID ) ) {
        rStatusRriNodeId = rriMonitorParams.getValue( RRI_STATUS_READ_NODE_ID );
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
      rStatusRri = tagsDevice.tag( rStatusRriNodeId.asString() );
      // тут комплексный тег
      wStatusRri = aComplexTagsContainer.getComplexTagById( complextStatusRriNodeId.asString() );
      pinRriDataTransmitters = aPinRriDataTransmitters;
    }
  }

  long setStatus() {
    IAtomicValue newStatus = AV_1;
    if( errInProcess ) {
      newStatus = AvUtils.avInt( 2 );
    }
    return wStatusRri.setValue( cmdSetStatus.asInt(), newStatus );
  }

  long resetStatus() {
    return wStatusRri.setValue( cmdResetStatus.asInt(), IAtomicValue.NULL );
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
        // ловим переход не0 -> 0
        IAtomicValue currRriOk = rStatusRri.get();
        if( (prevRriOk.equals( IAtomicValue.NULL ) || prevRriOk.asInt() != 0) && (currRriOk.asInt() == 0) ) {
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

  @SuppressWarnings( "nls" )
  private void checkCurrentNdGoNext() {
    // проверяем состояние передачи
    IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
    IComplexTag.EComplexTagState transferState = transmitter.getOpcCmdState();
    switch( transferState ) {
      case DONE: {
        // очередное значение записалось успешно, переходим к следующему
        logger.debug( "Done to writing %s, index %d,  go next", transmitter.gwid2Section().keys().first(),
            Integer.valueOf( currTransmitterIndex ) );
        transferErrorCounter = 0;
        break;
      }
      case ERROR:
        // произошла ошибка записи
        logger.error( "Error in writing %s", transmitter.gwid2Section().keys().first() );
        transferErrorCounter++;
        if( transferErrorCounter > ERR_COUNTER ) {
          // логируем проблему и переходим к следующему параметру
          logError( "Fail to writing %s, go to the next one...",
              transmitter.gwid2Section().keys().first().canonicalString() );
        }
        else {
          // делаем еще одну попытку c тем же параметром
          transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
          transmitter.transmitUskat2OPC();
          return;
        }
        break;
      case PROCESS:
        // запись в процессе выполнения, ничего не делаем, ждем следующего цикла
        // nop
        break;
      case TIMEOUT:
        // возник таймаут при записи, логируем и пробуем еще 2 раза
        logger.warning( "Timeout in writing %s", transmitter.gwid2Section().keys().first() );
        transferErrorCounter++;
        if( transferErrorCounter > ERR_COUNTER ) {
          logError( "Timeout in writing %s, go next", transmitter.gwid2Section().keys().first().canonicalString() );
        }
        break;
      case UNKNOWN:
        logError( "UNKNOWN state in writing %s, index %d, go next",
            transmitter.gwid2Section().keys().first().canonicalString() );
        break;
      default:
        break;
    }
    // переходим к следующему параметру НСИ
    ++currTransmitterIndex;
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
    // return currTransmitterIndex + 1 < 3;
  }

  @SuppressWarnings( "nls" )
  private void finishTransfer() {
    // все НСИ записали, ставим флаг "НСИ Ok"
    if( !wStatusRri.isBusy() ) {
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
          }
          else {
            setStatusTimestamp = setStatus();
          }
          break;
        case UNKNOWN:
          logger.error( "UNKNOWN state in process set StatusRRI" );
          break;
        default:
          break;
      }
    }
    // переходим в режим мониторинга
    currState = MonitorRunningStage.MONITORING;
  }

  /**
   * @return true - module correctly configured, else - false
   */
  public boolean isConfigured() {
    return configured;
  }

}

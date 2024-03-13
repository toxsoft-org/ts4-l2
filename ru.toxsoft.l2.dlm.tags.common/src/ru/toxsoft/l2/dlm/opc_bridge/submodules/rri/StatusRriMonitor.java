package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.IComplexTag.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Монитор статуса НСИ OPC UA сервер. Его задача "поймать" переход флага "статус НСИ" в ноль и начать процесс выгрузки
 * НСИ с USkat сервера. Кроме этого дает возможность внешнему коду установить/сбросить флаг статуса НСИ.
 *
 * @author dima
 */
public class StatusRriMonitor
    implements IStatusRriMonitor {

  private static final int ERR_COUNTER = 3;

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( StatusRriMonitor.class.getName() );

  /**
   * ITag для чтения статуса
   */
  protected ITag rStatusRri;

  /**
   * IComplexTag для записи статуса.
   */
  protected IComplexTag wStatusRri;

  /**
   * ERriControllerState текущее состояние НСИ контроллера.
   */
  protected ERriControllerState currState = ERriControllerState.UNKNOWN;

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
   * Этапы загрузки НСИ с сервера USkat
   *
   * @author max
   */
  public enum EUskatDownloadStage {

    /**
     * считывание перенос значений USkat -> OPC UA
     */
    TRANSFERING_USKAT_OPC_UA(),

    /**
     * установка флага НСИ Ok
     */
    SETTING_RRI_OK();

  }

  @Override
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
              configured = true;
            }
          }
        }
      }
    }
  }

  @Override
  public void start( IDlmContext aContext, IComplexTagsContainer aComplexTagsContainer,
      IList<IRriDataTransmitter> aPinRriDataTransmitters ) {
    if( isConfigured() ) {
      // получаем нужные теги от драйвера OPC UA
      ITsOpc tagsDevice = (ITsOpc)aContext.hal().listSpecificDevices().getByKey( deviceId.asString() );
      rStatusRri = tagsDevice.tag( rStatusRriNodeId.asString() );
      // тут комплексный тег
      wStatusRri = aComplexTagsContainer.getComplexTagById( complextStatusRriNodeId.asString() );
      pinRriDataTransmitters = aPinRriDataTransmitters;
    }
  }

  @Override
  public long setStatus() {
    return wStatusRri.setValue( cmdSetStatus.asInt(), IAtomicValue.NULL );
  }

  @Override
  public long resetStatus() {
    return wStatusRri.setValue( cmdResetStatus.asInt(), IAtomicValue.NULL );
  }

  @Override
  public ERriControllerState getState() {
    // читаем состояни тега
    IAtomicValue stateFlag = rStatusRri.get();
    switch( currState ) {
      case NEED_DOWNLOAD_USKAT_RRI:
        // в запросе на НСИ с сервера USkat
        if( stateFlag.asInt() == 0 ) {
          currState = ERriControllerState.USKAT_RRI_LOADING;
        }
        break;
      case RRI_CONTROLLER_OK:
        // работаем с нормальным НСИ
        if( stateFlag.asInt() == 0 ) {
          currState = ERriControllerState.NEED_DOWNLOAD_USKAT_RRI;
        }
        else
          if( stateFlag.asInt() == 1 ) {
            currState = ERriControllerState.RRI_CONTROLLER_OK;
          }

        break;
      case UNKNOWN:
        // начало работы
        if( stateFlag.asInt() == 0 ) {
          // контроллер хочет НСИ сверху
          currState = ERriControllerState.NEED_DOWNLOAD_USKAT_RRI;
        }
        else
          if( stateFlag.asInt() == 1 ) {
            // НСИ контроллера в норме, читаем с него и пишем на сервер USkat
            currState = ERriControllerState.RRI_CONTROLLER_OK;
          }
        break;
      case USKAT_RRI_LOADING:
        // загрузка НСИ сверху
        if( stateFlag.asInt() == 1 ) {
          // отрабатываем окончание загрузки НСИ с сервера USkat
          currState = ERriControllerState.RRI_CONTROLLER_OK;
        }
        break;
      default:
        break;
    }
    return currState;
  }

  @Override
  public void startDownload() {
    currTransmitterIndex = 0;
    IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
    transmitter.transmitUskat2OPC();
  }

  @Override
  public void processDownload() {
    // процесс передачи состоит из этап загрузки НСИ и этапа установки флага "НСИ OK"
    EUskatDownloadStage processStage = processStage();
    switch( processStage ) {
      case SETTING_RRI_OK:
        finishTransfer();
        break;
      case TRANSFERING_USKAT_OPC_UA:
        transferUskat2OPC_UA();
        break;
      default:
        break;
    }
  }

  private EUskatDownloadStage processStage() {
    if( hasMoreDownload() ) {
      return EUskatDownloadStage.TRANSFERING_USKAT_OPC_UA;
    }
    return EUskatDownloadStage.SETTING_RRI_OK;
  }

  private boolean hasMoreDownload() {
    // return currTransmitterIndex < pinRriDataTransmitters.size();
    // for debug
    return currTransmitterIndex + 1 < 3;
  }

  @SuppressWarnings( "nls" )
  private void transferUskat2OPC_UA() {
    // проверяем состояние передачи
    IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
    IComplexTag.EComplexTagState transferState = transmitter.getOpcCmdState();
    switch( transferState ) {
      case DONE: {
        // очередное значение записалось успешно, переходим к следующему
        logger.debug( "Done to writing %s, index %d,  go next", transmitter.gwid2Section().keys().first(),
            Integer.valueOf( currTransmitterIndex ) );
        transferErrorCounter = 0;
        processNext();
        break;
      }
      case ERROR:
        // произошла ошибка записи, повторяем
        logger.error( "Error in writing %s", transmitter.gwid2Section().keys().first() );
        transferErrorCounter++;
        if( transferErrorCounter > ERR_COUNTER ) {
          logger.error( "Fail to writing %s, go next", transmitter.gwid2Section().keys().first() );
          transferErrorCounter = 0;
          processNext();
        }
        else {
          transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
          transmitter.transmitUskat2OPC();
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
          logger.error( "Fail to writing %s, go next", transmitter.gwid2Section().keys().first() );
          transferErrorCounter = 0;
          processNext();
        }
        break;
      case UNKNOWN:
        logger.error( "UNKNOWN state in writing %s, , index %d, go next", transmitter.gwid2Section().keys().first(),
            Integer.valueOf( currTransmitterIndex ) );
        processNext();
        break;
      default:
        break;
    }
  }

  private void processNext() {
    if( hasMoreDownload() ) {
      // пишем следующий параметр НСИ
      currTransmitterIndex++;
      IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
      transmitter.transmitUskat2OPC();
    }
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
            setStatusTimestamp = 0;
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
  }

  @Override
  public boolean isConfigured() {
    return configured;
  }
}

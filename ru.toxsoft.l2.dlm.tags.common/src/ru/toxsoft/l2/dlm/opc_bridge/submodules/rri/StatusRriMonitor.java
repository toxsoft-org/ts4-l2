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
import ru.toxsoft.l2.thd.opc.*;

/**
 * Монитор статуса НСИ OPC UA сервер. Его задача "поймать" переход флага "статус НСИ" в ноль и начать процесс выгрузки
 * НСИ с USkat сервера. Кроме этого дает возможность внешнему коду установить/сбросить флаг статуса НСИ.
 *
 * @author dima
 */
public class StatusRriMonitor
    implements IStatusRriMonitor {

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
  protected IAtomicValue complextStatusRriNodeId = AvUtils.avStr( "syntetic_ns_2_i_1832" ); //$NON-NLS-1$

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
  private int                        currTransmitterIndex = 0;
  /**
   * Набор описаний данных, полученный из конфиг информации.
   */
  private IList<IRriDataTransmitter> pinRriDataTransmitters;

  @Override
  public void config( IAvTree aParams ) {
    // читаем описание конфигурации монитора
    IOptionSet rriMonitorParams = aParams.fields();
    if( rriMonitorParams.hasValue( RRI_STATUS_DEVICE_ID ) ) {
      deviceId = rriMonitorParams.getValue( RRI_STATUS_DEVICE_ID );
    }
    if( rriMonitorParams.hasValue( RRI_STATUS_READ_NODE_ID ) ) {
      rStatusRriNodeId = rriMonitorParams.getValue( RRI_STATUS_READ_NODE_ID );
    }
    if( rriMonitorParams.hasValue( RRI_STATUS_COMPLEX_NODE_ID ) ) {
      complextStatusRriNodeId = rriMonitorParams.getValue( RRI_STATUS_COMPLEX_NODE_ID );
    }
    if( rriMonitorParams.hasValue( RRI_STATUS_CMD_SET_ID ) ) {
      cmdSetStatus = rriMonitorParams.getValue( RRI_STATUS_CMD_SET_ID );
    }
    if( rriMonitorParams.hasValue( RRI_STATUS_CMD_RESET_ID ) ) {
      cmdResetStatus = rriMonitorParams.getValue( RRI_STATUS_CMD_RESET_ID );
    }
  }

  @Override
  public void start( IDlmContext aContext, IComplexTagsContainer aComplexTagsContainer,
      IList<IRriDataTransmitter> aPinRriDataTransmitters ) {
    // получаем нужные теги от драйвера OPC UA
    ITsOpc tagsDevice = (ITsOpc)aContext.hal().listSpecificDevices().getByKey( deviceId.asString() );
    rStatusRri = tagsDevice.tag( rStatusRriNodeId.asString() );
    // тут комплексный тег
    wStatusRri = aComplexTagsContainer.getComplexTagById( complextStatusRriNodeId.asString() );
    pinRriDataTransmitters = aPinRriDataTransmitters;
  }

  @Override
  public void setStatus() {
    wStatusRri.setValue( cmdSetStatus.asInt(), IAtomicValue.NULL );
  }

  @Override
  public void resetStatus() {
    wStatusRri.setValue( cmdResetStatus.asInt(), IAtomicValue.NULL );
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
    // проверяем состояние передачи
    IRriDataTransmitter transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
    IComplexTag.EComplexTagState transferState = transmitter.getOpcCmdState();
    switch( transferState ) {
      case DONE:
        // очередное значение записалось успешно
        if( currTransmitterIndex + 1 >= pinRriDataTransmitters.size() ) {
          // все записали, гасим флаг "контроллеру нужен НСИ сверху"
          if( !wStatusRri.isDirty() ) {
            // в случае если флаг установили, то ставим
            setStatus();
          }
          // тут ничего не делаем, просто ждем
        }
        else {
          // пишем следующий параметр НСИ
          currTransmitterIndex++;
          transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
          transmitter.transmitUskat2OPC();
        }

        break;
      case ERROR:
        // произошла ошибка записи, повторяем
        transmitter = pinRriDataTransmitters.get( currTransmitterIndex );
        transmitter.transmitUskat2OPC();
        break;
      case PROCESS:
        // запись в процессе выполнения, ничего не делаем, ждем следующего цикла
        // nop
        break;
      case TIMEOUT:
        break;
      case UNKNOWN:
        break;
      default:
        break;
    }
  }

}

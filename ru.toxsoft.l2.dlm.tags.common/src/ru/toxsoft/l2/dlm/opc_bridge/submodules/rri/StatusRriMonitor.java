package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.dlm.*;
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
  protected IAtomicValue deviceId = AvUtils.avStr( "status.rri.read.tag.id" );

  /**
   * id тега на чтение
   */
  protected IAtomicValue rStatusRriNodeId = AvUtils.avStr( "status.rri.read.tag.id" );

  /**
   * id тега на запись
   */
  protected IAtomicValue wStatusRriNodeId = AvUtils.avStr( "syntetic1" );

  /**
   * cmd address
   */
  protected IAtomicValue cmdIndex = AvUtils.avInt( 13 );

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
    if( rriMonitorParams.hasValue( RRI_STATUS_WRITE_NODE_ID ) ) {
      wStatusRriNodeId = rriMonitorParams.getValue( RRI_STATUS_WRITE_NODE_ID );
    }
    if( rriMonitorParams.hasValue( RRI_STATUS_CMD_ID ) ) {
      cmdIndex = rriMonitorParams.getValue( RRI_STATUS_CMD_ID );
    }
  }

  @Override
  public void start( IDlmContext aContext ) {
    // получаем нужные теги от драйвера OPC UA
    ITsOpc tagsDevice = (ITsOpc)aContext.hal().listSpecificDevices().getByKey( deviceId.asString() );
    rStatusRri = tagsDevice.tag( rStatusRriNodeId.asString() );
    // FIXME
    wStatusRri = (IComplexTag)tagsDevice.tag( wStatusRriNodeId.asString() );

  }

  @Override
  public void setStatus( Integer aValue ) {
    wStatusRri.setValue( cmdIndex.asInt(), AvUtils.avInt( aValue.intValue() ) );
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

}

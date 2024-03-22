package org.toxsoft.l2.thd.modbus.common;

import static org.toxsoft.l2.thd.modbus.common.IModbusThdConstants.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;

/**
 * Поставщик универсальных аппаратов, работающих по протоколу Modbus. // сделать хитрый продюсер - каждое соединение -
 * своё специфическое устройство, и сделать одно интегральное устройство - но все его методы оставить пустыми - только
 * API выставлять наружу. Продумать диагностику и тайм-ауты.
 *
 * @author MAX
 */
public class CommonModbusDeviceProducer
    implements IDevicesProducer {

  /**
   * Обработчик ошибок нижнего уровня.
   */
  private IHalErrorProcessor errorProcessor;

  private IStridablesListEdit<AbstractSpecificDevice> specDevices = new StridablesList<>();

  @Override
  public void configYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {
    IAvTree params = aConfig.params();

    // Массив соединений
    IAvTree connections = params.nodes().findByKey( CONNECTIONS_PARAM_ID );
    if( connections == null ) {
      throw new TsIllegalArgumentRtException();
    }
    for( int i = 0; i < connections.arrayLength(); i++ ) {
      IAvTree devices = connections.arrayElement( i );

      try {

        CommonModbusDevice dev =
            new CommonModbusDevice( devices.structId(), devices.structId(), devices, errorProcessor );
        specDevices.add( dev );
      }
      catch( Exception e ) {
        // Возможно нужно не перехватывать - а выбрасывать наверх - весь аппарат не прошёл конфигурацию
        LoggerUtils.errorLogger().error( e );
      }

    }

  }

  @Override
  public void setHalErrorProcessor( IHalErrorProcessor aErrorProcessor ) {
    errorProcessor = aErrorProcessor;
  }

  @Override
  public AbstractPinsDevice createPinsDevice()
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IStridablesList<AbstractSpecificDevice> createSpecificDevices()
      throws Exception {
    return specDevices;
  }

}

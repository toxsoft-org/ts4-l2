package org.toxsoft.l2.thd.s7;

import static org.toxsoft.l2.thd.s7.IS7ThdConstants.*;

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
 * Поставщик универсальных аппаратов, работающих по протоколу Siemens S7.
 *
 * @author dima
 */
public class S7DeviceProducer
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

    // Массив S7 устройств
    IAvTree s7Devices = params.nodes().findByKey( S7_DEVICES_PARAM_ID );
    if( s7Devices == null ) {
      throw new TsIllegalArgumentRtException();
    }
    for( int i = 0; i < s7Devices.arrayLength(); i++ ) {
      IAvTree deviceConfigTree = s7Devices.arrayElement( i );

      try {

        S7Device dev =
            new S7Device( deviceConfigTree.structId(), deviceConfigTree.structId(), deviceConfigTree, errorProcessor );
        specDevices.add( dev );
      }
      catch( Exception e ) {
        // Возможно нужно не перехватывать - а выбрасывать наверх - весь аппарат не прошёл конфигурацию
        LoggerUtils.error( e );
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

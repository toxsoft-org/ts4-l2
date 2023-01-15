package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;

/**
 * Создает коллекцию драйверов opc ua
 *
 * @author dima
 */
public class OpcUaMiloDriverProducer
    implements IDevicesProducer {

  /**
   * Обработчик ошибок нижнего уровня.
   */
  private IHalErrorProcessor errorProcessor;
  /**
   * Класс - мост OPC UA
   */
  private OpcUaMiloDriver    opcUaDriver;

  /**
   * Пустой конструктор.
   */
  public OpcUaMiloDriverProducer() {
  }

  @Override
  public void setHalErrorProcessor( IHalErrorProcessor aErrorProcessor ) {
    errorProcessor = aErrorProcessor;

  }

  @Override
  public void configYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {
    opcUaDriver = createOpcUaMiloDriver( aConfig );
  }

  /**
   * По описанию из конфигурации создает мостов opc2S5
   *
   * @param aConfig конфигурация мостов
   * @return коллекция коллекцию мостов opc2S5
   */
  private OpcUaMiloDriver createOpcUaMiloDriver( IUnitConfig aConfig ) {
    IAvTree params = aConfig.params();
    // Массив мостов
    IAvTree bridges = params.nodes().findByKey( BRIDGES_PARAM_NAME );

    if( bridges.arrayLength() > 0 ) {
      IAvTree bridgeConfig = bridges.arrayElement( 0 );
      String driverId = bridgeConfig.fields().getStr( ID );
      String driverDescr = bridgeConfig.fields().getStr( DESCRIPTION );

      return new OpcUaMiloDriver( driverId, driverDescr, errorProcessor, bridgeConfig );
    }

    return null;
  }

  @Override
  public AbstractPinsDevice createPinsDevice()
      throws Exception {
    return null;
  }

  @Override
  public IStridablesList<AbstractSpecificDevice> createSpecificDevices()
      throws Exception {
    StridablesList<AbstractSpecificDevice> devices = new StridablesList<>();
    if( opcUaDriver != null ) {
      devices.add( opcUaDriver );
    }
    return devices;
  }
}

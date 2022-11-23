package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Фабрика передатчиков - один к одному - один тег на одно данное.
 *
 * @author max
 * @param <T> - класс дата-сета.
 */
public class OneToOneDataTransmitterFactory<T extends ISkRtdataChannel>
    extends AbstractTransmitterFactory<T> {

  @Override
  public IDataTransmitter<T> createTransmitter( IAvTree aConfig ) {

    return new OneToOneDataTransmitter<>();
  }

}

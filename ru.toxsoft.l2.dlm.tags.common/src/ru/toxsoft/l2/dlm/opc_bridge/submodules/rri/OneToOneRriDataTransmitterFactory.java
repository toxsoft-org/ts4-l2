package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.tslib.av.avtree.*;

/**
 * Фабрика передатчиков - один к одному - один тег на одно rri данное.
 *
 * @author dima
 */
public class OneToOneRriDataTransmitterFactory
    extends AbstractRriTransmitterFactory {

  @Override
  public IRriDataTransmitter createTransmitter( IAvTree aConfig ) {

    return new OneToOneRriDataTransmitter();
  }

}

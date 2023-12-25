package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.tslib.av.avtree.*;

/**
 * Фабрика передатчиков - один int тег к одному bool rri данному.
 *
 * @author dima
 */
public class SingleIntToSingleBoolRriDataTransmitterFactory
    extends AbstractRriTransmitterFactory {

  @Override
  public IRriDataTransmitter createTransmitter( IAvTree aConfig ) {

    return new SingleIntToSingleBoolRriDataTransmitter();
  }

}

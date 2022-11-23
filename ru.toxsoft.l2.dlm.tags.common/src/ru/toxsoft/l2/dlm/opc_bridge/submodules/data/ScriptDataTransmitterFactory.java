package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Фабрика передатчиков логика которых программируется в скрипте, расположенном в конфигурационном файле
 *
 * @author max
 * @param <T> - класс дата-сета
 */
public class ScriptDataTransmitterFactory<T extends ISkRtdataChannel>
    extends AbstractTransmitterFactory<T> {

  @Override
  public IDataTransmitter<T> createTransmitter( IAvTree aConfig ) {

    return new ScriptDataTransmitter<>();
  }

}

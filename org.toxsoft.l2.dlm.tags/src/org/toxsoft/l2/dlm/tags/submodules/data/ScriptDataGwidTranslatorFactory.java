package org.toxsoft.l2.dlm.tags.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.l2.dlm.tags.*;

/**
 * Фабрика передатчиков логика которых программируется в скрипте, расположенном в конфигурационном файле
 *
 * @author max
 */
public class ScriptDataGwidTranslatorFactory
    extends AbstractTranslatorFactory {

  @Override
  public IDataGwidTranslator createTransmitter( IAvTree aConfig ) {

    return new ScriptDataGwidTranslator();
  }

}

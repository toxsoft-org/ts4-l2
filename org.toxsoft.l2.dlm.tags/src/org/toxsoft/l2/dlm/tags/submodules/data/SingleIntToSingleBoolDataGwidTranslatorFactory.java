package org.toxsoft.l2.dlm.tags.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.l2.dlm.tags.*;

/**
 * Фабрика передатчиков - один int тег к одному bool данному.
 *
 * @author max
 */
public class SingleIntToSingleBoolDataGwidTranslatorFactory
    extends AbstractTranslatorFactory {

  @Override
  public IDataGwidTranslator createTransmitter( IAvTree aConfig ) {

    return new SingleIntToSingleBoolDataGwidTranslator();
  }

}

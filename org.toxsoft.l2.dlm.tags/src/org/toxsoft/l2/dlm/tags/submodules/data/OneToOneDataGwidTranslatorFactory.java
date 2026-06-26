package org.toxsoft.l2.dlm.tags.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;

/**
 * Фабрика передатчиков - один к одному - один тег на одно данное.
 *
 * @author max
 */
public class OneToOneDataGwidTranslatorFactory
    extends AbstractTranslatorFactory {

  @Override
  public IDataGwidTranslator createTransmitter( IAvTree aConfig ) {
    return new OneToOneDataGwidTranslator();
  }

}

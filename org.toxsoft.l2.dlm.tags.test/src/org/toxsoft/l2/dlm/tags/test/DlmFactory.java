package org.toxsoft.l2.dlm.tags.test;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.dlm.impl.*;

/**
 * Фабрика модуля opc-моста.
 *
 * @author max
 */
public class DlmFactory
    extends AbstractDlmFactory {

  @Override
  protected IDlm doCreateDlm( IDlmContext aContext ) {
    return new TestTagsDlm( info(), aContext );
  }

}

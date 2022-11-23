package ru.toxsoft.l2.dlm.opc_bridge;

import ru.toxsoft.l2.core.dlm.IDlm;
import ru.toxsoft.l2.core.dlm.IDlmContext;
import ru.toxsoft.l2.core.dlm.impl.AbstractDlmFactory;

/**
 * Фабрика модуля opc-моста.
 *
 * @author max
 */
public class DlmFactory
    extends AbstractDlmFactory {

  @Override
  protected IDlm doCreateDlm( IDlmContext aContext ) {
    return new OpcBridgeDlm( info(), aContext );
  }

}

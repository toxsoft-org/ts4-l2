package org.toxsoft.l2.dlm.tags;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.l2.lib.dlms.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * Фабрика модуля opc-моста.
 *
 * @author max
 */
public class DlmFactory
    extends L2AbstractDlmFactory {

  protected DlmFactory( IPluginInfo aPluginInfo ) {
    super( aPluginInfo );

  }

  @Override
  protected L2AbstractDlm doCreateDlm( String aInstanceId, IOptionSet aParams ) {
    return new OpcBridgeDlm( aInstanceId, info(), aParams );
  }

}

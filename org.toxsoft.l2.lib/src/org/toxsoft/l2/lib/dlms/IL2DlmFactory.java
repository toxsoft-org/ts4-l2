package org.toxsoft.l2.lib.dlms;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.common.*;

/**
 * Factory for creating an instance of a dynamically loadable module.
 *
 * @author hazard157
 */
public interface IL2DlmFactory {

  /**
   * Returns information about the module this factory creates.
   * <p>
   * Created DLM returns the same information via {@link IL2Dlm#info()}.
   *
   * @return {@link DlmInfo} - information about the module
   */
  DlmInfo info();

  /**
   * Creates the DLM instance.
   * <p>
   * DLM instance will have ID as specified by {@link L2ModuleConfigFile#id()}.
   *
   * @param aConfig {@link L2ModuleConfigFile} - DLM configuration
   * @return {@link L2AbstractDlm} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  L2AbstractDlm createDlm( L2ModuleConfigFile aConfig );

}

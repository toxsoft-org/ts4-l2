package org.toxsoft.l2.lib.dlms;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Factory for creating an instance of a dynamically loadable module.
 *
 * @author hazard157
 */
public interface IDlmFactory {

  /**
   * Returns information about the module this factory creates.
   * <p>
   * Created DLM returns the same information via {@link IDlm#info()}.
   *
   * @return {@link DlmInfo} - information about the module
   */
  DlmInfo info();

  /**
   * Creates the DLM instance.
   *
   * @param aInstanceId String the DLM instance ID (an IDpath)
   * @param aParams {@link IOptionSet} - instance creation parameters
   * @return {@link L2AbstractDlm} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  L2AbstractDlm createDlm( String aInstanceId, IOptionSet aParams );

}

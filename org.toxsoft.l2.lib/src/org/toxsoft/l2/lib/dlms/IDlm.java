package org.toxsoft.l2.lib.dlms;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * The basic interface of the Dynamically Loadable Modules (DLM) implementation.
 * <p>
 * Implements {@link IStridable}, where:
 * <ul>
 * <li>{@link #id()} - is the DLM instance ID;</li>
 * <li>{@link #nmName()} - the instance name, just for developer to distinguish instances;</li>
 * <li>{@link #description()} - the instance description, usually empty, not used;</li>
 * </ul>
 * <p>
 * The {@link #params()} method contains the instance creation parameters of
 * {@link IDlmFactory#createDlm(String, IOptionSet)}. Several DLM instances created by one factory differs by instance
 * ID and creation parameters. Also {@link #params()} provides values for {@link #nmName()} and {@link #description()}
 * with options {@link IAvMetaConstants#TSID_NAME} and {@link IAvMetaConstants#TSID_DESCRIPTION}.
 *
 * @author hazard157
 */
public interface IDlm
    extends IStridableParameterized {

  /**
   * Returns identification information about the module.
   * <p>
   * This is the same information that is returned by this module's factory in the {@link IDlmFactory#info()} method.
   *
   * @return {@link DlmInfo} - information about the module
   */
  DlmInfo info();

}

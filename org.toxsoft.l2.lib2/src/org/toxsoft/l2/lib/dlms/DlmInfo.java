package org.toxsoft.l2.lib.dlms;

import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.l2.lib.*;

/**
 * Information about DLM is provided by {@link IDlm} itself.
 *
 * @author hazard157
 * @param moduleId String - module ID is the same as provider plugin ID {@link IPluginInfo#pluginId()}
 * @param version {@link TsVersion} - module version is the same as {@link IPluginInfo#pluginVersion()}
 * @param moduleName String - module name as defined by {@link IL2HardConstants#MF_ATTR_DLM_NAME}
 */
public record DlmInfo ( String moduleId, TsVersion version, String moduleName ) {

  /**
   * Constructor.
   *
   * @param moduleId String - module ID is the same as provider plugin ID {@link IPluginInfo#pluginId()}
   * @param version {@link TsVersion} - module version is the same as {@link IPluginInfo#pluginVersion()}
   * @param moduleName String - module name as defined by {@link IL2HardConstants#MF_ATTR_DLM_NAME}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public DlmInfo( String moduleId, TsVersion version, String moduleName ) {
    StridUtils.checkValidIdPath( moduleId );
    TsNullArgumentRtException.checkNulls( version, moduleName );
    this.moduleId = moduleId;
    this.version = version;
    this.moduleName = moduleName;
  }

}

package org.toxsoft.l2.lib.net;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.l2.lib.*;

/**
 * Package constants.
 *
 * @author hazard157
 */
public interface IL2NetworkConstants {

  /**
   * Network component configuration files subdirectory.
   * <p>
   * Note: this option definition is used only to display help.
   */
  IDataDef OPDEF_NETWORK_CFG_SUBDIR = DataDef.create( EL2ComponentKind.NETWORK.getCfgSubirOptionId(), STRING, //
      TSID_NAME, "Config subdir", //
      TSID_DESCRIPTION, "NETWORK (devices) configuration files subdirectory", //
      TSID_IS_MANDATORY, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( EL2ComponentKind.NETWORK.id() ) //
  );

  /**
   * All known L2 Network options.
   */
  IStridablesListEdit<IDataDef> ALL_NETWORK_ARG_OPDEFS = new StridablesList<>( //
      OPDEF_NETWORK_CFG_SUBDIR //
  );

}

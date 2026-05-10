package org.toxsoft.l2.lib;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;

/**
 * Global options - list of the L2 application and components configuration options.
 *
 * @author hazard157
 */
public interface IL2GlobalOptions {

  // ------------------------------------------------------------------------------------
  // L2 component: HAL

  /**
   * HAL component option definitions.
   */
  IStridablesListEdit<IDataDef> ALL_HAL_COMP_OPDEFS = new StridablesList<>( //
  //
  );

  // ------------------------------------------------------------------------------------
  // L2 component: DLM Manager

  /**
   * DLM manager component option definitions.
   */
  IStridablesListEdit<IDataDef> ALL_DLMMGR_COMP_OPDEFS = new StridablesList<>( //
  //
  );

  // ------------------------------------------------------------------------------------
  // L2 component: Network

  /**
   * Network component option definitions.
   */
  IStridablesListEdit<IDataDef> ALL_NET_COMP_OPDEFS = new StridablesList<>( //
  //
  );

  // ------------------------------------------------------------------------------------
  // L2 Application

  /**
   * L2 application configuration files root directory.
   * <p>
   * Directory contains sub-directories name {@link EL2ComponentKind#id()} with respective component module
   * configuration files.
   */
  IDataDef OPDEF_L2_COMP_CFG_DIR_ROOT = DataDef.create( "l2.app.ConfigDir", STRING, //$NON-NLS-1$
      TSID_NAME, STR_L2_COMP_CFG_DIR_ROOT, //
      TSID_DESCRIPTION, STR_L2_COMP_CFG_DIR_ROOT_D, //
      TSID_DEFAULT_VALUE, avStr( "l2-cfg" ) //$NON-NLS-1$
  );

  /**
   * All global option definitions.
   */
  IStridablesListEdit<IDataDef> ALL_GLOBAL_OPDEFS = new StridablesList<>( //
      OPDEF_L2_COMP_CFG_DIR_ROOT //
  );

}

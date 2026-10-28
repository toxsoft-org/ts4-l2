package org.toxsoft.l2.lib.l10n;

import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IL2LibSharedResources {

  // ------------------------------------------------------------------------------------
  // Core.xxx

  /**
   * {@link EL2ComponentKind}
   */
  String STR_L2CK_HAL       = Messages.getString( "Core.L2CompKind.STR_L2CK_HAL" );       //$NON-NLS-1$
  String STR_L2CK_HAL_D     = Messages.getString( "Core.L2CompKind.STR_L2CK_HAL_D" );     //$NON-NLS-1$
  String STR_L2CK_DLMMGR    = Messages.getString( "Core.L2CompKind.STR_L2CK_DLMMGR" );    //$NON-NLS-1$
  String STR_L2CK_DLMMGR_D  = Messages.getString( "Core.L2CompKind.STR_L2CK_DLMMGR_D" );  //$NON-NLS-1$
  String STR_L2CK_NETWORK   = Messages.getString( "Core.L2CompKind.STR_L2CK_NETWORK" );   //$NON-NLS-1$
  String STR_L2CK_NETWORK_D = Messages.getString( "Core.L2CompKind.STR_L2CK_NETWORK_D" ); //$NON-NLS-1$

  /**
   * {@link L2Application}
   */
  String FMT_L2_APP_FINISH_STATUS = Messages.getString( "Core.FMT_L2_APP_FINISH_STATUS" ); //$NON-NLS-1$

  /**
   * L2AbstractComponent
   */
  String FMT_INFO_L2COMP_INITED_QUIT         = Messages.getString( "Core.FMT_INFO_L2COMP_INITED_QUIT" );         //$NON-NLS-1$
  String FMT_ERR_NO_MODULE_FACTORY           = Messages.getString( "Core.FMT_ERR_NO_MODULE_FACTORY" );           //$NON-NLS-1$
  String FMT_WARN_NO_MODULE_FACTORY          = Messages.getString( "Core.FMT_WARN_NO_MODULE_FACTORY" );          //$NON-NLS-1$
  String FMT_ERR_FACTORY_CANT_CREATE_MODULE  = Messages.getString( "Core.FMT_ERR_FACTORY_CANT_CREATE_MODULE" );  //$NON-NLS-1$
  String FMT_WARN_FACTORY_CANT_CREATE_MODULE = Messages.getString( "Core.FMT_WARN_FACTORY_CANT_CREATE_MODULE" ); //$NON-NLS-1$
  String FMT_ERR_CANT_INIT_MODULE            = Messages.getString( "Core.FMT_ERR_CANT_INIT_MODULE" );            //$NON-NLS-1$
  String FMT_WARN_CANT_INIT_MODULE           = Messages.getString( "Core.FMT_WARN_CANT_INIT_MODULE" );           //$NON-NLS-1$
  String FMT_INFO_MODULE_INITED              = Messages.getString( "Core.FMT_INFO_MODULE_INITED" );              //$NON-NLS-1$
  String FMT_ERR_CANT_START_MODULE           = Messages.getString( "Core.FMT_ERR_CANT_START_MODULE" );           //$NON-NLS-1$
  String FMT_WARN_CANT_START_MODULE          = Messages.getString( "Core.FMT_WARN_CANT_START_MODULE" );          //$NON-NLS-1$
  String FMT_INFO_MODULE_STARTED             = Messages.getString( "Core.FMT_INFO_MODULE_STARTED" );             //$NON-NLS-1$
  String FMT_L2COMP_VOID_ERROR               = Messages.getString( "Core.FMT_L2COMP_VOID_ERROR" );               //$NON-NLS-1$

  /**
   * {@link L2AbstractModuleFactory}
   */
  String FMT_ERR_INV_CREATED_MODULE_CLASS = Messages.getString( "Core.FMT_ERR_INV_CREATED_MODULE_CLASS" ); //$NON-NLS-1$

  // // ------------------------------------------------------------------------------------
  // // Net.xxx
  //
  // /**
  // * {@link IL2GlobalOptions}
  // */
  // String STR_NET_L2_APP_SKOBJ_STRID = Messages.getString( "Net.STR_NET_L2_APP_SKOBJ_STRID" ); //$NON-NLS-1$
  // String STR_NET_L2_APP_SKOBJ_STRID_D = Messages.getString( "Net.STR_NET_L2_APP_SKOBJ_STRID_D" ); //$NON-NLS-1$
  //
  // /**
  // * {@link L2AbstractDlmFactory}
  // */
  // String MSG_ERR_DLM_CREATION_EXCEPTION = Messages.getString( "Dlm.MSG_ERR_DLM_CREATION_EXCEPTION" ); //$NON-NLS-1$
  // String MSG_ERR_NULL_DLM_CREATED = Messages.getString( "Dlm.MSG_ERR_NULL_DLM_CREATED" ); //$NON-NLS-1$
  // String MSG_ERR_DLM_CREATED_WITH_BAD_INFO = Messages.getString( "Dlm.MSG_ERR_DLM_CREATED_WITH_BAD_INFO" );
  // //$NON-NLS-1$
  // String MSG_ERR_DLM_CREATED_WITH_BAD_ID = Messages.getString( "Dlm.MSG_ERR_DLM_CREATED_WITH_BAD_INFO" );
  // //$NON-NLS-1$
  //
  // // ------------------------------------------------------------------------------------
  // // Core.xxx
  // //
  //
  // /**
  // * L2DlmManager
  // */
  // String FMT_DUP_MODULE_ID_IN_FILES_NO2_IGNORED = Messages.getString( "Core.FMT_DUP_MODULE_ID_IN_FILES_NO2_IGNORED"
  // ); //$NON-NLS-1$
  // String FMT_WARN_IGNORED_NOT_INITED_DLM = Messages.getString( "Core.FMT_WARN_IGNORED_NOT_INITED_DLM" );
  // //$NON-NLS-1$
  // String FMT_WARN_IGNORED_NOT_STARTED_DLM = Messages.getString( "Core.MSG_ERR_DLM_CREATED_WITH_BAD_ID" );
  // //$NON-NLS-1$
  //
  // /**
  // * L2CompCfgDir
  // */
  // String FMT_ERR_BAD_CFG_FILE_IGNORED = Messages.getString( "Core.FMT_ERR_BAD_CFG_FILE_IGNORED" ); //$NON-NLS-1$
  // String FMT_ERR_DUP_MID_CFG_FILE_IGNORED = Messages.getString( "Core.FMT_ERR_DUP_MID_CFG_FILE_IGNORED" );
  // //$NON-NLS-1$
  //
  // /**
  // * {@link IL2GlobalOptions}
  // */
  // String STR_L2_COMP_CFG_DIR_ROOT = Messages.getString( "Core.STR_L2_COMP_CFG_DIR_ROOT" ); //$NON-NLS-1$
  // String STR_L2_COMP_CFG_DIR_ROOT_D = Messages.getString( "Core.STR_L2_COMP_CFG_DIR_ROOT_D" ); //$NON-NLS-1$
  //
  // // ------------------------------------------------------------------------------------
  // // Hal.xxx
  // //
  // String FMT_ERR_CANT_START_DEVICE = Messages.getString( "Hal.FMT_ERR_CANT_START_DEVICE" ); //$NON-NLS-1$
  // String FMT_WARN_DUP_TAG_IGNORED = Messages.getString( "Hal.FMT_WARN_DUP_TAG_IGNORED" ); //$NON-NLS-1$

}

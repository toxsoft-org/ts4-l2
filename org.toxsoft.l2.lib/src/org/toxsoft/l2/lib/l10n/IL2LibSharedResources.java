package org.toxsoft.l2.lib.l10n;

import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IL2LibSharedResources {

  /**
   * {@link EL2ComponentKind}
   */
  String STR_L2CK_HAL       = Messages.getString( "L2CompKind.STR_L2CK_HAL" );       //$NON-NLS-1$
  String STR_L2CK_HAL_D     = Messages.getString( "L2CompKind.STR_L2CK_HAL_D" );     //$NON-NLS-1$
  String STR_L2CK_DLMMGR    = Messages.getString( "L2CompKind.STR_L2CK_DLMMGR" );    //$NON-NLS-1$
  String STR_L2CK_DLMMGR_D  = Messages.getString( "L2CompKind.STR_L2CK_DLMMGR_D" );  //$NON-NLS-1$
  String STR_L2CK_NETWORK   = Messages.getString( "L2CompKind.STR_L2CK_NETWORK" );   //$NON-NLS-1$
  String STR_L2CK_NETWORK_D = Messages.getString( "L2CompKind.STR_L2CK_NETWORK_D" ); //$NON-NLS-1$

  /**
   * {@link L2AbstractDlmFactory}
   */
  String MSG_ERR_DLM_CREATION_EXCEPTION    = Messages.getString( "Dlm.MSG_ERR_DLM_CREATION_EXCEPTION" );    //$NON-NLS-1$
  String MSG_ERR_NULL_DLM_CREATED          = Messages.getString( "Dlm.MSG_ERR_NULL_DLM_CREATED" );          //$NON-NLS-1$
  String MSG_ERR_DLM_CREATED_WITH_BAD_INFO = Messages.getString( "Dlm.MSG_ERR_DLM_CREATED_WITH_BAD_INFO" ); //$NON-NLS-1$

  /**
   * L2DlmManager
   */
  String FMT_WARN_IGNORED_NOT_INITED_DLM  = Messages.getString( "Core.FMT_WARN_IGNORED_NOT_INITED_DLM" );  //$NON-NLS-1$
  String FMT_WARN_IGNORED_NOT_STARTED_DLM = Messages.getString( "Core.FMT_WARN_IGNORED_NOT_STARTED_DLM" ); //$NON-NLS-1$

  /**
   * {@link L2Application}
   */
  String FMT_L2_APP_FINISH_STATUS = Messages.getString( "Core.FMT_L2_APP_FINISH_STATUS" ); //$NON-NLS-1$

  /**
   * L2AbstractComponent
   */
  String FMT_INFO_L2COMP_INITED_QUIT = Messages.getString( "Core.FMT_INFO_L2COMP_INITED_QUIT" ); //$NON-NLS-1$

  /**
   * L2CompCfgDir
   */
  String FMT_ERR_BAD_CFG_FILE_IGNORED     = Messages.getString( "Core.FMT_ERR_BAD_CFG_FILE_IGNORED" );     //$NON-NLS-1$
  String FMT_ERR_DUP_MID_CFG_FILE_IGNORED = Messages.getString( "Core.FMT_ERR_DUP_MID_CFG_FILE_IGNORED" ); //$NON-NLS-1$

  /**
   * {@link IL2Hal}
   */
  String FMT_ERR_NO_HAL_DEVICE_FACTORY = Messages.getString( "Hal.FMT_ERR_NO_HAL_DEVICE_FACTORY" ); //$NON-NLS-1$

  /**
   * {@link IL2GlobalOptions}
   */
  String STR_L2_COMP_CFG_DIR_ROOT   = Messages.getString( "Core.STR_L2_COMP_CFG_DIR_ROOT" );   //$NON-NLS-1$
  String STR_L2_COMP_CFG_DIR_ROOT_D = Messages.getString( "Core.STR_L2_COMP_CFG_DIR_ROOT_D" ); //$NON-NLS-1$

}

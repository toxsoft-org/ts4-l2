package org.toxsoft.l2.main.l10n;

import org.toxsoft.l2.main.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IL2MainSharedResources {

  /**
   * {@link L2Main}
   */
  String STR_HELLO                             = Messages.getString( "L2Main.STR_HELLO" );                             //$NON-NLS-1$
  String FMT_L2MAIN_FINISHED                   = Messages.getString( "L2Main.FMT_L2MAIN_FINISHED" );                   //$NON-NLS-1$
  String FMT_LOG_L2MAIN_FINISHED               = Messages.getString( "L2Main.FMT_LOG_L2MAIN_FINISHED" );               //$NON-NLS-1$
  String FMT_INF_EMPTY_CFG_FILE_CREATED        = Messages.getString( "L2Main.FMT_INF_EMPTY_CFG_FILE_CREATED" );        //$NON-NLS-1$
  String FMT_INF_CFG_FILE_READ_OK              = Messages.getString( "L2Main.FMT_INF_CFG_FILE_READ_OK" );              //$NON-NLS-1$
  String FMT_ERR_CFG_FILE_READ_FAIL            = Messages.getString( "L2Main.FMT_ERR_CFG_FILE_READ_FAIL" );            //$NON-NLS-1$
  String STR_HELP_SHOWN_EXIT                   = Messages.getString( "L2Main.STR_HELP_SHOWN_EXIT" );                   //$NON-NLS-1$
  String FMT_MSG_COMMAND_LINE_HELP             = Messages.getString( "L2Main.FMT_MSG_COMMAND_LINE_HELP" );             //$NON-NLS-1$
  String FMT_WARN_NON_IDPATH_CLINE_ARG_IGNORED = Messages.getString( "L2Main.FMT_WARN_NON_IDPATH_CLINE_ARG_IGNORED" ); //$NON-NLS-1$
  String FMT_WARN_INV_CLINE_ARG_VALUE_IGNORED  = Messages.getString( "L2Main.FMT_WARN_INV_CLINE_ARG_VALUE_IGNORED" );  //$NON-NLS-1$
  String FMT_ERR_INV_BACKEND_CLASS             = Messages.getString( "L2Main.FMT_ERR_INV_BACKEND_CLASS" );             //$NON-NLS-1$
  String FMT_ERR_STOP_TIMEOUTED                = Messages.getString( "L2Main.FMT_ERR_STOP_TIMEOUTED" );                //$NON-NLS-1$

  /**
   * {@link IL2MainConstants}
   */
  String STR_CLARG_HELP                 = Messages.getString( "L2MainConstants.STR_CLARG_HELP" );                 //$NON-NLS-1$
  String STR_CLARG_HELP_D               = Messages.getString( "L2MainConstants.STR_CLARG_HELP_D" );               //$NON-NLS-1$
  String STR_CLARG_MAIN_CFG_FILE_NAME   = Messages.getString( "L2MainConstants.STR_CLARG_MAIN_CFG_FILE_NAME" );   //$NON-NLS-1$
  String STR_CLARG_MAIN_CFG_FILE_NAME_D = Messages.getString( "L2MainConstants.STR_CLARG_MAIN_CFG_FILE_NAME_D" ); //$NON-NLS-1$
  String STR_LOGGER_CFG_RESCAN_SECS     = Messages.getString( "L2MainConstants.STR_LOGGER_CFG_RESCAN_SECS" );     //$NON-NLS-1$
  String STR_LOGGER_CFG_RESCAN_SECS_D   = Messages.getString( "L2MainConstants.STR_LOGGER_CFG_RESCAN_SECS_D" );   //$NON-NLS-1$
  String STR_SHUTDOWN_TIMEOUT_SECS      = Messages.getString( "L2MainConstants.STR_SHUTDOWN_TIMEOUT_SECS" );      //$NON-NLS-1$
  String STR_SHUTDOWN_TIMEOUT_SECS_D    = Messages.getString( "L2MainConstants.STR_SHUTDOWN_TIMEOUT_SECS_D" );    //$NON-NLS-1$
  String STR_USE_TEXT_CONSOLE           = Messages.getString( "L2MainConstants.STR_USE_TEXT_CONSOLE" );           //$NON-NLS-1$
  String STR_USE_TEXT_CONSOLE_D         = Messages.getString( "L2MainConstants.STR_USE_TEXT_CONSOLE_D" );         //$NON-NLS-1$
  String STR_BACKEND_PROVIDER_CLASS     = Messages.getString( "L2MainConstants.STR_BACKEND_PROVIDER_CLASS" );     //$NON-NLS-1$
  String STR_BACKEND_PROVIDER_CLASS_D   = Messages.getString( "L2MainConstants.STR_BACKEND_PROVIDER_CLASS_D" );   //$NON-NLS-1$

}

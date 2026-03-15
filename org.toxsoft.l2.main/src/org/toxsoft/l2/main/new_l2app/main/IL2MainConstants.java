package org.toxsoft.l2.main.new_l2app.main;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.progargs.*;
import org.toxsoft.l2.main.new_l2app.app.*;

/**
 * Constants used by application runner {@link L2Main}.
 * <p>
 * Command line of the {@link L2Main} must be specified as described in {@link ProgramArgs}, that is in form of
 * "<b><code>-argName argValue</code></b>". The <i><code>argName</code></i> must be an IDpath, while
 * <i><code>argValue</code></i> is an atomic value string representation. Each <i><code>argValue</code></i> is read by
 * {@link AtomicValueKeeper#KEEPER}.
 * <p>
 * Contains command line options used by {@link L2Main}. These options are <b>not</b> passed to the
 * {@link IL2Application}.
 *
 * @author hazard157
 */
public interface IL2MainConstants {

  // ------------------------------------------------------------------------------------
  // Command line arguments that are NOT represented as global options
  //

  /**
   * Displays help message and exists.
   */
  String CLINEARG_HELP = "help"; //$NON-NLS-1$

  /**
   * Specifies path to the main configuration file.
   */
  String CLINEARG_MAIN_CFG_FILE_NAME = "cfgFile"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Default values
  //

  /**
   * Default value of the {@link #CLINEARG_MAIN_CFG_FILE_NAME}.
   */
  String DEFAULT_MAIN_CFG_FILE_NAME = "l2main.cfg"; //$NON-NLS-1$

  /**
   * Default value of the {@link #OPDEF_LOGGER_CFG_RESCAN_SECS}.
   */
  int DEFAULT_LOGGER_CFG_RESCAN_SECS = 10;

  /**
   * Default value of the {@link #OPDEF_SHUTDOWN_TIMEOUT_SECS}.
   */
  int DEFAULT_SHUTDOWN_TIMEOUT_SECS = 5;

  /**
   * Default value of the {@link #OPDEF_USE_TEXT_CONSOLE}.
   */
  boolean DEFAULT_USE_TEXT_CONSOLE = false;

  /**
   * Default value of the {@link #OPDEF_BACKEND_PROVIDER_CLASS}.
   */
  // String DEFAULT_BACKEND_PROVIDER_CLASS = "org.toxsoft.uskat.s5.client.remote.S5RemoteBackendProvider"; //$NON-NLS-1$
  String DEFAULT_BACKEND_PROVIDER_CLASS = "org.toxsoft.uskat.backend.memtext.MtbBackendToFileProvider"; //$NON-NLS-1$
  // String DEFAULT_BACKEND_PROVIDER_CLASS = "org.toxsoft.uskat.backend.sqlite.SkBackendSqliteProvider"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Command line arguments that ARE represented as global options
  //

  /**
   * Prefix of some {@link L2Main} specific command line arguments.
   */
  String L2MAIN_SPECIFIC_CLINE_ARGS_PREFIX = "l2main"; //$NON-NLS-1$

  /**
   * ID and command line argument name for option {@link #OPDEF_LOGGER_CFG_RESCAN_SECS}.
   */
  String OPID_LOGGER_CFG_RESCAN_SECS = L2MAIN_SPECIFIC_CLINE_ARGS_PREFIX + ".LoggerCfgRescanSecs"; //$NON-NLS-1$

  /**
   * ID and command line argument name for option {@link #OPDEF_LOGGER_CFG_RESCAN_SECS}.
   */
  String OPID_SHUTDOWN_TIMEOUT_SECS = L2MAIN_SPECIFIC_CLINE_ARGS_PREFIX + ".ShutdownTimeoutSecs"; //$NON-NLS-1$

  /**
   * ID and command line argument name for option {@link #OPDEF_LOGGER_CFG_RESCAN_SECS}.
   */
  String OPID_USE_TEXT_CONSOLE = L2MAIN_SPECIFIC_CLINE_ARGS_PREFIX + ".UseTextConsole"; //$NON-NLS-1$

  /**
   * ID and command line argument name for option {@link #OPDEF_BACKEND_PROVIDER_CLASS}.
   */
  String OPID_BACKEND_PROVIDER_CLASS = L2MAIN_SPECIFIC_CLINE_ARGS_PREFIX + ".BackendProviderClassName"; //$NON-NLS-1$

  /**
   * Each specified seconds logger read configuration to accept changes, 0 means no rescan.
   */
  IDataDef OPDEF_LOGGER_CFG_RESCAN_SECS = DataDef.create( OPID_LOGGER_CFG_RESCAN_SECS, INTEGER, //
      TSID_NAME, "Rescan logger (sec)", //
      TSID_DESCRIPTION, "Each specified seconds logger read configuration to accept changes, 0 means no rescan.",
      TSID_DEFAULT_VALUE, avInt( DEFAULT_SHUTDOWN_TIMEOUT_SECS ) //
  );

  /**
   * If application shutdown after quit command timeouts specified number of seconds the {@link L2Main} will forcibly
   * terminate it.
   */
  IDataDef OPDEF_SHUTDOWN_TIMEOUT_SECS = DataDef.create( OPID_SHUTDOWN_TIMEOUT_SECS, INTEGER, //
      TSID_NAME, "Shutdown timeout (sec)", //
      TSID_DESCRIPTION,
      "If application shutdown after quit command timeouts specified number of seconds the {@link L2Main} will forcibly terminate it",
      TSID_DEFAULT_VALUE, avInt( DEFAULT_SHUTDOWN_TIMEOUT_SECS ) //
  );

  /**
   * Creates text console for user to communicate with running application.
   */
  IDataDef OPDEF_USE_TEXT_CONSOLE = DataDef.create( OPID_USE_TEXT_CONSOLE, BOOLEAN, //
      TSID_NAME, "Use console", //
      TSID_DESCRIPTION, "Creates text console for user to communicate with running application", //
      TSID_DEFAULT_VALUE, avBool( DEFAULT_USE_TEXT_CONSOLE ) //
  );

  /**
   * Full class name of the Ubackend provider to create the USkat connection.
   */
  IDataDef OPDEF_BACKEND_PROVIDER_CLASS = DataDef.create( OPID_BACKEND_PROVIDER_CLASS, STRING, //
      TSID_NAME, "Backend provider", //
      TSID_DESCRIPTION, "Full class name of the Ubackend provider to create the USkat connection", //
      TSID_DEFAULT_VALUE, avStr( DEFAULT_BACKEND_PROVIDER_CLASS ) //
  );

  /**
   * List of all options used by {@link L2Main} itself, not passing to {@link IL2Application}.
   * <p>
   * This list
   */
  IStridablesList<IDataDef> ALL_L2_MAIN_PARAMS = new StridablesList<>( //
      OPDEF_SHUTDOWN_TIMEOUT_SECS, //
      OPDEF_LOGGER_CFG_RESCAN_SECS //
  );

  // ------------------------------------------------------------------------------------
  // L2Main program exit code
  //

  /**
   * This exit code is never returned by {@link L2Main} program.
   * <p>
   * It is used internally to restart {@link L2Application} in {@link L2Main}.
   */
  int ECODE_RESTART_L2APP = -1;

  int ECODE_OK = 0;

  int ECODE_HELP_DISPLAYED = 1;

  int ECODE_CONN_OPEN_FAILED = 3;

  int ECODE_INIT_FAILED = 4;

  int ECODE_START_FAILED = 5;

}

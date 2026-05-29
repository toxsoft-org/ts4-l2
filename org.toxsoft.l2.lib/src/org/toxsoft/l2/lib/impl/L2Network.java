package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.IL2GlobalOptions.*;
import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.net.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
class L2Network
    extends L2AbstractComponent
    implements IL2Network, ICooperativeWorkerComponent, ISkCommandExecutor {

  private ISkConnection skConn;

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2SharedContext} - the L2 context
   */
  public L2Network( IL2SharedContext aL2Context ) {
    super( aL2Context, ALL_NET_COMP_OPDEFS );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doDoInit( ITsContextRo aArgs ) {
    // TODO load params from cfg file???
    IOptionSet netCfgs = cfgDir().readConfigs().values().first().cfg().fields();

    params().addAll( netCfgs );

    skConn = REFDEF_SK_CONNECTION.getRef( aArgs );
    ITsThreadExecutor executor = guardThread();

    executor.syncExec( () -> {
      // TODO - get special service and register itself as l2 bridge on server

      // register l2 as cmd executer (all commands of specified l2 object)
      String strid = OPDEF_NET_COMP_L2_SK_OBJ_STRID.getValue( params() ).asString();
      GwidList listOfSelfCmds = new GwidList(); // TODO - from l2 run settings
      Gwid cmdMainGwid = Gwid.createObj( IL2HardConstants.L2_SK_CLASS_ID, strid );
      listOfSelfCmds.add( cmdMainGwid );
      skConn.coreApi().cmdService().registerExecutor( L2Network.this, listOfSelfCmds );
    } );

    // TODO Auto-generated method stub

    return ValidationResult.SUCCESS;
  }

  // DEBUG ---
  private static final int  QUIT_SECS   = 3;
  private static final long SLEEP_MSECS = 50;

  int counter = 0;
  // ---

  @Override
  protected void doDoJob() {

    // TODO L2Network.doDoJob()

    // DEBUG ---
    ++counter;
    try {
      Thread.sleep( SLEEP_MSECS );
    }
    catch( InterruptedException ex ) {
      LoggerUtils.error( ex );
    }
    if( counter >= QUIT_SECS * 1000L / SLEEP_MSECS ) {
      counter = 0;
      L2AppCommand cmd =
          new L2AppCommand( EL2AppCmdCode.CODE_OK, "Test normal finish after " + QUIT_SECS + " seconds" ); //$NON-NLS-1$
      setQuitCommand( cmd );
    }
    // ---

  }

  @Override
  protected boolean doQueryStop() {

    // TODO L2Network.doQueryStop()

    return true;
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractComponent
  //

  // ------------------------------------------------------------------------------------
  // IL2Component
  //

  @Override
  final public EL2ComponentKind kind() {
    return EL2ComponentKind.NETWORK;
  }

  // ------------------------------------------------------------------------------------
  // IL2Network
  //

  @Override
  public ISkConnection getSkConnection() {
    return skConn;
  }

  // --------------------------------------------------------------------------------------
  // cmd executer
  @Override
  public void executeCommand( IDtoCommand aCmd ) {
    // TODO need synchronization?
    // TODO process different commands from server

    switch( aCmd.cmdGwid().propId() ) {
      case IL2HardConstants.L2_RESTART_CMD_ID: {
        L2AppCommand cmd = new L2AppCommand( EL2AppCmdCode.CODE_RESTART_L2APP, "Restart cmd from server" );
        setQuitCommand( cmd );
        return;
      }
      case IL2HardConstants.L2_STOP_CMD_ID: {
        L2AppCommand cmd = new L2AppCommand( EL2AppCmdCode.CODE_OK, "Stop cmd from server" );
        setQuitCommand( cmd );
        return;
      }
      case IL2HardConstants.L2_RELOAD_CMD_ID:

      default:
    }
  }

}

package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.app.IL2ApplicationConstants.*;
import static org.toxsoft.l2.main.IL2MainConstants.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.net.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
class L2Network
    extends L2AbstractComponent
    implements IL2Network, ICooperativeWorkerComponent {

  private ISkConnection skConn;

  /**
   * Constructor.
   *
   * @param aL2App {@link L2Application} - the L2 Application
   */
  public L2Network( L2Application aL2App ) {
    super( aL2App, IL2NetworkConstants.ALL_NETWORK_ARG_OPDEFS );
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doDoInit( ITsContextRo aArgs ) {
    skConn = REFDEF_SK_CONNECTION.getRef( aArgs );

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
      LoggerUtils.errorLogger().error( ex );
    }
    if( counter >= QUIT_SECS * 1000L / SLEEP_MSECS ) {
      counter = 0;
      L2AppQuitCommand cmd = new L2AppQuitCommand( ECODE_OK, "Test normal finish after " + QUIT_SECS + " seconds" ); //$NON-NLS-1$
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

}

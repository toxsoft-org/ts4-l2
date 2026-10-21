package org.toxsoft.l2.main.test;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.app.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.api.module.net.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * ML2 NET module.
 *
 * @author hazard157
 */
public class NetModuleQuitAfterTime
    extends L2AbstractNetModule {

  /**
   * Default value of the option {@link #OPDEF_WORK_TIME_SECS}.
   */
  public static final int DEFAULT_WORK_TIME_SECS = 10;

  /**
   * The factory ID.
   */
  public static final String FACTORY_ID = "NetModuleQuitAfterTime"; //$NON-NLS-1$

  /**
   * Module option: After this time interval module will initialize application quit sequence.
   */
  public static final IDataDef OPDEF_WORK_TIME_SECS = DataDef.create( FACTORY_ID + ".WorkTimeSecs", INTEGER, //$NON-NLS-1$
      TSID_DEFAULT_VALUE, avInt( 10 ) //
  );

  /**
   * The factory singleton.
   */
  public static final IL2ModuleFactory<L2AbstractNetModule> FACTORY =
      new L2AbstractModuleFactory<>( EL2ComponentKind.NETWORK, FACTORY_ID, "Quit after some time", //$NON-NLS-1$
          "Factory of NET module that quits application after the specified time" ) { //$NON-NLS-1$

        protected L2AbstractNetModule doDoCreate( L2AbstractComponent<L2AbstractModule> aOwner,
            L2ModuleConfig aConfig ) {
          return new NetModuleQuitAfterTime( aOwner, aConfig );
        }

      };

  private long workTimeMillsecs = 1000L * DEFAULT_WORK_TIME_SECS;
  private long startTime        = 0L;

  /**
   * Constructor.
   *
   * @param aOwner {@link L2AbstractComponent} - the owner component creating the module
   * @param aConfig {@link L2ModuleConfig} - the module configuration
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public NetModuleQuitAfterTime( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig ) {
    super( aOwner, aConfig );
  }

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    int wtSecs = OPDEF_WORK_TIME_SECS.getValue( aArgs.params() ).asInt();
    if( wtSecs < 1 ) {
      wtSecs = 1;
    }
    workTimeMillsecs = 1000L * wtSecs;
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doStart() {
    startTime = System.currentTimeMillis();
  }

  @Override
  protected void doDoJob() {
    long passesMsecs = l2Context().sweepStartTime() - startTime;
    if( passesMsecs > workTimeMillsecs ) {
      int secs = (int)workTimeMillsecs / 1000;
      String msg = String.format( "Auto-shutwon after working time %s", HmsUtils.autoHms( secs ) ); //$NON-NLS-1$
      L2AppCommand cmd = new L2AppCommand( EL2AppCmdCode.CODE_OK, msg );
      owner().setQuitCommand1( cmd );
    }
  }

  @Override
  protected boolean doQueryStop() {
    return true;
  }

  @Override
  protected boolean doStopping() {
    return true;
  }

  @Override
  protected void doDestroy() {
    // nop
  }

}

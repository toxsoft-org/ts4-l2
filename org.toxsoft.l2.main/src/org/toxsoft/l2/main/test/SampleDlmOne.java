package org.toxsoft.l2.main.test;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.api.module.dlm.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.l2.lib.impl.comps.*;

@SuppressWarnings( { "javadoc", "nls" } )
public class SampleDlmOne
    extends L2AbstractDlm {

  /**
   * The factory ID.
   */
  public static final String FACTORY_ID = "SampleDlmOne"; //$NON-NLS-1$

  /**
   * The factory singleton.
   */
  public static final IL2ModuleFactory<L2AbstractDlm> FACTORY =
      new L2AbstractModuleFactory<>( EL2ComponentKind.DLMMGR, FACTORY_ID, "Simple DLM", //$NON-NLS-1$
          "Simple DLM just sends messages to the console" ) { //$NON-NLS-1$

        protected L2AbstractDlm doDoCreate( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig ) {
          return new SampleDlmOne( aOwner, aConfig );
        }

      };

  public SampleDlmOne( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig ) {
    super( aOwner, aConfig );
    TsTestUtils.pl( "%s (%s) - SampleDlmOne()", id(), nmName() );
  }

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    TsTestUtils.pl( "%s (%s) - doInit()", id(), nmName() );
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doStart() {
    TsTestUtils.pl( "%s (%s) - doStart()", id(), nmName() );
  }

  @Override
  protected void doDoJob() {
    TsTestUtils.pl( "%s (%s) - doDoJob()", id(), nmName() );
    try {
      Thread.sleep( 100 );
    }
    catch( InterruptedException ex ) {
      LoggerUtils.error( ex );
    }
  }

  @Override
  protected boolean doQueryStop() {
    TsTestUtils.pl( "%s (%s) - doQueryStop()", id(), nmName() );
    return false;
  }

  @Override
  protected boolean doStopping() {
    TsTestUtils.pl( "%s (%s) - doStopping()", id(), nmName() );
    return true;
  }

  @Override
  protected void doDestroy() {
    TsTestUtils.pl( "%s (%s) - doDestroy()", id(), nmName() );
  }

}

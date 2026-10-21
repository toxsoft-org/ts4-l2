package org.toxsoft.l2.main.test;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.TsTestUtils.*;
import static org.toxsoft.l2.lib.api.app.IL2ApplicationArguments.*;
import static org.toxsoft.l2.main.IL2MainConstants.*;
import static org.toxsoft.l2.main.l10n.IL2MainSharedResources.*;

import java.lang.reflect.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.api.app.*;
import org.toxsoft.l2.lib.api.module.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.l2.lib.impl.comps.*;
import org.toxsoft.l2.main.*;
import org.toxsoft.uskat.backend.memtext.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.metainf.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Testing program for L2 development process.
 * <p>
 * This is a temporary file, may be removed from final build.
 *
 * @author hazard157
 */
@SuppressWarnings( { "javadoc", "nls" } )
public class L2Test {

  private static final String SK_TEXTUAL_FILE_NAME = "l2main-test.textual";

  private static final ILogger logger = LoggerUtils.getLogger( L2Main.class );

  public static void main( String[] aArgs ) {
    pl( "L2 testing program." );
    // prepare
    L2ModuleFactoriesRepositorySimple repo = new L2ModuleFactoriesRepositorySimple();
    repo.addFactory( NetModuleQuitAfterTime.FACTORY );
    repo.addFactory( SampleDlmOne.FACTORY );
    L2ModuleConfigManagerSImple cfgMan = new L2ModuleConfigManagerSImple();
    L2ModuleConfig cfg1 =
        new L2ModuleConfig( "simple", NetModuleQuitAfterTime.FACTORY_ID, "Name1", "Description1", IAvTree.NULL );
    cfgMan.addModuleConfig( EL2ComponentKind.NETWORK, cfg1 );
    L2ModuleConfig cfg2 =
        new L2ModuleConfig( "simpleDlm1", SampleDlmOne.FACTORY_ID, "DLM 1", "Description1", IAvTree.NULL );
    L2ModuleConfig cfg3 =
        new L2ModuleConfig( "simpleDlm2", SampleDlmOne.FACTORY_ID, "DLM 2", "Description2", IAvTree.NULL );
    cfgMan.addModuleConfig( EL2ComponentKind.DLMMGR, cfg2 );
    cfgMan.addModuleConfig( EL2ComponentKind.DLMMGR, cfg3 );
    TsThreadExecutor mainThreadGuard =
        new TsThreadExecutor( L2Test.class.getSimpleName(), Thread.currentThread(), logger );
    ISkConnection skConn;
    try {
      IOptionSetEdit skArgs = new OptionSet();
      skArgs.setStr( MtbBackendToFile.OPDEF_FILE_PATH, SK_TEXTUAL_FILE_NAME );
      skConn = openConnection( skArgs, mainThreadGuard );
    }
    catch( Exception ex ) {
      pl( ex.getLocalizedMessage() );
      System.exit( 1 );
      return; // just for compiler
    }
    // initialize application
    pl( "Inititlizing L2 application..." );
    IL2Application l2App = new L2Application( "TestApp" );
    ITsContext l2Args = new TsContext();
    REFDEF_L2_MAIN_THREAD_GUARD.setRef( l2Args, mainThreadGuard );
    REFDEF_L2_MODULE_CONFIG_MANAGER.setRef( l2Args, cfgMan );
    REFDEF_L2_MODULE_FACTORIES_REPOSITORY.setRef( l2Args, repo );
    REFDEF_SK_CONNECTION.setRef( l2Args, skConn );
    OPDEF_FAIL_APP_ON_MODULE_START_FAIL.setValue( l2Args.params(), AV_TRUE );
    ValidationResult vr = l2App.init( l2Args );
    switch( vr.type() ) {
      case OK: {
        break;
      }
      case WARNING: {
        pl( "WARNING: %s", vr.message() );
        break;
      }
      case ERROR: {
        pl( "ERROR: %s", vr.message() );
        System.exit( 1 );
        return; // just for compiler
      }
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // run application
    pl( "Starting L2 application..." );
    l2App.start();
    pl( "L2 application running..." );
    L2AppCommand quitCmd = null;
    while( quitCmd == null ) {
      l2App.doJob();
      quitCmd = l2App.getQuitCommandIfAny();
    }
    pl( "Program finished." );
  }

  private static ISkConnection openConnection( IOptionSet aGlobalOps, TsThreadExecutor aMainThreadGuard ) {
    ITsContext connArgs = new TsContext();
    connArgs.params().addAll( aGlobalOps );
    // get backend provider
    ISkBackendProvider backendProvider = getSkBackendProvider( aGlobalOps );
    ISkCoreConfigConstants.REFDEF_BACKEND_PROVIDER.setRef( connArgs, backendProvider );
    ISkBackendMetaInfo metaInfo = backendProvider.getMetaInfo();
    // separate backend and L2 main threads
    ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.setRef( connArgs, aMainThreadGuard );
    // validate arguments
    TsValidationFailedRtException.checkError( metaInfo.checkArguments( connArgs ) );
    // open the connection
    ISkConnection skConn = SkCoreUtils.createConnection();
    skConn.open( connArgs );
    return skConn;
  }

  private static ISkBackendProvider getSkBackendProvider( IOptionSet aGlobalOps ) {
    String className = OPDEF_BACKEND_PROVIDER_CLASS.getValue( aGlobalOps ).asString();
    try {
      Class<?> rawClass = Class.forName( className );
      if( !ISkBackendProvider.class.isAssignableFrom( rawClass ) ) {
        throw new TsIoRtException( FMT_ERR_INV_BACKEND_CLASS, className );
      }
      @SuppressWarnings( "unchecked" )
      Class<ISkBackendProvider> clazz = (Class<ISkBackendProvider>)rawClass;
      Constructor<ISkBackendProvider> constructor = clazz.getConstructor();
      return constructor.newInstance();
    }
    catch( TsRuntimeException ex ) {
      throw ex;
    }
    catch( Exception ex ) {
      throw new TsIoRtException( ex );
    }
  }

}

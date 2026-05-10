package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.IL2GlobalOptions.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.dlms.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
class L2DlmManager
    extends L2AbstractComponent
    implements IL2DlmManager, ICooperativeWorkerComponent {

  private final IStridablesListEdit<L2AbstractDlm> initedDlmsList  = new StridablesList<>();
  private final IStridablesListEdit<L2AbstractDlm> startedDlmsList = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2SharedContext} - the L2 context
   */
  public L2DlmManager( IL2SharedContext aL2Context ) {
    super( aL2Context, ALL_DLMMGR_COMP_OPDEFS );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void checkDlmFileChangesAndReloadIfNeeded() {

    // TODO check if enough time passed to invoke the check

    // TODO scan for changes in files in configuration directory

    // TODO scan for changes in DLM JAR files directory

  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doDoInit( ITsContextRo aArgs ) {
    initedDlmsList.clear();
    startedDlmsList.clear();
    IStridablesListEdit<L2AbstractDlm> llLoadedDlms = new StridablesList<>();

    // TODO load configurations from the files in configuration directory
    // TODO load needed DLM factories from the DLM JAR files directory
    // TODO create requested DLMs

    // initialize created DLMs
    ValidationResult vr = ValidationResult.SUCCESS;
    for( L2AbstractDlm d : llLoadedDlms ) {
      ValidationResult vrDlm = d.init( aArgs );
      switch( vrDlm.type() ) {
        case OK: { // DLM init OK - add to #initedDlmsList
          initedDlmsList.add( d );
          break;
        }
        case WARNING: { // DLM init OK - add to #initedDlmsList and remember the result
          initedDlmsList.add( d );
          vr = ValidationResult.firstNonOk( vr, vrDlm );
          break;
        }
        case ERROR: { // DLM init FAIL - ignore this DLM and remember the result
          vr = ValidationResult.warn( FMT_WARN_IGNORED_NOT_INITED_DLM, d.id(), vrDlm.message() );
          break;
        }
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    return vr;
  }

  @Override
  protected void doStart() {
    for( L2AbstractDlm d : initedDlmsList ) {
      try {
        d.start();
        startedDlmsList.add( d );
      }
      catch( Exception ex ) {
        logger().error( ex, FMT_WARN_IGNORED_NOT_STARTED_DLM, d.id(), ex.getMessage() );
      }
    }
  }

  @Override
  protected void doDoJob() {
    checkDlmFileChangesAndReloadIfNeeded();
    // iterate over existing DLMs and call doJob()
    for( int i = 0, c = startedDlmsList.size(); i < c; i++ ) { // index iteration to avoid any memory allocation
      L2AbstractDlm d = startedDlmsList.get( i );
      try {
        d.doJob();
      }
      catch( Exception ex ) {
        logger().error( ex );
      }
    }
  }

  @Override
  protected boolean doQueryStop() {
    boolean stopped = true;
    for( L2AbstractDlm d : initedDlmsList ) {
      boolean b = d.queryStop();
      stopped = stopped && b;
    }
    return stopped;
  }

  @Override
  protected void doDestroy() {
    while( !initedDlmsList.isEmpty() ) {
      L2AbstractDlm d = initedDlmsList.removeByIndex( 0 );
      d.destroy();
    }
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractComponent
  //

  // ------------------------------------------------------------------------------------
  // IL2Component
  //

  @Override
  final public EL2ComponentKind kind() {
    return EL2ComponentKind.DLMMGR;
  }

  // ------------------------------------------------------------------------------------
  // IL2DlmManager
  //

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IStridablesList<IStridable> dlms() {
    return switch( compState() ) {
      case CREATED, DESTROYED -> IStridablesList.EMPTY;
      case INITIALIZED -> (IStridablesList)initedDlmsList;
      case WORKING, STOPPING -> (IStridablesList)startedDlmsList;
      default -> throw new TsNotAllEnumsUsedRtException();
    };
  }

}

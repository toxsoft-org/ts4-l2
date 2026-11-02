package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.api.app.IL2ApplicationArguments.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.module.hal.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
public final class L2Hal
    extends L2AbstractComponent<L2AbstractHalDriver>
    implements IL2Hal, ICooperativeWorkerComponent {

  // TODO add stopping timeout - forcibly kill drivers after timeout

  private final IStridablesListEdit<L2Tag> tagsList = new StridablesList<>( 1000 );

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2SharedContext} - the L2 context
   */
  public L2Hal( IL2SharedContext aL2Context ) {
    super( aL2Context, ALL_HAL_COMP_OPDEFS );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private IStridablesList<L2AbstractHalDriver> driversList() {
    return super.modules();
  }

  // ------------------------------------------------------------------------------------
  // IL2Component
  //

  @Override
  public EL2ComponentKind kind() {
    return EL2ComponentKind.HAL;
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractComponent
  //

  @Override
  protected ValidationResult doL2Init( ITsContextRo aArgs ) {
    // check for duplicate IDs
    for( L2AbstractHalDriver d1 : driversList() ) {
      for( L2AbstractHalDriver d2 : driversList() ) {
        if( d1 != d2 ) {
          for( String id : d1.tags().keys() ) {
            if( d2.tags().hasKey( id ) ) {
              throw new TsItemAlreadyExistsRtException( FMT_ERR_DUP_TAG_ID, id, d1.id(), d2.id() );
            }
          }
        }
      }
    }
    // fill #tagsList with tags provided by the modules and check for duplicate IDs
    for( L2AbstractHalDriver d1 : driversList() ) {
      tagsList.addAll( d1.tags() );
    }
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doL2DestroyAfterModules() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IL2Hal
  //

  @Override
  public IStridablesList<IL2HalDriver> drivers() {
    return super.modules();
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesList<IL2Tag> tags() {
    return (IStridablesList)tagsList;
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesList<IL2Tag> listDeviceTags( String aDeviceId ) {
    return (IStridablesList)drivers().getByKey( aDeviceId ).tags();
  }

}

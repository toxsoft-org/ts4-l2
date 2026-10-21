package org.toxsoft.l2.lib.api.module;

import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.module.dlm.*;
import org.toxsoft.l2.lib.api.module.hal.*;
import org.toxsoft.l2.lib.api.module.net.*;
import org.toxsoft.l2.lib.impl.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * {@link IL2ModuleFactory} implementation base.
 *
 * @author hazard157
 * @param <T> - concrete type of the created modules
 */
public non-sealed abstract class L2AbstractModuleFactory<T extends L2AbstractModule>
    extends Stridable
    implements IL2ModuleFactory<T> {

  private final EL2ComponentKind                  componentKind;
  private final Class<? extends L2AbstractModule> moduleClass;

  /**
   * Constructor.
   *
   * @param aCompKind {@link EL2ComponentKind} - the component kind
   * @param aId String - factory ID (an IDpath)
   * @param aName String - factory short name
   * @param aDescription String - factory description
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public L2AbstractModuleFactory( EL2ComponentKind aCompKind, String aId, String aName, String aDescription ) {
    super( aId, aName, aDescription, true );
    TsNullArgumentRtException.checkNull( aCompKind );
    componentKind = aCompKind;
    moduleClass = switch( componentKind ) {
      case DLMMGR -> L2AbstractDlm.class;
      case HAL -> L2AbstractHalDriver.class;
      case NETWORK -> L2AbstractNetModule.class;
      default -> throw new TsNotAllEnumsUsedRtException();
    };
  }

  // ------------------------------------------------------------------------------------
  // IL2ModuleFactory
  //

  @Override
  final public EL2ComponentKind componentKind() {
    return componentKind;
  }

  @Override
  final public T create( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig ) {
    TsNullArgumentRtException.checkNulls( aOwner, aConfig );
    T moduleInstance = doDoCreate( aOwner, aConfig );
    TsIllegalStateRtException.checkFalse( moduleClass.isInstance( moduleInstance ), FMT_ERR_INV_CREATED_MODULE_CLASS,
        id(), moduleInstance.getClass().getCanonicalName(), moduleClass.getCanonicalName() );
    return moduleInstance;
  }

  // ------------------------------------------------------------------------------------
  // To implement/override
  //

  protected abstract T doDoCreate( L2AbstractComponent<L2AbstractModule> aOwner, L2ModuleConfig aConfig );

}

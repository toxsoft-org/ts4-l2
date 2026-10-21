package org.toxsoft.l2.lib.api.module;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * Simple implementation of the {@link IL2ModuleConfigManager} holding previously added configurations.
 * <p>
 * Allows to add MCFs instances via {@link #addModuleConfig(EL2ComponentKind, L2ModuleConfig)}.
 */
public class L2ModuleConfigManagerSImple
    implements IL2ModuleConfigManager {

  private final ITsEventer<IL2ModuleConfigManagerListener> eventer = new AbstractTsEventer<>() {

    @Override
    protected boolean doIsPendingEvents() {
      return false;
    }

    @Override
    protected void doFirePendingEvents() {
      // nop
    }

    @Override
    protected void doClearPendingEvents() {
      // nop
    }
  };

  private final IMapEdit<EL2ComponentKind, IStridablesListEdit<L2ModuleConfig>> cfgMap = new ElemMap<>();

  /**
   * Constructor.
   */
  public L2ModuleConfigManagerSImple() {
    for( EL2ComponentKind k : EL2ComponentKind.asList() ) {
      cfgMap.put( k, new StridablesList<>() );
    }
  }

  // ------------------------------------------------------------------------------------
  // IL2ModuleConfigManager
  //

  @Override
  public IStridablesList<L2ModuleConfig> listModuleConfigs( EL2ComponentKind aCompKind ) {
    return cfgMap.getByKey( aCompKind );
  }

  @Override
  public ITsEventer<IL2ModuleConfigManagerListener> eventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // addModuleConfig
  //

  /**
   * Adds configuration.
   *
   * @param aCompKind {@link EL2ComponentKind} - kind of module's owner component
   * @param aConfig {@link L2ModuleConfig} - configuration to add
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException configuration with the same ID is already added
   */
  public void addModuleConfig( EL2ComponentKind aCompKind, L2ModuleConfig aConfig ) {
    TsNullArgumentRtException.checkNulls( aCompKind, aConfig );
    IStridablesListEdit<L2ModuleConfig> llCfgs = cfgMap.getByKey( aCompKind );
    TsItemAlreadyExistsRtException.checkTrue( llCfgs.hasKey( aCompKind.id() ) );
    llCfgs.add( aConfig );
  }

}

package org.toxsoft.l2.lib.api.module;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.impl.comps.*;

/**
 * Simple implementation of the {@link IL2ModuleFactoriesRepository} holding previously added factories.
 * <p>
 * Allows to add factories instances via {@link #addFactory(IL2ModuleFactory)}.
 *
 * @author hazard157
 */
public class L2ModuleFactoriesRepositorySimple
    implements IL2ModuleFactoriesRepository {

  private final IMapEdit<EL2ComponentKind, IStridablesListBasicEdit<L2AbstractModuleFactory<L2AbstractModule>>> factoriesMap =
      new ElemMap<>();

  /**
   * Constructor.
   */
  public L2ModuleFactoriesRepositorySimple() {
    for( EL2ComponentKind k : EL2ComponentKind.asList() ) {
      factoriesMap.put( k, new SortedStridablesList<>() );
    }
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public <T extends L2AbstractModule> IStridablesList<IL2ModuleFactory<T>> listFactories( EL2ComponentKind aCompKind ) {
    return (IStridablesList)factoriesMap.getByKey( aCompKind );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Adds factory to the repository.
   *
   * @param aFactory {@link L2AbstractModuleFactory} - the factory to add
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException factory with the same ID is already registered
   */
  @SuppressWarnings( { "rawtypes", "unchecked" } )
  public void addFactory( IL2ModuleFactory<? extends L2AbstractModule> aFactory ) {
    TsNullArgumentRtException.checkNull( aFactory );
    IStridablesListBasicEdit<L2AbstractModuleFactory<L2AbstractModule>> llFacs =
        factoriesMap.getByKey( aFactory.componentKind() );
    TsItemAlreadyExistsRtException.checkTrue( llFacs.hasKey( aFactory.id() ) );
    llFacs.add( (L2AbstractModuleFactory)aFactory );
  }

}

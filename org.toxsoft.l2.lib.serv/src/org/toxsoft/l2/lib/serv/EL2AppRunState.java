package org.toxsoft.l2.lib.serv;

import static org.toxsoft.l2.lib.serv.l10n.ISkL2AppServiceSharedResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The enumeration of XXX.
 *
 * @author AUTHOR_NAME
 */
@SuppressWarnings( "javadoc" )
public enum EL2AppRunState
    implements IStridable {

  ANY( "any", STR_L2APP_STATE_ANY, STR_L2APP_STATE_ANY_D ), //$NON-NLS-1$

  OFF( "off", STR_L2APP_STATE_OFF, STR_L2APP_STATE_OFF_D ), //$NON-NLS-1$

  ON( "on", STR_L2APP_STATE_ON, STR_L2APP_STATE_ON_D ); //$NON-NLS-1$

  /**
   * The registered keeper ID.
   */
  public static final String KEEPER_ID = "EL2AppRunState"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<EL2AppRunState> KEEPER = new StridableEnumKeeper<>( EL2AppRunState.class );

  private static IStridablesListEdit<EL2AppRunState> list = null;

  private final String id;
  private final String name;
  private final String description;

  EL2AppRunState( String aId, String aName, String aDescription ) {
    id = aId;
    name = aName;
    description = aDescription;
  }

  // --------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return name;
  }

  @Override
  public String description() {
    return description;
  }

  // ----------------------------------------------------------------------------------
  // Stridable enum common API
  //

  /**
   * Returns all constants in single list.
   *
   * @return {@link IStridablesList}&lt; {@link EL2AppRunState} &gt; - list of constants in order of declaraion
   */
  public static IStridablesList<EL2AppRunState> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Returns the constant by the ID.
   *
   * @param aId String - the ID
   * @return {@link EL2AppRunState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified ID
   */
  public static EL2AppRunState getById( String aId ) {
    return asList().getByKey( aId );
  }

  /**
   * Finds the constant by the name.
   *
   * @param aName String - the name
   * @return {@link EL2AppRunState} - found constant or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static EL2AppRunState findByName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EL2AppRunState item : values() ) {
      if( item.name.equals( aName ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Returns the constant by the name.
   *
   * @param aName String - the name
   * @return {@link EL2AppRunState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified name
   */
  public static EL2AppRunState getByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByName( aName ) );
  }

}

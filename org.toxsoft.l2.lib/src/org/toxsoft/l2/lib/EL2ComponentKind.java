package org.toxsoft.l2.lib;

import static org.toxsoft.l2.lib.IL2HardConstants.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.app.*;

/**
 * Kinds of {@link IL2Component}.
 * <p>
 * Note: IDs of the constants are the persistent hard constants and must nt be changed.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public enum EL2ComponentKind
    implements IStridable {

  HAL( "hal", STR_L2CK_HAL, STR_L2CK_HAL_D ), //$NON-NLS-1$

  DLMMGR( "dlm", STR_L2CK_DLMMGR, STR_L2CK_DLMMGR_D ), //$NON-NLS-1$

  NETWORK( "net", STR_L2CK_NETWORK, STR_L2CK_NETWORK_D ); //$NON-NLS-1$

  /**
   * The registered keeper ID.
   */
  public static final String KEEPER_ID = "EL2ComponentKind"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<EL2ComponentKind> KEEPER = new StridableEnumKeeper<>( EL2ComponentKind.class );

  private static IStridablesListEdit<EL2ComponentKind> list = null;

  private final String id;
  private final String name;
  private final String description;

  EL2ComponentKind( String aId, String aName, String aDescription ) {
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

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns global option ID specifying the component config files subdirectory in the configuration files root.
   * <p>
   * Default value of the configuration subdirectory name is the same as this constant ID {@link #id()}.
   * <p>
   * Configuration files root directory is determined by the global option
   * {@link IL2ApplicationConstants#OPDEF_L2_COMP_CFG_DIR_ROOT}.
   *
   * @return String - global option ID containing component configuration files subdirectory
   */
  public String getCfgSubirOptionId() {
    return L2_ID + '.' + id() + ".CfgSubdir"; //$NON-NLS-1$
  }

  // ----------------------------------------------------------------------------------
  // Stridable enum common API
  //

  /**
   * Returns all constants in single list.
   *
   * @return {@link IStridablesList}&lt; {@link EL2ComponentKind} &gt; - list of constants in order of declaraion
   */
  public static IStridablesList<EL2ComponentKind> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Returns the constant by the ID.
   *
   * @param aId String - the ID
   * @return {@link EL2ComponentKind} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified ID
   */
  public static EL2ComponentKind getById( String aId ) {
    return asList().getByKey( aId );
  }

  /**
   * Finds the constant by the name.
   *
   * @param aName String - the name
   * @return {@link EL2ComponentKind} - found constant or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static EL2ComponentKind findByName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EL2ComponentKind item : values() ) {
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
   * @return {@link EL2ComponentKind} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified name
   */
  public static EL2ComponentKind getByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByName( aName ) );
  }

}

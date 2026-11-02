package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The L2 application main loop step.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public enum EL2SweepStep
    implements IStridable {

  IO_READ( "io_read", STR_L2SWST_IO_READ, STR_L2SWST_IO_READ_D ), //$NON-NLS-1$

  DLM_RUN( "dlm_run", STR_L2SWST_DLM_RUN, STR_L2SWST_DLM_RUN_D ), //$NON-NLS-1$

  IO_WRITE( "io_write", STR_L2SWST_IO_WRITE, STR_L2SWST_IO_WRITE_D ), //$NON-NLS-1$

  NET_COMM( "net_comm", STR_L2SWST_NET_COMM, STR_L2SWST_NET_COMM_D ); //$NON-NLS-1$

  /**
   * The registered valobj ID.
   */
  public static final String KEEPER_ID = "EL2SweepStep"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<EL2SweepStep> KEEPER = new StridableEnumKeeper<>( EL2SweepStep.class );

  private static IStridablesListEdit<EL2SweepStep> list = null;

  private final String id;
  private final String name;
  private final String description;

  EL2SweepStep( String aId, String aName, String aDescription ) {
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
   * @return {@link IStridablesList}&lt; {@link EL2SweepStep} &gt; - list of constants in order of declaraion
   */
  public static IStridablesList<EL2SweepStep> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Returns the constant by the ID.
   *
   * @param aId String - the ID
   * @return {@link EL2SweepStep} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified ID
   */
  public static EL2SweepStep getById( String aId ) {
    return asList().getByKey( aId );
  }

  /**
   * Finds the constant by the name.
   *
   * @param aName String - the name
   * @return {@link EL2SweepStep} - found constant or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static EL2SweepStep findByName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EL2SweepStep item : values() ) {
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
   * @return {@link EL2SweepStep} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified name
   */
  public static EL2SweepStep getByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByName( aName ) );
  }

}

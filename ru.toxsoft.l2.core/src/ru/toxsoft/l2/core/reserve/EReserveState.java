package ru.toxsoft.l2.core.reserve;

/**
 * Состояние резервирования шкафа
 * 
 * @author max
 */
public enum EReserveState {

  MAIN( "Main", 3 ),
  MAIN_INVALID( "MainInvalid", 4 ),
  RESERVE( "Reserve", 1 ),
  RESERVE_AUTO( "ReserveAuto", 2 ),
  UNKNOWN( "Unknown", 0 );

  private String id;

  private int dbId;

  private EReserveState( String aId, int aBdId ) {
    this.id = aId;
    dbId = aBdId;
  }

  public int dbId() {
    return dbId;
  }

  public String id() {
    return id;
  }
}

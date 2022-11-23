package ru.toxsoft.l2.core.reserve;

/**
 * Значение состояния шкафа партнёра.
 * 
 * @author max
 */
public enum EPartnerBoxReserveState {

  MAIN( "psMain", 1 ),
  RESERVE( "psReserve", 2 ),
  UNKNOWN( "psUndef", 3 );

  private String id;

  private int dbId;

  private EPartnerBoxReserveState( String aId, int aDbId ) {
    this.id = aId;
    dbId = aDbId;
  }

  public String id() {
    return id;
  }

  public int dbId() {
    return dbId;
  }

  public static EPartnerBoxReserveState findById( String aId ) {
    for( EPartnerBoxReserveState state : values() ) {
      if( state.id.equals( aId ) ) {
        return state;
      }
    }
    return EPartnerBoxReserveState.UNKNOWN;
  }
}

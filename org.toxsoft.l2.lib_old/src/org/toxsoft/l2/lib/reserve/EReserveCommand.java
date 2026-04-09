package org.toxsoft.l2.lib.reserve;

import static org.toxsoft.l2.lib.IL2CoreSysdescrConstants.*;

/**
 * Перечисление возможных команд резервирования, поступающих с сервера
 *
 * @author max
 */
public enum EReserveCommand {
  // Все параметры сущностей сервера из файла, сгенерированного skide

  /**
   * Команда - стать главным
   */
  CDM_MAIN( CMDID_CLSID_L2_HOTSWAP_CMDMAIN ),

  /**
   * Команда - стать главным несмотря на недостатки
   */
  CMD_MAIN_INVALID( CMDID_CLSID_L2_HOTSWAP_CMDMAININVALID ),

  /**
   * Команда - стать резервным
   */
  CMD_RESERVE( CMDID_CLSID_L2_HOTSWAP_CMDRESERVE ),

  /**
   * Неизвестная команда
   */
  UNKNOWN( "cmdUnknown" ); //$NON-NLS-1$

  private String id;

  EReserveCommand( String aId ) {
    this.id = aId;
  }

  /**
   * Возвращает идентификатор команды.
   *
   * @return String - идентификатор команды.
   */
  public String id() {
    return id;
  }

  /**
   * Проводит поиск и возвращает команду по идентификатору.
   *
   * @param aId String - идентификатор команды.
   * @return EReserveCommand - найденная команда или {@link EReserveCommand#UNKNOWN} если нет команды с таким
   *         идентификатором.
   */
  public static EReserveCommand findById( String aId ) {
    for( EReserveCommand cmd : values() ) {
      if( cmd.id.equals( aId ) ) {
        return cmd;
      }
    }
    return EReserveCommand.UNKNOWN;
  }
}

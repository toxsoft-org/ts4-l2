package ru.toxsoft.l2.thd.opc;

import static ru.toxsoft.l2.thd.opc.IL2Resources.*;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Состояние здоровья тега.
 *
 * @author Dima
 */
@SuppressWarnings( "nls" )
public enum ETagHealth {

  /**
   * unknown state of health (just created)
   */
  UNKNOWN( 0, "unknown", E_HEALTH_UNKNOWN ),

  /**
   * tag is working
   */
  WORKING( 1, "working", E_HEALTH_OK ),

  /**
   * tag is just broken
   */
  JUST_BROKEN( 2, "just broken", E_JUST_BROKEN ),

  /**
   * tag is just recovered
   */
  JUST_RECOVERED( 3, "just recovered", E_JUST_RECOVERED ),

  /**
   * tag is dead
   */
  DEAD( 4, "dead", E_DEAD );

  private int id;

  private String name;

  /**
   * отображаемое описание..
   */
  private String description;

  /**
   * Конструктор состояния.
   *
   * @param aId byte - идентфикатор состояния (из протокола)
   * @param aName - название состояния.
   * @param aDescription - описание состояния команды.
   */
  ETagHealth( int aId, String aName, String aDescription ) {
    id = aId;
    name = aName;
    description = aDescription;
  }

  /**
   * Возвращает идентификатор состояния.
   *
   * @return int байтовый идентфикатор состояния.
   */
  public int getId() {
    return id;
  }

  /**
   * Возвращает название состояния
   *
   * @return String - название состояния.
   */
  public String getName() {
    return name;
  }

  /**
   * Возвращает описание состояния команды (отображаемое имя).
   *
   * @return String - описание состояния команды (отображаемое имя).
   */
  public String getDescription() {
    return description;
  }

  /**
   * Возвращает состояние по идентификатору или null.
   *
   * @param aId int - идентификатор
   * @return EKind - найденный тип, или null если нет типа с таким идентификатором
   */
  public static ETagHealth findByIdOrNull( int aId ) {

    for( ETagHealth item : values() ) {
      if( item.id == aId ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Возвращает тип тега по идентификатору или выбрасывает исключение.
   *
   * @param aId int - идентификатор
   * @return {@link ETagHealth} - найденный тип
   * @throws TsItemNotFoundRtException нет типа с таким идентификатором
   */
  public static ETagHealth findById( int aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

}

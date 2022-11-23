package ru.toxsoft.l2.thd.opc;

import static ru.toxsoft.l2.thd.opc.IL2Resources.*;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Перечисление типов тегов по чтению/записи.
 *
 * @author Dima
 */
@SuppressWarnings( "nls" )
public enum EKind {

  /**
   * Тег только на чтение
   */
  R( 0, "R", E_READ_ONLY ),

  /**
   * Тег только на запись
   */
  W( 0, "W", E_WRITE_ONLY ),

  /**
   * Тег на чтение и запись
   */
  RW( 0, "RW", E_READ_WRITE );

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
  EKind( int aId, String aName, String aDescription ) {
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
  public static EKind findByIdOrNull( int aId ) {

    for( EKind item : values() ) {
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
   * @return {@link EKind} - найденный тип
   * @throws TsItemNotFoundRtException нет типа с таким идентификатором
   */
  public static EKind findById( int aId ) {
    return TsItemNotFoundRtException.checkNull( findByIdOrNull( aId ) );
  }

}

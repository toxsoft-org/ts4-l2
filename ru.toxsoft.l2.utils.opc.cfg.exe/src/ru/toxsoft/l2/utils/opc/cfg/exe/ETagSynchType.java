package ru.toxsoft.l2.utils.opc.cfg.exe;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Тип тега - синхронный, асинхронный
 *
 * @author max
 */
public enum ETagSynchType {

  SYNCH( "sync" ),
  ASYNCH( "async" )

  ;

  private String name;

  ETagSynchType( String aName ) {
    name = aName;
  }

  public String getName() {
    return name;
  }

  /**
   * Возвращает элемент перечисления по его имени
   *
   * @return ETagType - элемент перечисления.
   * @throws TsItemNotFoundRtException - если нет элемента с таким именем.
   */
  public static ETagSynchType searchTypeByName( String aName ) {
    for( ETagSynchType type : values() ) {
      if( type.getName().equals( aName ) ) {
        return type;
      }
    }
    throw new TsItemNotFoundRtException( "ETagType doesnt contain element with name %s", aName );
  }
}

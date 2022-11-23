package ru.toxsoft.l2.utils.opc.cfg.exe;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Тип значения тега - boolean, float, int и т.д.
 *
 * @author max
 */
public enum ETagValueType {

  BOOLEAN( "Boolean" ),
  FLOAT( "Float" ),
  INTEGER( "Integer" ),
  STRING( "String" ),

  ;

  private String name;

  ETagValueType( String aName ) {
    name = aName;
  }

  public String getName() {
    return name;
  }

  /**
   * Возвращает элемент перечисления по его имени
   *
   * @return ETagValueType - элемент перечисления.
   * @throws TsItemNotFoundRtException - если нет элемента с таким именем.
   */
  public static ETagValueType searchTypeByName( String aName ) {
    for( ETagValueType type : values() ) {
      if( aName.contains( type.getName() ) ) {
        // if( type.getName().equals( aName ) ) {
        return type;
      }
    }
    throw new TsItemNotFoundRtException( "ETagValueType doesnt contain element with name %s", aName );
  }

}

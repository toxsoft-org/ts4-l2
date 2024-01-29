package ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Types of complex tags realizations.
 *
 * @author max
 */
public enum EComplexTagType {

  SIMPLE_COMPLEX_TAG( "node.with.address.params.feedback",
      "Tag with address tag, feedback tag, params tags with different types", ComplexTagImpl.class );

  String typeId;

  String typeDescripyion;

  Class<? extends AbstractComplexTag> typeClass;

  EComplexTagType( String aTypeId, String aTypeDescripyion, Class<? extends AbstractComplexTag> aTypeClass ) {
    typeId = aTypeId;
    typeDescripyion = aTypeDescripyion;
    typeClass = aTypeClass;
  }

  /**
   * Returns id of complex tag realization.
   *
   * @return String - id of complex tag realization.
   */
  public String getTypeId() {
    return typeId;
  }

  /**
   * Returns description of complex tag realization.
   *
   * @return String - description of complex tag realization.
   */
  public String getTypeDescription() {
    return typeDescripyion;
  }

  /**
   * Returns class of complex tag realization.
   *
   * @return Class - class of complex tag realization.
   */
  public Class<? extends AbstractComplexTag> getTypeClass() {
    return typeClass;
  }

  /**
   * Create tag instance of specified type.
   *
   * @param aTypeId String - tag type id - one of from this enum or full class name (if aTypeStrCanBeClass attribue -
   *          true)
   * @param aTagId String - tag id
   * @param aTypeStrCanBeClass true - aTypeId param can be interpreted as full class name
   * @return AbstractComplexTag - created Tag
   * @throws Exception - if error during creation (class not found and so on)
   */
  @SuppressWarnings( "unchecked" )
  static AbstractComplexTag createComplexTag( String aTypeId, String aTagId, boolean aTypeStrCanBeClass )
      throws Exception {
    EComplexTagType type = null;
    for( EComplexTagType t : EComplexTagType.values() ) {
      if( t.getTypeId().equals( aTypeId ) ) {
        type = t;
        break;
      }
    }

    TsItemNotFoundRtException.checkTrue( type == null && !aTypeStrCanBeClass );

    Class<? extends AbstractComplexTag> tClass =
        type != null ? type.getTypeClass() : (Class<? extends AbstractComplexTag>)Class.forName( aTypeId );

    return tClass.getConstructor( String.class ).newInstance( aTagId );
  }
}

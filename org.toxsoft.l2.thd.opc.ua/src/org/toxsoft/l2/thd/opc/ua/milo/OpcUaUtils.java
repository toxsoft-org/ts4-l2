package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;

/**
 * Утилитный класс для работы с OPC
 *
 * @author max
 */
public class OpcUaUtils {

  /**
   * Закрытый конструктор
   */
  private OpcUaUtils() {
  }

  public static IAtomicValue convertFromOpc( Variant aValue, EAtomicType aTagType ) {
    if( aValue.isNull() ) {
      return IAtomicValue.NULL;
    }
    return AvUtils.avFromObj( aValue.getValue() );

  }

  public static Variant convertToOpc( IAtomicValue aValue, EAtomicType aTagType ) {
    Variant result = switch( aTagType ) {
      case BOOLEAN -> new Variant( Boolean.valueOf( aValue.asBool() ) );
      case FLOATING -> new Variant( Double.valueOf( aValue.asDouble() ) );
      case INTEGER -> new Variant( Integer.valueOf( aValue.asInt() ) );
      case NONE -> Variant.NULL_VALUE;
      case STRING -> new Variant( aValue.asString() );
      case TIMESTAMP -> new Variant( Long.valueOf( aValue.asLong() ) );
      case VALOBJ -> new Variant( aValue.asValobj() );
      default -> new Variant( aValue.asString() );
    };
    return result;
  }

  /**
   * По дереву конфигурации группы создает список пинов
   *
   * @param aTagsGroupConfig дерево конфигурации
   * @return список описания пинов
   */
  public static IList<TagCfgItem> createTagsCfgItems( IAvTree aTagsGroupConfig ) {

    IListEdit<TagCfgItem> tagsCfgItems = new ElemArrayList<>();
    for( int i = 0; i < aTagsGroupConfig.arrayLength(); i++ ) {
      IAvTree tagConfig = aTagsGroupConfig.arrayElement( i );
      TagCfgItem tagCfgItem = createOpcTagCfgItem( tagConfig );
      tagsCfgItems.add( tagCfgItem );
    }
    return tagsCfgItems;
  }

  /**
   * По описанию из конфигурации создает пин OPC
   *
   * @param aTagConfig дерево конфигурация пина
   * @return пин
   */
  public static TagCfgItem createOpcTagCfgItem( IAvTree aTagConfig ) {
    String pinTypeId = aTagConfig.fields().getStr( PIN_TYPE_PARAM_NAME );
    EAtomicType tagType = EAtomicType.findById( pinTypeId );
    int namespace = aTagConfig.fields().getInt( "opc.tag.namespace" );
    IAtomicValue tagId = aTagConfig.fields().getByKey( OPC_TAG_PARAM_NAME );

    return new TagCfgItem( namespace, tagId, tagType );
  }

  /**
   * Создаёт идентификатор узла (тега) по конфигурации этого узла.
   *
   * @param aItemCfg TagCfgItem - конфигурация узла.
   * @return NodeId - идентификатор узла (тега)
   */
  public static NodeId createNodeFromCfg( TagCfgItem aItemCfg ) {
    int namespaceId = aItemCfg.getNamespaceId();
    IAtomicValue tagId = aItemCfg.getTagId();

    NodeId nodeId;
    if( tagId.atomicType() == EAtomicType.INTEGER ) {
      nodeId = new NodeId( namespaceId, tagId.asInt() );
    }
    else
      if( tagId.atomicType() == EAtomicType.STRING ) {
        nodeId = new NodeId( namespaceId, tagId.asString() );
      }
      else {
        nodeId = new NodeId( namespaceId, tagId.asString() );
      }

    return nodeId;
  }
}

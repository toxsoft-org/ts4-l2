package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;

/**
 * Утилитный класс для работы с OPC
 *
 * @author max
 */
public class OpcUaUtils {

  private static final String OPC_TAG_NAMESPACE_PARAM_NAME = "opc.tag.namespace";

  /**
   * Имя параметра - подтип пина.
   */
  private static final String PIN_TYPE_EXTRA_PARAM_NAME = "pin.type.extra";

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

  public static Variant convertToOpc( IAtomicValue aValue, EAtomicType aTagType, String aTagTypeExtra ) {
    Variant result;
    switch( aTagType ) {
      case BOOLEAN:
        result = new Variant( Boolean.valueOf( aValue.asBool() ) );
        break;
      case FLOATING:
        result = new Variant( Float.valueOf( aValue.asFloat() ) );
        break;
      case INTEGER: {
        result = new Variant( Integer.valueOf( aValue.asInt() ) );
        if( aTagTypeExtra != null && aTagTypeExtra.length() > 0 ) {
          // if( aTagTypeExtra.equals( "DINT" ) ) {
          // result = new Variant( Integer.valueOf( aValue.asInt() ) );
          // }
          // else
          if( aTagTypeExtra.equals( "INT" ) ) {
            result = new Variant( Short.valueOf( String.valueOf( aValue.asInt() ) ) );
          }
          else
            if( aTagTypeExtra.equals( "BYTE" ) ) {
              result = new Variant( Byte.valueOf( String.valueOf( aValue.asInt() ) ) );
            }
            else
              if( aTagTypeExtra.equals( "WORD" ) ) {// TODO сделать беззнаковый short
                result = new Variant( Short.valueOf( String.valueOf( aValue.asInt() ) ) );
              }
        }
        break;
      }
      case NONE: {
        result = Variant.NULL_VALUE;
        break;
      }
      case STRING: {
        result = new Variant( aValue.asString() );
        break;
      }
      case TIMESTAMP: {
        result = new Variant( Long.valueOf( aValue.asLong() ) );
        break;
      }
      case VALOBJ: {
        result = new Variant( aValue.asValobj() );
        break;
      }
      default: {
        result = new Variant( aValue.asString() );
      }
    }
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

    IAtomicValue tagId = aTagConfig.fields().getByKey( OPC_TAG_PARAM_NAME );

    int namespace = 0;

    if( !aTagConfig.fields().hasKey( OPC_TAG_NAMESPACE_PARAM_NAME ) ) {
      NodeId nodeId = NodeId.parse( tagId.asString() );
      namespace = nodeId.getNamespaceIndex().intValue();
      tagId = AvUtils.avStr( nodeId.getIdentifier().toString() );
    }
    else {
      namespace = aTagConfig.fields().getInt( OPC_TAG_NAMESPACE_PARAM_NAME );
    }

    String tagTypeExtra = aTagConfig.fields().getStr( PIN_TYPE_EXTRA_PARAM_NAME, TsLibUtils.EMPTY_STRING );

    return new TagCfgItem( namespace, tagId, tagType, tagTypeExtra );
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

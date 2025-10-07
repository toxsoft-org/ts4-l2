package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import java.util.*;

import org.eclipse.milo.opcua.sdk.client.nodes.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.util.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

/**
 * Утилитный класс для работы с OPC
 *
 * @author max
 */
public class OpcUaUtils {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( OpcUaUtils.class.getName() );

  private static final String OPC_TAG_NAMESPACE_PARAM_NAME = "opc.tag.namespace";

  /**
   * Имя параметра - подтип пина.
   */
  private static final String PIN_TYPE_EXTRA_PARAM_NAME = "pin.type.extra";

  /**
   * Имя параметра - пин является контрольным словом
   */
  private static final String PIN_CONTROL_WORD_PARAM_NAME = "is.pin.control.word";

  /**
   * Закрытый конструктор
   */
  private OpcUaUtils() {
  }

  public static IAtomicValue convertFromOpc( Variant aValue, EAtomicType aTagType ) {
    if( aValue.isNull() ) {
      return IAtomicValue.NULL;
    }
    if( aValue.getValue() instanceof UShort ) {
      UShort ushortVal = (UShort)aValue.getValue();

      return AvUtils.avInt( ushortVal.intValue() );
    }

    if( aValue.getValue() instanceof UByte ) {
      UByte ubytetVal = (UByte)aValue.getValue();

      return AvUtils.avInt( ubytetVal.intValue() );
    }

    IAtomicValue defaultConvertVal = AvUtils.avFromObj( aValue.getValue() );

    if( defaultConvertVal == null ) {
      logger.error( "Cant convert from opc '%s' to IAtomicValue", aValue.getValue().getClass().getName() );
    }

    return defaultConvertVal;
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
              if( aTagTypeExtra.equals( "WORD" ) ) {
                UShort ushortVal = UShort.valueOf( aValue.asInt() );
                result = new Variant( ushortVal );
                // result = new Variant( Short.valueOf( String.valueOf( aValue.asInt() ) ) );
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

  public static Variant convertToOpc( IAtomicValue aValue, EAtomicType aTagType, Class<?> aTagTypeExtra ) {
    Variant result;
    switch( aTagType ) {
      case BOOLEAN:
        result = new Variant( Boolean.valueOf( aValue.asBool() ) );
        break;
      case FLOATING:
        result = new Variant( Float.valueOf( aValue.asFloat() ) );
        break;
      case INTEGER: {
        if( aTagTypeExtra != null ) {
          result = getVariant( aTagTypeExtra, aValue.asString() );
        }
        else {
          result = new Variant( Integer.valueOf( aValue.asInt() ) );
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
   * Получить Variant из текстового значения
   *
   * @param aClazz класс переменной
   * @param aVal текстовое значение
   * @return значение {@link Variant } применяемое в milo для передачи/записи значений тегов
   */
  public static Variant getVariant( Class<?> aClazz, String aVal ) {
    if( aClazz.equals( Boolean.class ) ) {
      return new Variant( Boolean.valueOf( aVal ) );
    }
    if( aClazz.equals( Byte.class ) ) {
      return new Variant( Byte.valueOf( aVal ) );
    }
    if( aClazz.equals( UByte.class ) ) {
      return new Variant( UByte.valueOf( aVal ) );
    }
    if( aClazz.equals( Short.class ) ) {
      return new Variant( Short.valueOf( aVal ) );
    }
    if( aClazz.equals( UShort.class ) ) {
      return new Variant( UShort.valueOf( aVal ) );
    }
    if( aClazz.equals( Integer.class ) ) {
      return new Variant( Integer.valueOf( aVal ) );
    }
    if( aClazz.equals( UInteger.class ) ) {
      return new Variant( UInteger.valueOf( aVal ) );
    }
    if( aClazz.equals( Long.class ) ) {
      return new Variant( Long.valueOf( aVal ) );
    }
    if( aClazz.equals( ULong.class ) ) {
      return new Variant( ULong.valueOf( aVal ) );
    }
    if( aClazz.equals( Float.class ) ) {
      return new Variant( Float.valueOf( aVal ) );
    }
    return Variant.NULL_VALUE;
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
    EAtomicType tagType = EAtomicType.getById( pinTypeId );

    IAtomicValue tagId = aTagConfig.fields().getByKey( OPC_TAG_PARAM_NAME );

    String tagTypeExtra = aTagConfig.fields().getStr( PIN_TYPE_EXTRA_PARAM_NAME, TsLibUtils.EMPTY_STRING );

    boolean isControlWord = aTagConfig.fields().getBool( PIN_CONTROL_WORD_PARAM_NAME, false );

    int namespace = 0;

    if( !aTagConfig.fields().hasKey( OPC_TAG_NAMESPACE_PARAM_NAME ) ) {
      NodeId nodeId = NodeId.parse( tagId.asString() );
      // namespace = nodeId.getNamespaceIndex().intValue();
      // tagId = AvUtils.avStr( nodeId.getIdentifier().toString() );
      return new TagCfgItem( nodeId, tagType, tagTypeExtra, isControlWord );
    }
    // else {
    namespace = aTagConfig.fields().getInt( OPC_TAG_NAMESPACE_PARAM_NAME );
    // }

    return new TagCfgItem( namespace, tagId, tagType, tagTypeExtra, isControlWord );
  }

  /**
   * Создаёт идентификатор узла (тега) по конфигурации этого узла.
   *
   * @param aItemCfg TagCfgItem - конфигурация узла.
   * @return NodeId - идентификатор узла (тега)
   */
  public static NodeId createNodeFromCfg( TagCfgItem aItemCfg ) {
    if( aItemCfg.getNodeId() != null ) {
      return aItemCfg.getNodeId();
    }

    int namespaceId = aItemCfg.getNamespaceId();
    IAtomicValue tagId = aItemCfg.getTagId();

    NodeId nodeId;
    if( tagId.atomicType() == EAtomicType.INTEGER ) {
      nodeId = new NodeId( namespaceId, tagId.asInt() );
    }
    else {
      if( tagId.atomicType() == EAtomicType.STRING ) {
      }
      nodeId = new NodeId( namespaceId, tagId.asString() );
    }

    return nodeId;
  }

  /**
   * @param aEntity узел значения переменной
   * @return класс типа данных значения узла
   */
  public static Class<?> getNodeDataTypeClass( UaVariableNode aEntity ) {
    // new verion of max
    Class<?> retVal = null;
    try {
      retVal = TypeUtil.getBackingClass( aEntity.getDataType() );
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    // dima 29.02.24 на Siemens верхний метод срабатывает не всегда, а нижний работает
    if( retVal == null ) {
      // old version of dima
      // получение значения узла
      DataValue dataValue = aEntity.getValue();
      // тут получаем Variant
      Variant variant = dataValue.getValue();
      Optional<ExpandedNodeId> dataTypeNode = variant.getDataType();
      if( dataTypeNode.isPresent() ) {
        ExpandedNodeId expNodeId = dataTypeNode.get();
        // TODO разобраться с отображением не числовых типов
        if( expNodeId.getType() == IdType.Numeric ) {
          UInteger id = (UInteger)expNodeId.getIdentifier();
          NodeId nodeId = new NodeId( expNodeId.getNamespaceIndex(), id );
          Class<?> clazz = TypeUtil.getBackingClass( nodeId );
          retVal = clazz;
        }
      }
    }
    return retVal;
  }

  // public static void main( String[] a ) {
  // int bit16 = 64512;
  // System.out.println( Integer.toBinaryString( bit16 ) );
  // UShort ushortVal = UShort.valueOf( bit16 );
  // System.out.println( ushortVal );
  // System.out.println( Integer.toBinaryString( ushortVal.intValue() ) );
  // }
}

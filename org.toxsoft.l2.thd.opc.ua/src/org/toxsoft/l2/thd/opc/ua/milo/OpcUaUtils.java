package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
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
    return IAtomicValue.NULL;
  }

  public static Variant convertToOpc( IAtomicValue aValue, EAtomicType aTagType ) {
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
    EAtomicType tagType = EAtomicType.findById( pinTypeId );
    int namespace = aTagConfig.fields().getInt( "opc.tag.namespace" );
    String tagId = aTagConfig.fields().getStr( OPC_TAG_PARAM_NAME );

    return new TagCfgItem( namespace, tagId, tagType );
  }
}

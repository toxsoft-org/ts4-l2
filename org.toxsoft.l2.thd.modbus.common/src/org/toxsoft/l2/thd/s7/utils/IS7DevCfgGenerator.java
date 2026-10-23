package org.toxsoft.l2.thd.s7.utils;

import org.toxsoft.core.tslib.av.avtree.*;

/**
 * Генератор содержимого файла s7.devcfg по массиву конфигурационных юнитов sk-s7. Построен по паттерну - билдер:
 * сначала устанавливаются все необходимые исходные данные - затем вызывается метод generate().
 *
 * @author dima
 */
@SuppressWarnings( { "javadoc", "nls" } )
public interface IS7DevCfgGenerator {

  /**
   * Формирует содержимое файла конфигурации драйвера (.devcfg)
   *
   * @return IAvTree - содержимое файла конфигурации драйвера (.devcfg)
   */
  IAvTree generate();

  String JAVA_CLASS_PARAM_NAME         = "javaClassName";
  String JAVA_CLASS_PARAM_VAL_TEMPLATE = "org.toxsoft.l2.thd.s7.S7DeviceProducer";

  String S7_CFG_NODE_ID        = "s7.common.cfg";
  String S7_DEVICES_ARRAY_NAME = "s7.devices";

  String ID_PARAM_NAME                  = "id";
  String S7_DEVICE_ID                   = "s7.bridge.collection.id";
  String DESCRIPTION_PARAM_NAME         = "description";
  String DESCRIPTION_PARAM_VAL_TEMPLATE = "Siemens S7 apparat producer";
  String SYNCH_PERIOD_PARAM_NAME        = "period";

  String DEVICE_DEF_FORMAT     = "device.%s.def";
  String DEVICE_ID             = "device.id";
  String DEVICE_IP_ADDRESS     = "IP";
  String DEVICE_RACK           = "rack";
  String DEVICE_SLOT           = "slot";
  String DATABLOCKS_ARRAY_NAME = "data.blocks";

  String DB_DEF_FORMAT   = "db.%s.def";
  String DB_ID           = "db.id";
  String DB_NUM          = "db.num";
  String TAGS_ARRAY_NAME = "tags";

  String TAG_DEF_FORMAT    = "tag.%s.def";
  String TAG_ID            = "tag.id";
  String TAG_BUFFER_OFFSET = "tag.buffer.offset";
  String TAG_KIND_ID       = "tag.kind";         // «R\W\RW»
  String TAG_VALUE_TYPE_ID = "tag.value.type";
  String BIT_NUMBER        = "bit.number";       // только для типа Boolean

}

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

  String DEV_CONFIG_STR = "DeviceConfig = ";

  String JAVA_CLASS_PARAM_NAME         = "javaClassName";
  String JAVA_CLASS_PARAM_VAL_TEMPLATE = "org.toxsoft.l2.thd.s7.S7DeviceProducer";

  String S7_CFG_NODE_ID        = "s7.common.cfg";
  String S7_DEVICES_ARRAY_NAME = "s7.devices";

  String ID_PARAM_NAME                  = "id";
  String S7_DEVICE_ID                   = "s7.bridge.collection.id";
  String DESCRIPTION_PARAM_NAME         = "description";
  String DESCRIPTION_PARAM_VAL_TEMPLATE = "Siemens S7 apparat producer";
  String SYNCH_PERIOD_PARAM_NAME        = "period";

}

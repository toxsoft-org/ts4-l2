
package ru.toxsoft.l2.thd.opc;

/**
 * Жёсткие константы (в основном имена параметров в конфиг файлах)
 *
 * @author max
 */
@SuppressWarnings( "nls" )
public interface IOpcConstants {

  /**
   * Пустая строка
   */
  String EMPTY_STRING           = "";
  /**
   * Имя .
   */
  String DOT                    = ".";
  /**
   * Имя параметра - тип пина.
   */
  String PIN_TYPE_PARAM_NAME    = "pin.type";
  /**
   * Имя тега "bridge.type" из конфигурационного файла.
   */
  String BRIDGE_TYPE_PARAM_NAME = "bridge.type";

  /**
   * Префикс ид конфиг.файла
   */
  String ID_VAL_PREFIX = "opc2s5.";

  /**
   * Имя параметра - описание пина.
   */
  String PIN_DESCR_PARAM_NAME = "pin.descr";

  /**
   * Имя параметра - идентификатор пина.
   */
  String PIN_ID_PARAM_NAME    = "pin.id";
  /**
   * Имя параметра - тэг OPC .
   */
  String OPC_TAG_PARAM_NAME   = "opc.tag";
  /**
   * Имя тега "группы тегов" из конфигурационного файла.
   */
  String GROUPS_PARAM_NAME    = "groups";
  /**
   * Имя тега "группа синхронных тегов" из конфигурационного файла.
   */
  String SYNC_TAGS_PARAM_NAME = "sync.tags";

  /**
   * Имя тега "группа асинхронных тегов" из конфигурационного файла.
   */
  String ASYNC_TAGS_PARAM_NAME = "async.tags";

  /**
   * Имя тега "группа выходных тегов" из конфигурационного файла.
   */
  String OUTPUT_TAGS_PARAM_NAME = "output.tags";

  /**
   * Имя тега "устройства" из конфигурационного файла.
   */
  String PERIOD_PARAM_NAME   = "period";
  /**
   * Имя тега "host" из конфигурационного файла.
   */
  String HOST_PARAM_NAME     = "host";
  /**
   * Имя тега "domain" из конфигурационного файла.
   */
  String DOMAIN_PARAM_NAME   = "domain";
  /**
   * Имя тега "user" из конфигурационного файла.
   */
  String USER_PARAM_NAME     = "user";
  /**
   * Имя тега "password" из конфигурационного файла.
   */
  String PASSWORD_PARAM_NAME = "password";
  /**
   * Имя тега "progId" из конфигурационного файла.
   */
  String PROGID_PARAM_NAME   = "progId";
  /**
   * Имя тега "clsId" из конфигурационного файла.
   */
  String CLSID_PARAM_NAME    = "clsId";

  /**
   * Идентификатор коллекции OPC.
   */
  String OPC_COLLECTION_ID = "opc.collection.id";

  /**
   * Описание коллекции шин OPC
   */
  String OPC_COLLECTION_DESCR = "opc.collection.descr";

  /**
   * Постфикс в полном имени конфигурационного файла моста Opc2S5
   */
  String CONFIG_FILE_POSTFIX = "_opc2s5.devcfg";

  /**
   * Имя тега полное имя класса продюсера
   */
  String JAVA_CLASS_NAME = "javaClassName";

  /**
   * полное имя класса продюсера Opc2S5CollectionProducer
   */
  String OPC_2_S5_COLLECTION_PRODUCER = "ru.toxsoft.l2.core.hal.devices.impl.opc.Opc2S5CollectionProducer";

  /**
   * Имя тега "id" из конфигурационного файла.
   */
  String ID                          = "id";
  /**
   * Имя тега "description" из конфигурационного файла.
   */
  String DESCRIPTION                 = "description";
  /**
   * Значение тега "description" из конфигурационного файла.
   */
  String DESCRIPTION_VAL             = "opc 2 s5 pins apparat producer";
  /**
   * Значение тега "host" из конфигурационного файла по умолчанию.
   */
  String DEFAULT_HOST_VAL            = "localhost";
  /**
   * Значение тега "domain" из конфигурационного файла по умолчанию.
   */
  String DEFAULT_DOMAIN_VAL          = "localhost";
  /**
   * Значение тега "user" из конфигурационного файла по умолчанию.
   */
  String DEFAULT_USER_VAL            = "opc";
  /**
   * Значение тега "password" из конфигурационного файла по умолчанию.
   */
  String DEFAULT_PASSWORD_VAL        = "010101";
  /**
   * Значение тега "progId" из конфигурационного файла по умолчанию.
   */
  String DEFAULT_PROGID_VAL          = "OPC.SimaticNET";
  /**
   * Значение тега "clsId" из конфигурационного файла по умолчанию.
   */
  String DEFAULT_CLSID_VAL           = "2C2E36B7-FE45-4A29-BF89-9BFBA6A40857";
  /**
   * Имя тега "bridges" из конфигурационного файла.
   */
  String BRIDGES_PARAM_NAME          = "bridges";
  /**
   * Идентификатор коллекции мостов OPC2S5.
   */
  String OPC2S5_BRIDGE_COLLECTION_ID = "opc2s5.bridge.collection.id";

  /**
   * Описание коллекции коллекции мостов OPC2S5
   */
  String OPC2S5_BRIDGE_COLLECTION_DESCR = "opc2s5.bridge.collection.descr";
  /**
   * user name for OPC Client
   */
  String OPC2S5_BRIDGE_CLIENT_HANDLE    = "OPC2S5 bridge";
  /**
   * Постфикс описания группы синхронных данных
   */
  String SYNC_GROUP_DEF_POSTFIX         = ".sync.group.def";
  /**
   * Постфикс описания группы асинхронных данных
   */
  String ASYNC_GROUP_DEF_POSTFIX        = ".async.group.def";

  /**
   * Мост на движке openScada
   */
  String OPENSCADA_BRIDGE = "OPENSCADA";

  /**
   * Мост на движке JEasyOpc
   */
  String JEASYOPC_BRIDGE = "JEasyOpc";

}

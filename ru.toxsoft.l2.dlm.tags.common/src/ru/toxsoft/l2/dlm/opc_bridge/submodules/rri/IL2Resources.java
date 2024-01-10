package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

/**
 * Локализуемые ресурсы подмодулей загружаемого модуля НСИ.
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  String MSG_COMMAND_COME_FOR_RRI_MODULE              = "Command %s come for RRI module";
  String MSG_COMMAND_COMPLETE_RRI_MODULE              = "Command %s complete in RRI module";
  String MSG_COMMAND_UNDER_DEVELOPMENT_RRI_MODULE     = "Command %s complete is under development in RRI module";
  String MSG_COMMAND_STATE_CHANGED_FOR_RRI_MODULE     = "State of RRI module command ( %s ) changed  on: %s";
  String MSG_COMMAND_STATE_CANT_CHANGE_FOR_RRI_MODULE = "Can't change of RRI module command ( %s ) state: %s";

  String MSG_RRI_DATA_MODULE_IS_STARTED_FORMAT = "Работа с НСИ данными в модуле %s стартовала";

  String ERR_MSG_CONFIG_IS_CALLED_TWICE_FORMAT = "Метод конфигурации вызван повторно в %s";

  String ERR_MSG_INVALID_OBJ = "В системе нет объекта: %s класса: %s";

  String ERR_MSG_INVALID_DATA_ID = "В описании класса: %s нет dataId: %s ";

  String ERR_MSG_CANT_CREATE_TRANSMITTER_FOR_PIN_ID = "Не могу создать передатчик значения для пина: %s";

  String ERR_MSG_ADD_PARAM_METHOD_AFTER_CONFIG_FORMAT =
      "Метод добавления параметров конфигурации вызван после процесса конфигурации в %s";

  String ERR_MSG_RRI_MODULE_CANT_BE_STARTED_FORMAT =
      "Модуль НСИ в %s не может быть запущен, так как не сконфигурирован";

  String ERR_MSG_CANT_CREATE_INSTANCE_TRANSMITTER_FORMAT = "Cant create instance of transmitter %s";

  String ERR_MSG_DEFAULT_VALUE_OF_PARAM_IS_ABSENT_FORMAT = "default value of param %s is absent";

  String ERR_MSG_DURING_CONFIG_DATA_TRANSMITTER_FORMAT = "Error during configuration of data %s of transmitter %s";

  String ERR_MSG_DURING_CONFIG_TAG_TRANSMITTER_FORMAT = "Error during configuration of tag %s of transmitter %s";

}

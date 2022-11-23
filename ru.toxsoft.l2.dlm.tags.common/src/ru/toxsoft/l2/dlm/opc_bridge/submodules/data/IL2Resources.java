package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

/**
 * Локализуемые ресурсы подмодулей загружаемого модуля диспетчеризации ТМ.
 *
 * @author max
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  String MSG_COMMAND_COME_FOR_APPLICATION = "Command %s come for application";

  String MSG_HISTORY_DATA_MODULE_IS_STARTED_FORMAT = "Работа с историческими данными в модуле %s стартовала";

  String MSG_CURR_DATA_MODULE_IS_STARTED_FORMAT = "Работа с текущими данными в модуле %s стартовала";

  String MSG_EVENTS_MODULE_IS_STARTED_FORMAT = "Работа с событиями в модуле %s стартовала";

  String ERR_MSG_EVENT_MODULE_IS_NOT_STARTED_FORMAT = "Подмодуль событий в %s не стартовал:\n Некорректно указаны пины";

  String ERR_MSG_CONFIG_IS_CALLED_TWICE_FORMAT = "Метод конфигурации вызван повторно в %s";

  String ERR_MSG_INVALID_OBJ = "В системе нет объекта: %s класса: %s";

  String ERR_MSG_INVALID_DATA_ID = "В описании класса: %s нет dataId: %s ";

  String ERR_MSG_GET_TRANSMITTERS_BEFORE_CONFIG_FORMAT =
      "Запрос передатчиков сигнала до завершения процесса конфигурации в %s";

  String ERR_MSG_GET_DATASET_BEFORE_CONFIG_FORMAT = "Запрос датасета до завершения процесса конфигурации в %s";

  String ERR_MSG_CANT_CREATE_TRANSMITTER_FOR_PIN_ID = "Не могу создать передатчик значения для пина: %s";

  String ERR_MSG_ADD_PARAM_METHOD_AFTER_CONFIG_FORMAT =
      "Метод добавления параметров конфигурации вызван после процесса конфигурации в %s";

  String ERR_MSG_EVENT_MODULE_CANT_BE_STARTED_FORMAT =
      "Модуль событий в %s не может быть запущен, так как не сконфигурирован";

  String ERR_MSG_COMMAND_MODULE_CANT_BE_STARTED_FORMAT =
      "Модуль команд в %s не может быть запущен, так как не сконфигурирован";

  String ERR_MSG_HISTORY_MODULE_CANT_BE_STARTED_FORMAT =
      "Модуль исторических данных в %s не может быть запущен, так как не сконфигурирован";

  String ERR_MSG_CURR_MODULE_CANT_BE_STARTED_FORMAT =
      "Модуль текущих данных в %s не может быть запущен, так как не сконфигурирован";

  String ERR_SYNCH_PIN_HAS_WRONG_MIN_UPDATE_INTERVAL = "Synch pin %s has min update interval 0 or less then 0";

  String ERR_EXCEEDED_MAX_UPDATE_INTERVAL =
      "Send NULL on server with %d time from synch pin: %s, because of exceedind update interval more then 2 times";

  String ERR_MSG_CANT_CREATE_INSTANCE_TRANSMITTER_FORMAT = "Cant create instance of transmitter %s";

  String ERR_MSG_DEFAULT_VALUE_OF_PARAM_IS_ABSENT_FORMAT = "default value of param %s is absent";

  String ERR_MSG_DURING_CONFIG_DATA_TRANSMITTER_FORMAT = "Error during configuration of data %s of transmitter %s";

  String ERR_MSG_DURING_CONFIG_TAG_TRANSMITTER_FORMAT = "Error during configuration of tag %s of transmitter %s";

  String ERR_MSG_TRANSMIT_SCRIPT_THREW_EXCEPTION = "transmit script for event threw exception";

  String ERR_MSG_TRANSMIT_SCRIPT_RETURNED_NULL = "transmit script for event returned null";

  String ERR_MSG_DATA_INIT_SCRIPT_THREW_EXCEPTION = "data transmitter init script threw exception";

}

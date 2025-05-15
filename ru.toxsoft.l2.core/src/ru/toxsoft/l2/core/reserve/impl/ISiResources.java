package ru.toxsoft.l2.core.reserve.impl;

/**
 * Локлизуемые ресурсы.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface ISiResources {

  String STR_N_BECOME_MAIN_INTERVAL = "Интервал перехода в состояние Главный";
  String STR_D_BECOME_MAIN_INTERVAL =
      "Интервал после запуска, по истечении которого шкаф может перейти в состояние Главный";

  String STR_N_IS_ONLY_BOX = "Шкаф является единственным";
  String STR_D_IS_ONLY_BOX = "Шкаф является единственным (без резервного)";

  String STR_N_IS_DEFAULT = "Шкаф является шкафом по умолчанию";
  String STR_D_IS_DEFAULT = "Шкаф является шкафом по умолчанию";

  String STR_N_MAIN_CONTROL_PIN = "Идентификатор пина - мастер";
  String STR_D_MAIN_CONTROL_PIN = "Идентификатор выходного пина, устанавливающего железо в состояние мастер";

  String STR_N_RESERVE_MEANDER_HALH_PERIOD = "Половина периода меандра";
  String STR_D_RESERVE_MEANDER_HALH_PERIOD =
      "пол периода меандра входного пина с тремя состояниями (меандер), принимающего состояние шкафа-партнёра";

  String STR_N_RESERVE_MEANDER_MIN_COUNT = "Мин. количество тактов на пол периода меандра";
  String STR_D_RESERVE_MEANDER_MIN_COUNT =
      "Минимальное количество тактов на пол периода меандра входного пина с тремя состояниями (меандер)";

  String STR_N_RESERVE_MEANDER_PIN = "Идентификатор входного пина - состояние шкафа партнёра";
  String STR_D_RESERVE_MEANDER_PIN =
      "Идентификатор входного пина с тремя состояниями (меандер) - состояние шкафа партнёра";

  String STR_N_RESERVE_START_INTERVAL = "Интервал запуска системы резервирования";
  String STR_D_RESERVE_START_INTERVAL =
      "Интервал запуска системы резервирования, в течение которого алгоритм ещё стоит, а обмен уже работает";

  String STR_N_RSRV_CMD_REREG_INTERVAL = "Интервал между попытками регистрации команд резервирования";
  String STR_D_RSRV_CMD_REREG_INTERVAL = "Интервал между попытками регистрации обработчика команд резервирования";

  String STR_N_DLMS_STOP_TIMEOUT = "Таймаут останова модулей";
  String STR_D_DLMS_STOP_TIMEOUT = "В течение времени таймаута модули не были остановлены";

  String STR_N_PARTNER_TIMEOUT = "Таймаут сообщений от напарника";
  String STR_D_PARTNER_TIMEOUT = "В течение времени таймаута от напарника не приходило сообщений о его состоянии";

  String STR_N_PAS_SERVER_REG_TRY_PERIOD = "Промежуток между попытками зарегистрировать pas сервер";
  String STR_D_PAS_SERVER_REG_TRY_PERIOD = "Промежуток между попытками зарегистрировать pas сервер на прослушивание";

  String STR_N_LOCAL_ID = "Идентификатор";
  String STR_D_LOCAL_ID = "Идентификатор локального узла HotSwap";

  String STR_N_WEIGHT = "Вес";
  String STR_D_WEIGHT = "Вес локального узла HotSwap при выборе главного узла";

  String STR_N_LOCAL_ADDRESS = "localIP";
  String STR_D_LOCAL_ADDRESS = "IP-адрес или сетевое имя локального узла HotSwap";

  String STR_N_LOCAL_PORT = "localPort";
  String STR_D_LOCAL_PORT = "Номер порта локального узла HotSwap";

  String STR_N_REMOTE_ADDRESS = "remoteIP";
  String STR_D_REMOTE_ADDRESS = "IP-адрес или сетевое имя удаленного узла HotSwap";

  String STR_N_REMOTE_PORT = "remotePort";
  String STR_D_REMOTE_PORT = "Номер порта удаленного узла HotSwap";

  String STR_D_ELECTION = "Узел находится в состоянии выбора текущего состояния";
  String STR_N_ELECTION = "Выборы";

  String STR_D_MAIN = "Узел находится в состоянии главного узла";
  String STR_N_MAIN = "Главный";

  String STR_D_RESERV = "Узел находится в состоянии резервног узла";
  String STR_N_RESERV = "Резервный";

  String MSG_HELLO = "SITROL TM2 HotSwap Software. ToxSoft, 2019.";
  String MSG_BYE   = "Program finished nomally.";
  String MSG_FAIL  = "Bridge abnormally terminated with exit code 2";

  String MSG_METHOD_NOT_FOUND = "Метод не найден %s";

  String MSG_ERR_GET_CONFIG          = "Ошибка получения конфигурационных данных табло. %s. - %s";
  String MSG_ERR_UNEXPECTED          = "Неожиданное получение: %s";
  String FMT_ERR_NO_CFG_FILE         = "Недоступен файл конфигурации %s";
  String FMT_WARN_IGNORED_INV_ARG_ID = "Аргумент '%s' не ИД-путь, он будет игнорирован";
  String FMT_WARN_ARG_VAL_AS_STRING  = "Значение аргмента '%s' не атомарное значение, интерпретировано как строка";

}

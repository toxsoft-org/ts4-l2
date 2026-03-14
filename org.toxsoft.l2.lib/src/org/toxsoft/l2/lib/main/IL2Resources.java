package org.toxsoft.l2.lib.main;

/**
 * Локализуемые ресурсы точки входа в программу нижнего уровня.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  // ------------------------------------------------------------------------------------
  // EMainOps
  //

  String E_MO_HAL_CONFIG_FILE                = "Путь к файлу конфигурации уровня драйверов (HAL).";
  String E_MO_THD_CONFIG_DIR                 = "Путь к директории, содержащий файлы конфигурации драйверов THD";
  String E_MO_DLM_CONFIG_DIR                 = "Путь к директории, содержащий файлы конфигурации модулей DLM";
  String E_MO_N_HAL_CONFIG_FILE              = "Файл конфигурации HAL.";
  String E_MO_N_THD_CONFIG_DIR               = "Директория конфигурации THD";
  String E_MO_N_DLM_CONFIG_DIR               = "Директории конфигурации модулей DLM";
  String E_MO_CONTROLLER_NO                  = "Номер контроллера (шкафа)";
  String E_MO_N_CONTROLLER_NO                = "Номер контроллера";
  String E_MO_PLUGINS_DIR                    = "Директория расположения динамически загружаемых модулей (DLM)";
  String E_MO_N_PLUGINS_DIR                  = "Директория модулей";
  String E_MO_DATA_DIR                       = "Директория расположения изменяющихся файлов данных";
  String E_MO_N_DATA_DIR                     = "Директоря данных";
  String E_MO_PLUGINS_RESCAN_INTERVAL_SECS   = "Интервал прверки обновлении модулей (в секундах)";
  String E_MO_N_PLUGINS_RESCAN_INTERVAL_SECS = "Интервал проверки";
  String E_MO_MAIN_LOOP_SLEEP_MSECS          = "Время \"засыпания\" на каждом проходе главного цикла в миллисекундах";
  String E_MO_N_MAIN_LOOP_SLEEP_MSECS        = "Главной простой";

}

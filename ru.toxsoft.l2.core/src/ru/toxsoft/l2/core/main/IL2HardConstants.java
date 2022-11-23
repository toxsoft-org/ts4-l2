package ru.toxsoft.l2.core.main;

import ru.toxsoft.l2.core.dlm.*;

/**
 * Жестко "зашитые" в коде, персистентные константы.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
public interface IL2HardConstants {

  /**
   * Имя главного файла конфигурации по умолчанию.
   */
  String L2_MAIN_CFG_FILE_NAME = "cfg/l2.cfg";

  /**
   * Имя главного файла конфигурации по умолчанию.
   */
  String L2_HAL_CFG_FILE_NAME = "cfg/hal/hal.cfg";

  /**
   * Имя главного файла конфигурации по умолчанию.
   */
  String L2_NET_CFG_FILE_NAME = "cfg/net/net.cfg";

  /**
   * Пароль БУ для входа на сервер.
   * <p>
   * У всех БУ разные логины на сервер, но одинаковые пароли.
   */
  String PASSWORD_FOR_SERVER = "1";

  // ------------------------------------------------------------------------------------
  // Поддержка динамически загружаемых модулей (DLM - Dynamic Loadable Modules)
  //

  /**
   * Тип плагина, в котором хранится DLM-модули.
   * <p>
   * Эта константа используется в качестве значения параметра {@link EPluginManagerOps#PLUGIN_TYPE_ID}. Напомним, что
   * JAR-файлы плагинов должны быть созданы по правилам подсистемы плагинов {@link IPluginManagerComponent} из
   * библиотеки tslib.
   */
  String DLM_PLUGIN_TYPE_ID = "DLM";

  /**
   * Название свойства {@link IDlmInfo#moduleName()} в соотвтетсвующем разделе манифеста JAR-файла плагина.
   */
  String MF_ATTR_DLM_NAME = "DlmName";

  /**
   * Название свойства {@link IDlmInfo#developerPersons()} в соотвтетсвующем разделе манифеста JAR-файла плагина.
   */
  String MF_ATTR_DLM_DEVELOPER_PERSONS = "DeveloperPersons";

  /**
   * Название свойства {@link IDlmInfo#developerCompany()} в соотвтетсвующем разделе манифеста JAR-файла плагина.
   */
  String MF_ATTR_DLM_DEVELOPER_COMPANY = "DeveloperCompany";

}

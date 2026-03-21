package org.toxsoft.l2.lib;

import org.toxsoft.core.tslib.math.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.l2.lib.dlm.*;

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

  // ------------------------------------------------------------------------------------
  // NEW section - adding tags, L2Application, refactoring old code, etc.
  //

  /**
   * Allowed range of the L2 entities health value.
   * <p>
   * The {@link IntRange#maxValue()} means absolutely healthy entity, {@link IntRange#minValue()} is a dead one.
   */
  IntRange L2_HEALTH_RANGE = new IntRange( 0, 100 );

  /**
   * Allowed range of the L2 entities health contribution measure to the system overall health.
   * <p>
   * The {@link IntRange#maxValue()} means most influent entity, {@link IntRange#minValue()} is a least important one.
   */
  IntRange L2_HEALTH_CONTRIBUTION_RANGE = new IntRange( 1, 10 );

  /**
   * Default value for health contribution if not specified.
   */
  int DEFAULT_HEALTH_CONTRIBUTION = 5;

}

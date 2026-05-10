package org.toxsoft.l2.lib.main;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.l2.lib.main.IL2Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;

/**
 * Глобальные параметры настройки программы нижнего уровня.
 * <p>
 * Значения параметров доступны через {@link IGlobalContext#globalOps()}.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
public interface IGlobalOps {

  /**
   * Путь к файлу конфигурации нижнего уровня.
   */
  IDataDef HAL_CONFIG_FILE = create( "HalConfigFile", STRING, TSID_DESCRIPTION, E_MO_HAL_CONFIG_FILE, TSID_NAME,
      E_MO_N_HAL_CONFIG_FILE, TSID_DEFAULT_VALUE, avStr( "cfg/hal/hal.cfg" ) );

  /**
   * Путь к директории, содержащей файлы конфигурации модулей.
   */
  IDataDef DLM_CONFIG_DIR = create( "DlmConfigDir", STRING, TSID_DESCRIPTION, E_MO_DLM_CONFIG_DIR, TSID_NAME,
      E_MO_N_DLM_CONFIG_DIR, TSID_DEFAULT_VALUE, avStr( "cfg/dlms" ) );

  /**
   * Путь к директории, содержащей файлы конфигурации драйверов.
   */
  IDataDef THD_CONFIG_DIR = create( "ThdConfigDir", STRING, TSID_DESCRIPTION, E_MO_THD_CONFIG_DIR, TSID_NAME,
      E_MO_N_THD_CONFIG_DIR, TSID_DEFAULT_VALUE, avStr( "cfg/hal/thds" ) );

  /**
   * Номер контроллера.
   */
  IDataDef CONTROLLER_NO = create( "ControllerNo", INTEGER, TSID_DESCRIPTION, E_MO_CONTROLLER_NO, TSID_NAME,
      E_MO_N_CONTROLLER_NO, TSID_DEFAULT_VALUE, avInt( 101 ) );

  /**
   * Класс, реализующий API работы с функционалом, специфичным для конкретного проекта.
   */
  IDataDef APP_CLASS_NAME =
      create( "AppClassName", STRING, TSID_DESCRIPTION, "Имя класса проектно-зависимого функционала", TSID_NAME,
          "Класс спец. функционала", TSID_DEFAULT_VALUE, avStr( "ru.toxsoft.l2.core.app.impl.BaseAppImpl" ) );

  /**
   * Директория расположения общепрограммных динамически загружаемых модулей (DLM).<br>
   * Тип данных: примитивный {@link EAtomicType#STRING}<br>
   * Формат: абсолютный или относительный (по отношению к стартовой директории) путь<br>
   * Значение по умолчанию: "dlms/common" (подкаталог в стартовой директории)
   */
  IDataDef PLUGINS_DIR = create( "PluginsCommonDir", STRING, TSID_DESCRIPTION, E_MO_PLUGINS_DIR, TSID_NAME,
      E_MO_N_PLUGINS_DIR, TSID_DEFAULT_VALUE, avStr( "dlms" ) );

  /**
   * Директория расположения изменяющихся файлов данных.<br>
   * Тип данных: примитивный {@link EAtomicType#STRING}<br>
   * Формат: абсолютный или относительный (по отношению к стартовой директории) путь<br>
   * Значение по умолчанию: "data" (подкаталог в стартовой директории)
   */
  IDataDef DATA_DIR = create( "DataDir", STRING, TSID_DESCRIPTION, E_MO_DATA_DIR, TSID_NAME, E_MO_N_DATA_DIR,
      TSID_DEFAULT_VALUE, avStr( "data" ) );

  /**
   * Интервал проверки обновлении модулей.<br>
   * Тип данных: примитивный {@link EAtomicType#INTEGER}<br>
   * Формат: количество секунд между проверками директорий на появление измененных модулей<br>
   * Значение по умолчанию: 60 (одна минута)
   */
  IDataDef PLUGINS_RESCAN_INTERVAL_SECS =
      create( "PluginsRescanIntervalSecs", INTEGER, TSID_DESCRIPTION, E_MO_PLUGINS_RESCAN_INTERVAL_SECS, TSID_NAME,
          E_MO_N_PLUGINS_RESCAN_INTERVAL_SECS, TSID_DEFAULT_VALUE, avInt( 60 ) );

  /**
   * Время \"засыпания\" на каждом проходе главного цикла в миллисекундах.<br>
   * Тип данных: примитивный {@link EAtomicType#INTEGER}<br>
   * Формат: кол-во миллисекунд (0..100), который главный цикл "уступает" драгим процессам на кажом проходе<br>
   * Значение по умолчанию: 10 (10 миллисекунд)
   */
  IDataDef MAIN_LOOP_SLEEP_MSECS = create( "MainLoopSleepMsecs", INTEGER, TSID_DESCRIPTION, E_MO_MAIN_LOOP_SLEEP_MSECS,
      TSID_NAME, E_MO_N_MAIN_LOOP_SLEEP_MSECS, TSID_DEFAULT_VALUE, avInt( 10 ) );

  /**
   * List of all known parameters used for {@link IL2Application} initialization.
   */
  IStridablesList<IDataDef> ALL_L2_GLOBAL_OPS = new StridablesList<>( //
      HAL_CONFIG_FILE, //
      DLM_CONFIG_DIR, //
      THD_CONFIG_DIR, //
      CONTROLLER_NO, //
      APP_CLASS_NAME, //
      PLUGINS_DIR, //
      DATA_DIR, //
      PLUGINS_RESCAN_INTERVAL_SECS, //
      MAIN_LOOP_SLEEP_MSECS //
  );

}

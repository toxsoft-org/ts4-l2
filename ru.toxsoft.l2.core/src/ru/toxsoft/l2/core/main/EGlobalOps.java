package ru.toxsoft.l2.core.main;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static ru.toxsoft.l2.core.main.IL2Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;

/**
 * Глобальные параметры настройки программы нижнего уровня.
 * <p>
 * Значения параметров доступны через {@link IGlobalContext#globalOps()}.
 * <p>
 * Эти жа параметры используются в качестве аргументов командной строки в формате <b>-{@link #id()} value</b>. Где
 * <b>value</b> это атомарное значение, записанное по правилам {@link IDvWriter#writeAtomicValue(IAtomicValue)}.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
public interface EGlobalOps {

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
   * Имя хоста или IP-адрес сервера ВУ.<br>
   * Тип данных: примитивный {@link EAtomicType#STRING}<br>
   * Формат: IP-адрес (вида 192.168.0.1) или имя хоста (вида ct.server.zavod.ru) сервера верхнего уровня<br>
   * Значение по умолчанию: "localhost" (на своей машине, удобно для отладчоного запуска)
   */
  // SERVER_ADDRESS( "ServerAddress", E_MO_SERVER_ADDRESS, E_MO_N_SERVER_ADDRESS, avStr( "localhost" ) ),

  /**
   * Номер порта сервера для доступа по JNDI к провайдеру имен Java (JNP).<br>
   * Тип данных: примитивный {@link EAtomicType#INTEGER}<br>
   * Формат: номер TCP-порта для доступа на сервере к слубам именования Java (JNP)<br>
   * Значение по умолчанию: 1099
   */
  // SERVER_JNP_PORT( "ServerJnpPort", E_MO_SERVER_JNP_PORT, E_MO_N_SERVER_JNP_PORT, avInt( 1099 ) ),

  /**
   * Логин (имя пользователя), с которым программа входит на сервер.<br>
   * Тип данных: примитивный {@link EAtomicType#STRING}<br>
   * Значение по умолчанию: admin
   */
  // LOGIN_NAME( "LoginName", E_MO_LOGIN_NAME, E_MO_N_LOGIN_NAME, avStr( "admin" ) ),

  /**
   * Время \"засыпания\" на каждом проходе главного цикла в миллисекундах.<br>
   * Тип данных: примитивный {@link EAtomicType#INTEGER}<br>
   * Формат: кол-во миллисекунд (0..100), который главный цикл "уступает" драгим процессам на кажом проходе<br>
   * Значение по умолчанию: 10 (10 миллисекунд)
   */
  IDataDef MAIN_LOOP_SLEEP_MSECS = create( "MainLoopSleepMsecs", INTEGER, TSID_DESCRIPTION, E_MO_MAIN_LOOP_SLEEP_MSECS,
      TSID_NAME, E_MO_N_MAIN_LOOP_SLEEP_MSECS, TSID_DEFAULT_VALUE, avInt( 10 ) );

  /**
   * Время \"засыпания\" на каждом проходе серверного цикла в миллисекундах.<br>
   */
  // NET_LOOP_SLEEP_MSECS( "NetLoopSleepMsecs", E_MO_NET_LOOP_SLEEP_MSECS, E_MO_N_NET_LOOP_SLEEP_MSECS, avInt( 10 ) ),

  OptionSet optionSet = new OptionSet();

  /**
   * Возвращает набор всех опции в виде {@link IOptionSet}.
   *
   * @return IOptionSet - набор опции компоненты по умолчанию
   */
  static IOptionSet asOptionSet() {
    if( optionSet.size() == 0 ) {
      HAL_CONFIG_FILE.setValue( optionSet, HAL_CONFIG_FILE.defaultValue() );
      DLM_CONFIG_DIR.setValue( optionSet, DLM_CONFIG_DIR.defaultValue() );
      THD_CONFIG_DIR.setValue( optionSet, THD_CONFIG_DIR.defaultValue() );
      CONTROLLER_NO.setValue( optionSet, CONTROLLER_NO.defaultValue() );
      APP_CLASS_NAME.setValue( optionSet, APP_CLASS_NAME.defaultValue() );
      PLUGINS_DIR.setValue( optionSet, PLUGINS_DIR.defaultValue() );
      DATA_DIR.setValue( optionSet, DATA_DIR.defaultValue() );
      PLUGINS_RESCAN_INTERVAL_SECS.setValue( optionSet, PLUGINS_RESCAN_INTERVAL_SECS.defaultValue() );
      MAIN_LOOP_SLEEP_MSECS.setValue( optionSet, MAIN_LOOP_SLEEP_MSECS.defaultValue() );
    }
    return optionSet;
  }

}

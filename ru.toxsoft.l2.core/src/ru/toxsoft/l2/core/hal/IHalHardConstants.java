package ru.toxsoft.l2.core.hal;

/**
 * Жестко "зашитые" в коде, персистентные константы HALa.
 * 
 * @author max
 */
@SuppressWarnings( "nls" )
public interface IHalHardConstants {

  /**
   * Ключевое слово, с которого начинается конфигурация драйвера в файле конфигурации.
   */
  String DEVICE_CONFIG = "DeviceConfig";

  /**
   * Расширение фалов конфигураций драйверов.
   */
  String DEVCFG = "devcfg";

  /**
   * Имя параметра в файле конфигурации драйвера - класс продюсера драйвером.
   */
  String JAVA_CLASS_NAME = "javaClassName";

  /**
   * Имя параметра в файле конфигурации драйвера - максимальное время, отводимое на запись на устройство.
   */
  String MAX_WRITE_TIME = "maxWriteTime";

  /**
   * Имя параметра в файле конфигурации драйвера - максимальное время, отводимое на чтение с устройства.
   */
  String MAX_READ_TIME = "maxReadTime";

  /**
   * Идентификатор общей ошибки НУ.
   */
  String COMMON_APPARAT_ERROR_ID = "common.apparat.error";

  /**
   * Идентификатор общей ошибки НУ.
   */
  String MUST_RESERVE = "mustReserve";
}

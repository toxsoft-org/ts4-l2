package ru.toxsoft.l2.core.hal.impl;

/**
 * Локализуемые ресурсы реализации HALa.
 * 
 * @author max
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  String MSG_HAL_CFG_FILE_READ_OK = "Считан конфигурационный файл HALa '%s'";

  String MSG_HAL_ERR_CANT_READ_CFG_FILE = "Ошибка чтения конфигурационного файла HALa '%s'";

  String ERR_HAL_ISNT_STARTED_DUE_TO_CFG_FAIL = "HAL не запущен из-за ошибки загрузки конфигураций";

  String ERR_PRODUCER_CREATION_FAILED = "Продюсер не создан";

  String ERR_APPARATUS_PINS_CREATION_FAILED = "Пины аппарата не созданы продюсером'";

  String ERR_SPEC_DEV_APPARATS_CREATION_FAILED = "Аппараты спец. устройств не созданы продюсером'";

  String ERR_PRODUCER_CREATION_N_CONFIG_FAILED =
      "Не удалось создать экземпляр класса-продюсера и корректно настроить его по поданной конфигурационной информации";

}

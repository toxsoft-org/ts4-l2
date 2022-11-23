package ru.toxsoft.l2.core.cfg.impl;

/**
 * Локализуемые ресурсы загрузчика конфигураций.
 * 
 * @author max
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  String ERR_CONFIG_LOADING_N_CREATION = "Ошибка загрузки и создания конфигурации из файла %s";

  String ERR_LIST_ALREADY_CONTAINS_CONFIG =
      "Список конфигураций уже содержит конфигурацию %s. Конфигурации из файла %s не добавлена";

}

package ru.toxsoft.l2.core.dlm.impl;

/**
 * Локализуемые ресурсы базовой реализации динамически загружаемых модулей.
 * 
 * @author goga
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  // ------------------------------------------------------------------------------------
  // AbstractDlmFactory
  //

  String MSG_ERR_DLM_CREATION_EXCEPTION = "Фабрика модулей \"%s\" (%s) вместо создания модуля выбросила исключение";
  String MSG_ERR_NULL_DLM_CREATED = "Фабрика модулей \"%s\" (%s) вместо создания модуля вернула null";
  String MSG_ERR_DLM_CREATED_WITH_BAD_INFO =
      "Фабрика модулей \"%s\" (%s) создала модуль с не своей идентификационной информацией";

  // ------------------------------------------------------------------------------------
  // DlmHolder
  //

  String MSG_ERR_DLM_CANT_START = "Модуль %s  не смог стартовать";
  String MSG_ERR_EXCEPTION_DURING_DLM_STOP_QUERY = "Исключение при попытке остановки модуля %s ";
  String MSG_ERR_EXCEPTION_DURING_DLM_STOPPING = "Исключение в процессе остановки модуля %s ";
  String MSG_ERR_EXCEPTION_DURING_DLM_DESTROY = "Исключение при уничтожении модуля %s ";
  String MSG_ERR_EXCEPTION_DURING_DLM_DO_JOB = "Исключение при работе модуля %s ";
  String MSG_INFO_NEW_PLUGIN = "обнаружен новый модуль %s";
  String MSG_INFO_CHANGED_PLUGIN = "обнаружена новая версия модуля %s (старая версия была %s)";
  String MSG_INFO_PLUGIN_REMOVED = "обнаружен удаление модуля %s";
  String MSG_ERR_DLM_NOT_FOUND = "Модуль %s  не найден";

  // ------------------------------------------------------------------------------------
  // DlmManager
  //

  String MSG_ERR_DLM_ALREADY_EXISTS = "Модуль с идентификатором '%s' (название модуля: %s) уже загружен";
  String MSG_ERR_CANT_INIT_PLUGIN = "Не могу инициализировать загруженный модуль %s из файла %s";

  // ------------------------------------------------------------------------------------
  // DlmContext
  //

  String ERR_WRONG_TYPE_VAL_ATTAMPT = "Попытка присвоить значение другого типа переменной %s";
  String ERR_UNMUTABLE_VAL_ATTAMPT = "Попытка присвоить значение неизменяемой переменной %s";
  String ERR_NULL_ARGUMENTS = "Метод должен содержать аргументы вызова, а они null";
  String ERR_ARG_VALUE_EMPTY = "Нет значения аргумента %s";
}

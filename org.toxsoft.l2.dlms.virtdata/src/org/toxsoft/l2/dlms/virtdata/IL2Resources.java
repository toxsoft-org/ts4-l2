package org.toxsoft.l2.dlms.virtdata;

/**
 * Локализуемые ресурсы загружаемого модуля.
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  String ERR_MSG_DISPATCH_SUBMODULE_START_ERROR = "Ошибка при старте  одного из подмодулей UserRulesExecutionDlm";

  String ERR_MSG_DISPATCH_SUBMODULE_CONFIG_ERROR = "Ошибка при конфигурации одного из подмодулей UserRulesExecutionDlm";

  /**
   * Сообщение об ошибке - необходимый параметр не задан в конфигурации
   */
  String ERR_MSG_NECESSARY_PARAM_IS_NOT_SET = "Необходимый параметр конфигурации модуля не задан";

  /**
   * Сообщение об ошибке - при выполнении JavaScript правила сгенерировано исключение
   */
  String ERR_MSG_JAVACSRIPT_RUN_EXCEPTION = "При выполнении JavaScript правила сгенерировано исключение: %s";

  /**
   * Сообщение об ошибке - не удалось получить Cod для данного.
   */
  String ERR_MSG_COULD_NOT_GET_COD = "Не удалось получить Cod для данного '%s'";

  /**
   * Сообщение об ошибке - не выполнить скрипт.
   */
  String ERR_MSG_TRANSMIT_SCRIPT_THREW_EXCEPTION = "rule script threw exception";

  /**
   * Сообщение об ошибке - не найден объекта.
   */
  String ERR_MSG_RULE_OBJ_NOT_FOUND = "Rule def: %s, class id: %s, strid: %s not found";

  /**
   * Отладочное сообщение о генерации события сработки правила.
   */
  String DEBUG_MSG_FIRE_EVENT = "Event fired! lass id: %s, obj name: %s, error: %s, event description: %s";
}

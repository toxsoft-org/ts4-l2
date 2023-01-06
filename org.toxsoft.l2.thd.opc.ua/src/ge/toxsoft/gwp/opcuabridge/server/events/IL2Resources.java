package ge.toxsoft.gwp.opcuabridge.server.events;

/**
 * Локализуемые ресурсы подмодулей загружаемого модуля диспетчеризации ТМ.
 *
 * @author max
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  String VALUES_EVENT_PARAMS_FORMER_DESCR_FORMAT = "Values Event Params Former Id=%s";

  String EVENTS_ATTR_INPUT_SRC_DESCR_FORMAT = "Attr Id=%s, Input Value Source Id=%s";

  String EVENTS_TAG_INPUT_SRC_DESCR_FORMAT = "Spec Device Id=%s, Tag Id=%s, Input Value Source Id=%s";

  String EVENTS_AI_INPUT_SRC_DESCR_FORMAT = "AI Pin Id=%s, Input Value Source Id=%s";

  String EVENTS_DI_INPUT_SRC_DESCR_FORMAT = "DI Pin Id=%s, Input Value Source Id=%s";

  String BOOLEAN_VALUES_TRIGGER_DESCR_FORMAT = "Boolean Values Trigger Id=%s";

  String BIT_INDEX_IS_NOT_POINTED_ERR_FORMAT = "For param %s bit index is not pointed";

  String ERR_MSG_EVENT_MODULE_CANT_BE_STARTED_FORMAT =
      "Модуль событий в %s не может быть запущен, так как не сконфигурирован";

  String MSG_EVENTS_MODULE_IS_STARTED_FORMAT = "Работа с событиями в модуле %s стартовала";

  String ERR_MSG_EVENT_MODULE_IS_NOT_STARTED_FORMAT = "Подмодуль событий в %s не стартовал:\n Некорректно указаны пины";

  String ERR_INSTANCE_OF_SOURCE_HAS_NOT_BEEN_CREATED = "Instance %s of Input Value Source has not been created";

  String ERR_PARAM_FOR_SOURCE_HAS_INCORRECT_VALUE =
      "Parameter '%s' for Input Value Source has incorrect value or is absent";

  String ERR_INSTANCE_OF_TRIGGER_HAS_NOT_BEEN_CREATED = "Instance %s of Event Trigger has not been created";

  String ERR_PARAM_FOR_TRIGGER_HAS_INCORRECT_VALUE =
      "Parameter '%s' for Event Trigger has incorrect value or is absent";

  String ERR_INSTANCE_OF_FORMER_HAS_NOT_BEEN_CREATED = "Instance %s of Event Params Former has not been created";

  String ERR_PARAM_FOR_FORMER_HAS_INCORRECT_VALUE =
      "Parameter '%s' for Event Params Former has incorrect value or is absent";

  String ERR_MSG_CANT_CREATE_INSTANCE_SENDER_FORMAT = "Cant create instance of sender %s";

  String ERR_MSG_IF_TAG_HAS_TYPE_INT_THEN_BIT_INDEX_MUST_BE_SETTED_ELSE_TAG_MUST_HAVE_TYPE_BOOLEAN =
      "if tag has type of int, then bit index must be setted, else tag must have type of boolean ";

  String ERR_MSG_IF_BIT_INDEX_IS_SET_THAN_TAG_MUST_HAVE_TYPE_INTEGER =
      "if   bit index is set, than tag must have type of integer ";

  String ERR_MSG_FIELD_VALUE_NOT_SET = "Field value not set";

  String ERR_MSG_FIELD_HAS_EMPTY_OR_WRONG_VALUE_FORMAT = "Field %s has empty or wrong value";

  String ERR_MSG_CANT_CREATE_INSTANCE_CONDITION_FORMAT = "Cant create or config instance of condition %s";

  String ERR_MSG_FIELD_IS_NOT_PRESENTED_IN_CFG_FILE_FORMAT = "Field %s is not presented in config file ";

  String ERR_MSG_DURING_CONFIG_EVENT_SENDER_FORMAT = "Exception during config event sender %s";

  String ERR_MSG_PARAMS_FORMER_SCRIPT_RETURNED_NULL = "params former script for event returned null";

  String ERR_MSG_PARAM_FORMER_SCRIPT_THREW_EXCEPTION = "params former script for event threw exception";

  String ERR_MSG_IS_EVENT_SCRIPT_THREW_EXCEPTION = "isEvent script for event threw exception";

  String ERR_MSG_EVENT_INIT_SCRIPT_THREW_EXCEPTION = "event init script threw exception";

  String ERR_MSG_IS_EVENT_SCRIPT_RETURNED_NULL = "isEvent script for event returned null";

  String ERR_MSG_EVENT_CANT_BE_SENT_DUE_TO_ERROR_FORMAT = "event %s cant be sent due to params forming error";

}

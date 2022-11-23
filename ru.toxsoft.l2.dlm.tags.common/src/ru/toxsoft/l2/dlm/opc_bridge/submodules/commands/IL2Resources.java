package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

/**
 * Локализуемые ресурсы подмодулей загружаемого модуля диспетчеризации ТМ.
 *
 * @author max
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  String ERR_INSTANCE_OF_CMD_EXEC_HAS_NOT_BEEN_CREATED = "Instance %s of Command Executer has not been created";

  String ERR_PARAM_FOR_CMD_EXEC_HAS_INCORRECT_VALUE =
      "Parameter '%s' for Command Executer has incorrect value or is absent";

  String ERR_MSG_COMMAND_MODULE_CANT_BE_STARTED_FORMAT =
      "Модуль команд в %s не может быть запущен, так как не сконфигурирован";

  String MSG_COMMAND_COME_FOR_APPLICATION = "Command %s come for application";

  String ERR_MSG_CANT_CREATE_INSTANCE_COMMAND_EXEC_FORMAT = "Cant create instance of command exec %s";

  String ERR_MSG_FIELD_IS_NOT_PRESENTED_IN_CFG_FILE_FORMAT = "Field %s is not presented in config file ";

  String ERR_MSG_DURING_CONFIG_COMMAND_EXECUTER_FORMAT = "Exception during config command executer %s";

  String ERR_MSG_EXEC_COMMAND_SCRIPT_THREW_EXCEPTION = "execCommand script for command exec threw exception";

  String ERR_MSG_CMD_INIT_SCRIPT_THREW_EXCEPTION = "command init script threw exception";

  String ERR_MSG_DO_JOB_SCRIPT_THREW_EXCEPTION = "doJob script for command exec threw exception";

}

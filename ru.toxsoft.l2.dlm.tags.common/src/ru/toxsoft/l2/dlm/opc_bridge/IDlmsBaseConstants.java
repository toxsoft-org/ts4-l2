package ru.toxsoft.l2.dlm.opc_bridge;

/**
 * Жестко "зашитые" в коде, персистентные константы загружаемого модуля opc-моста.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
public interface IDlmsBaseConstants {

  //
  // -----------------------------------------------------------------------------------------------
  // Константы - имена параметров в файле конфигурации подгружаемого модуля.
  /**
   * Имя параметра пина, определяющего будет ли по этому пину выводится дебаг информация.
   */
  String IS_DEBUG = "is.debug";

  /**
   * Начало блока, отвечающего за конфигурацию данных.
   */
  String DATA_DEFS = "dataDefs";

  /**
   * Имя параметра - является ли данное историческим.
   */
  String IS_HIST = "is.hist";

  /**
   * Имя параметра - является ли данное текущим.
   */
  String IS_CURR = "is.curr";

  /**
   * Имя параметра - является ли данное синхронным.
   */
  String IS_SYNCH = "is.synch";

  /**
   * Имя параметра - интервал обновления синхронного данного (для сервера) в мс.
   */
  String SYNCH_PERIOD = "synch.period";

  /**
   * Начало блока, отвечающего за события.
   */
  String EVENT_DEFS = "eventDefs";

  /**
   * Имя параметра - идентификатор класса.
   */
  String CLASS_ID = "class.id";

  /**
   * Имя параметра - имя объекта.
   */
  String OBJ_NAME = "obj.name";

  /**
   * Имя параметра - идентификатор данного.
   */
  String DATA_ID = "data.id";

  /**
   * Имя параметра - идентификатор инверсионного данного.
   */
  String INV_DATA_ID = "inv.data.id";

  /**
   * Имя параметра - идентификатор события.
   */
  String EVENT_ID = "event.id";

  /**
   * Имя параметра - идентификатор пина.
   */
  String PIN_ID = "pin.id";

  /**
   * Имя параметра - идентификатор атрибута.
   */
  String ATTR_ID = "attr.id";

  /**
   * Имя параметра - длительность сигнала.
   */
  String DURATION_PARAM_NAME = "duration";

  /**
   * Имя параметра - значение сигнала.
   */
  String VALUE_PARAM_NAME = "value";

  /**
   * Имя параметра - id параметра "значение" в системе
   */
  String EVENT_VALUE_PARAM_ID_PARAM_IN_CFG = "value.param.id";

  /**
   * Имя параметра - скрипт, исполняющийся в момент прихода команды.
   */
  String CMD_EXEC_SCRIPT = "cmd.exec.script";

  String CMD_INIT_SCRIPT = "cmd.init.script";

  /**
   * Имя параметра - скрипт, исполняющийся в методе doJob периодически.
   */
  String CMD_DOJOB_SCRIPT = "cmd.dojob.script";

  String EVENT_COND_SCRIPT = "condition.script";

  String EVENT_INIT_COND_SCRIPT = "init.cond.script";

  String EVENT_PARAM_FORMER_SCRIPT = "param.former.script";

  String EVENT_INIT_PARAM_FORMER_SCRIPT = "init.param.former.script";

  String DATA_TRANSMIT_SCRIPT = "data.transmit.script";

  String DATA_INIT_SCRIPT = "data.init.script";

  /**
   * Имя параметра - тип команды.
   */
  String CMD_TYPE = "cmd.type";

  /**
   * Имя параметра - идентификатор команды.
   */
  String CMD_ID = "cmd.id";

  /**
   * Имя параметра - идентификатор команды на OPC сервере.
   */
  String CMD_OPC_ID = "cmd.opc.id";

  /**
   * Имя параметра - идентификатор команды на OPC сервере.
   */
  String CMD_VALUE_PARAM_ID = "value.param.id";

  /**
   * Разделитель имён объектов в списке объектов, предназначенных для выполнения команд (а также в списке команд)
   */
  String LIST_DELIM = ",";

  /**
   * Имя параметра - список идентификаторов команд.
   */
  String CMD_IDS_LIST = "cmd.ids.list";

  /**
   * Имя параметра - список имён объектов..
   */
  String OBJ_NAMES_LIST = "obj.names.list";

  /**
   * Начало блока, отвечающего за конфигурацию команд.
   */
  String CMD_DEFS = "cmdDefs";

  /**
   * Начало блока, отвечающего за конфигурацию команд для регистрации в качестве слушателя.
   */
  String CMD_CLASS_DEFS = "cmdClassDefs";

  /**
   * Начало имени параметра триггера события.
   */
  String EVENT_TRIGGER_START = "event.trigger";

  /**
   * Начало имени параметра типа параметра.
   */
  String EVENT_PARAM_KIND_START = "param.kind";

  /**
   * Начало имени параметра параметра события.
   */
  String EVENT_PARAM_START = "event.param";

  /**
   * Имя параметра таймаута отключения команды.
   */
  String CMD_TIMEOUT = "cmd.timeout";

  /**
   * Имя параметра идентификатора пина обратной связи (отключения команды)
   */
  String FB_PIN_ID_NUMBER = "fb.pin.id.number";

  /**
   * Имя параметра значения пина обратной связи (отключения команды)
   */
  String FB_PIN_VAL_NUMBER = "fb.pin.val.number";

  /**
   * Имя параметра значения пина обратной связи (отключения команды)
   */
  String FB_DURATION = "fb.duration";

  //
  // -----------------------------------------
  // События

  /**
   * Имя параметра массива параметров триггеров и событий
   */
  String EVENTS_PARAMS = "params";

  /**
   * Имя параметра - идентификатор параметра события из системного описания S5
   */
  String EVENT_PARAM_ID = "event.param.id";

  /**
   * Имя параметра - идентификатор тега входного данного
   */
  String TAG_ID = "tag.id";

  /**
   * Имя параметра - идентификатор специального устройства с тегами
   */
  String TAG_DEVICE_ID = "tag.dev.id";

  /**
   * Имя параметра - список условий события в которых участвует тег
   */
  String EVENT_TAG_CONDITIONS = "tag.conditions";

  /**
   * Имя параметра - список формирователей параметров события в которых участвует тег
   */
  String EVENT_TAG_PARAM_FORMERS = "tag.param.formers";

  /**
   * Разделитель условий и формирователей в конфигурации события
   */
  String EVENT_CFG_FIELDS_VALS_DELIM = ";";

  /**
   * Имя параметра - значение параметра, необходимое для события
   */
  String BOOL_EVENT_VALUE = "bool.event.value";

  /**
   * Имя параметра - номер бита (начиная от младшего с нулевого)
   */
  String BIT_INDEX = "bit.index";

  /**
   * Имя параметра - массим триггеров события.
   */
  String EVENT_TRIGGERS = "event.triggers";

  /**
   * Имя параметра - массив формирователей параметров события.
   */
  String EVENT_PARAMS_FORMERS = "event.params.formers";

  /**
   * Значение параметра type - расширенный исполнитель команд.
   */
  String CMD_EXTENDED = "CMD.EXTENDED";

  /**
   * Значение параметра type - исполнитель команд: Инверсный алгоритм Импульсная команда, подающая на пин значение 1,
   * пока значение DI пина-триггера не сменится
   */
  String CMD_FEEDBACK_INVERSE = "CMD.FEEDBACK_INVERSE";

  /**
   * Значение параметра type - исполнитель команд: Импульсная команда, подающая на пин значение 1, пока значение DI
   * пина-триггера не станет 0
   */
  String CMD_FEEDBACK = "CMD.FEEDBACK";

  /**
   * Значение параметра type - исполнитель команд: Команда, подающая на пин указанное в команде значение.
   */
  String CMD_VALUE = "CMD.VALUE";

  /**
   * Значение параметра type - исполнитель команд: Импульсная команда, подающая на пин значение 1 в течение некоторого
   * времени.
   */
  String CMD_PULSE = "CMD.PULSE";

  /**
   * Значение параметра type - расширенный формирователь параметров события.
   */
  String FRM_EXTENDED = "FRM.EXTENDED";

  /**
   * Значение параметра type - обычный формирователь параметров события - значения параметров=значения пинов + тегов.
   */
  String FRM_VALUES = "FRM.VALUES";

  /**
   * Значение параметра type - расширенный триггер события.
   */
  String TRG_EXTENDED = "TRG.EXTENDED";

  /**
   * Значение параметра type - бинарный триггер события.
   */
  String TRG_BOOL_VALUES = "TRG.BOOL.VALUES";

  /**
   * Значение параметра type - расширенный источник данных для триггеров и формирователей параметров.
   */
  String SRC_EXTENDED = "SRC.EXTENDED";

  /**
   * Значение параметра type - источник данных - тег.
   */
  String SRC_TAG_SPEC_DEVICE = "SRC.TAG.SPEC.DEVICE";

  /**
   * Значение параметра type - источник данных - аналоговый пин.
   */
  String SRC_PIN_AI = "SRC.PIN.AI";

  /**
   * Значение параметра type - источник данных - дискретный пин.
   */
  String SRC_PIN_DI = "SRC.PIN.DI";

  /**
   * Значение параметра type - источник данных - значения атрибута объекта.
   */
  String SRC_OBJ_ATTR = "SRC.OBJ.ATTR";

  /**
   * Значение параметра type - источник данных - значения данного объекта.
   */
  String SRC_OBJ_DATA = "SRC.OBJ.DATA";

  /**
   * Имя параметра - тип.
   */
  String TYPE_PARAM = "type";

  /**
   * Имя параметра - java-класс.
   */
  String JAVA_CLASS = "java.class";

  /**
   * Имя параметра - java-класс исполнителя команды.
   */
  String COMMAND_EXEC_JAVA_CLASS = "command.exec.java.class";

  /**
   * Имя параметра - java-класс формирователя события (и его отправки).
   */
  String EVENT_SENDER_JAVA_CLASS = "event.sender.java.class";

  /**
   * Имя параметра - java-класс условия события.
   */
  String CONDITION_JAVA_CLASS = "condition.java.class";

  /**
   * Имя параметра - java-класс формирователя параметров события.
   */
  String PARAM_FORMER_JAVA_CLASS = "param.former.java.class";

  /**
   * Имя параметра - массив данных на выходе передатчика.
   */
  String TRANSMITTER_DATA_ARRAY = "data";

  /**
   * Имя параметра - массив тегов на входе передатчика.
   */
  String TRANSMITTER_TAGS_ARRAY = "tags";

  /**
   * Имя параметра - массив тегов на входе команды.
   */
  String COMMAND_TAGS_ARRAY = "tags";

  /**
   * Имя параметра - массив тегов на входе события.
   */
  String EVENT_TAGS_ARRAY = "tags";

  /**
   * Имя параметра - наличие условия: выключение (переход из 1 в 0 )
   */
  String CONDITION_SWITCH_OFF = "condition.switch.off";

  /**
   * Имя параметра - наличие условия: включение (переход из 0 в 1 )
   */
  String CONDITION_SWITCH_ON = "condition.switch.on";

  /**
   * Имя параметра - минимальное изменение значения, которое считается измененим (в процентах)
   */
  String CONDITION_CHANGE_PERCANT = "condition.change.percant";

  /**
   * Имя параметра - значение тега для выполнения условия.
   */
  String CONDITION_VALUE = "condition.value";

  /**
   * Имя параметра - список названий параметров события из системного описания
   */
  String FORMER_EVENT_PARAMS = "former.event.params";

  /**
   * Имя параметра - формат идентификатора формирователя параметров события.
   */
  String PARAMS_FORMER_PARAMS_FORMAT = "params.former%d.params";

  /**
   * Имя параметра - идентфикатор по умолчанию
   */

  String DEFAULT_ID = "default";

  /**
   * Имя параметра - идентфикатор тега по умолчанию
   */

  String DEFAULT_TAG_ID = "tag";

  /**
   * Имя параметра - формат идентификатора условия события.
   */
  String CONDITION_PARAMS_FORMAT = "condition%d.params";

  String SCRIPT_CURR_TIME_VAR = "curr_time";

  String SCRIPT_CMD_EXEC_TIME_VAR = "cmd_exec_time";

  String SCRIPT_IS_EVENT_VAR = "is_event";

  String SCRIPT_IS_TRANSMITTED_VAR = "is_transmitted";

  String JAVA_SCRIPT_NAME = "JavaScript";

  String DASH = "_";

  String DOT = "\\.";

  String DATA_SETTER_NAME_FOR_SCRIPT_FORMAT = "data%d";

  String TAG_NAME_FOR_SCRIPT_FORMAT = "tag%d";
}

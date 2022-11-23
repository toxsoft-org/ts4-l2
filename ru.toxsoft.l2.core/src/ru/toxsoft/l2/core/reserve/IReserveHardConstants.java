package ru.toxsoft.l2.core.reserve;

/**
 * Жестко "зашитые" в коде, персистентные константы резервирования.
 * 
 * @author max
 */
@SuppressWarnings( "nls" )
public interface IReserveHardConstants {

  /**
   * Признак того, резервирование включено.
   */
  // String L2_RESERVE_ON = "reserve.on";

  /**
   * Имя файла конфигурации резервирования.
   */
  String L2_RESERVE_CFG_FILE_NAME = "cfg/reserve/reserve.cfg";

  /**
   * Период проверки состояния шкафа.
   */
  String L2_RESERVE_UPDATE_STATE_PERIOD = "update.state.period";

  /**
   * Класс шкафа.
   */
  // String L2_RESERVE_BOX_CLASS_ID = "box.class.id";

  /**
   * Объект шкафа.
   */
  String L2_RESERVE_BOX_OBJ_NAME = "box.obj.name";

  /**
   * Данное, обозначающее, что шкаф основной.
   */
  // String L2_RESERVE_DATA_LEADER_ID = "data.leader.id";

  /**
   * Данное, обозначающее, что шкаф основной навсегда.
   */
  // String L2_RESERVE_DATA_LEADER_FOREVER_ID = "data.leader.forever.id";

  /**
   * Данное, обозначающее исправность шкафа.
   */
  // String L2_RESERVE_DATA_FUNC = "data.functionality";

  /**
   * Команда на установку шкафа основным.
   */
  // String L2_RESERVE_CMD_MAIN = "cmd.leader.id";

  /**
   * Команда на установку шкафа резервным.
   */
  // String L2_RESERVE_CMD_RESERVE = "cmd.slave.id";

  /**
   * Команда на установку шкафа основным даже в случае неисправности.
   */
  // String L2_RESERVE_CMD_MAIN_INVALID = "cmd.leader.forever.id";

  /**
   * Команда на отмену шкафа основным навсегда.
   */
  // String L2_RESERVE_CMD_CANCEL_LEADER_FOREVER = "cmd.cancel.leader.forever.id";
}

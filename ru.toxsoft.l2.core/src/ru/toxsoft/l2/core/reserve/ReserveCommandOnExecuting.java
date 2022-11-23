package ru.toxsoft.l2.core.reserve;

/**
 * Команда резервирования, находящаяся на исполнении
 * 
 * @author max
 */
public class ReserveCommandOnExecuting {

  private String cmdId;

  private EReserveCommand commandType;

  /**
   * Конструктор по идентификатору и типу команды резервирования
   * 
   * @param cmdId
   * @param commandType
   */
  public ReserveCommandOnExecuting( String cmdId, EReserveCommand commandType ) {
    super();
    this.cmdId = cmdId;
    this.commandType = commandType;
  }

  public synchronized String getCmdId() {
    return cmdId;
  }

  public synchronized EReserveCommand getCommandType() {
    return commandType;
  }

}

package ru.toxsoft.l2.core.main;

/**
 * Команда завершения работы НУ.
 * 
 * @author max
 */
public interface IProgramQuitCommand {

  /**
   * Возвращает код завершения работы НУ (код возврата программы)
   * 
   * @return int - код возврата программы.
   */
  int programRetCode();

  /**
   * Возвращает сообщение при завершении программы.
   * 
   * @return String - сообщение при завершении программы.
   */
  String message();

}

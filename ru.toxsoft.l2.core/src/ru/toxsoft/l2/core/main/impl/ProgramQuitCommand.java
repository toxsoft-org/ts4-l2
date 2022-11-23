package ru.toxsoft.l2.core.main.impl;

import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.core.main.*;

/**
 * Неизменяемая реализация {@link IProgramQuitCommand}.
 *
 * @author goga
 */
public class ProgramQuitCommand
    implements IProgramQuitCommand {

  private final int    programRetCode;
  private final String message;

  /**
   * Создает объект со всеми инвариантами.
   *
   * @param aRetCode int - код возврата программы
   * @param aMsg String - сообщение при завершении программы
   */
  public ProgramQuitCommand( int aRetCode, String aMsg ) {
    programRetCode = aRetCode;
    message = TsNullArgumentRtException.checkNull( aMsg );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IProgramQuitCommand
  //

  @Override
  public int programRetCode() {
    return programRetCode;
  }

  @Override
  public String message() {
    return message;
  }

}

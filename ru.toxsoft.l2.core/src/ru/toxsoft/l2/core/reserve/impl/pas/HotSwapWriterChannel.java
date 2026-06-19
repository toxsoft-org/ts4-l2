package ru.toxsoft.l2.core.reserve.impl.pas;

import java.net.*;

import org.toxsoft.core.pas.client.*;
import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.core.reserve.impl.*;

/**
 * Канал передачи HotSwap
 *
 * @author mvk
 */
public class HotSwapWriterChannel
    extends PasClientChannel {

  /**
   * Контроллер
   */
  private final PasBoxStateSender controller;

  /**
   * Фабрика каналов
   */
  @SuppressWarnings( "hiding" )
  public static final IPasClientChannelCreator<HotSwapWriterChannel> CREATOR =
      HotSwapWriterChannel::new;

  /**
   * Конструктор.
   *
   * @param aContext {@link ITsContextRo} - контекст выполнения, общий для всех каналов и сервера
   * @param aSocket {@link Socket} сокет соединения
   * @param aHandlerHolder {@link PasHandlerHolder} хранитель обработчиков канала
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException ошибка создания читателя канала
   * @throws TsIllegalArgumentRtException ошибка создания писателя канала
   */
  public HotSwapWriterChannel( ITsContextRo aContext, Socket aSocket,
      PasHandlerHolder<? extends PasClientChannel> aHandlerHolder ) {
    super( aContext, aSocket, aHandlerHolder );
    controller = aContext.get( PasBoxStateSender.class );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов PasClientChannel
  //
  @Override
  protected void doClose() {
    controller.onCloseWriterChannel( this );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}

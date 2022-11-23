package ru.toxsoft.l2.core.reserve.impl.pas;

import java.net.*;

import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.pas.server.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.reserve.impl.*;

/**
 * Канал приема HotSwap
 *
 * @author mvk
 */
public class HotSwapReaderChannel
    extends PasServerChannel {

  /**
   * Контроллер
   */
  private final PasPartnerBoxStateListener controller;

  /**
   * Фабрика каналов
   */
  @SuppressWarnings( "hiding" )
  public static final IPasServerChannelCreator<HotSwapReaderChannel> CREATOR = ( aContext, aSocket, aHandlerHolder, aLogger ) -> new HotSwapReaderChannel( aContext, aSocket, aHandlerHolder, aLogger );

  /**
   * Конструктор.
   *
   * @param aContext {@link ITsContextRo} - контекст выполнения, общий для всех каналов и сервера
   * @param aSocket {@link Socket} сокет соединения
   * @param aHandlerHolder {@link PasHandlerHolder} хранитель обработчиков канала
   * @param aLogger {@link ILogger} журнал работы класса канала
   * @throws TsNullArgumentRtException любой аргумент = <code>null</code>
   * @throws TsIllegalArgumentRtException ошибка создания читателя канала
   * @throws TsIllegalArgumentRtException ошибка создания писателя канала
   */
  public HotSwapReaderChannel( ITsContextRo aContext, Socket aSocket,
      PasHandlerHolder<? extends PasServerChannel> aHandlerHolder, ILogger aLogger ) {
    super( aContext, aSocket, aHandlerHolder, aLogger );
    controller = aContext.get( PasPartnerBoxStateListener.class );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов PasServerChannel
  //
  @Override
  protected void doClose() {
    controller.onCloseReaderChannel( this );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}

package ru.toxsoft.l2.dlm.opc_bridge.submodules.events;

import org.toxsoft.core.tslib.av.avtree.*;

import ru.toxsoft.l2.core.dlm.*;

/**
 * Класс формирования и отправки события на сервер.
 *
 * @author max
 */
public interface IEventSender {

  /**
   * Метод вызывается автоматически и периодически для отправки события, если для его отправки сложились условия.
   *
   * @param aTime long - время.
   */
  void sendEvent( long aTime );

  /**
   * Проводит конфигурацию по конфигурационным параметрам.
   *
   * @param aParams IAvTree - дерево конфигурационных параметров в стандартной форме.
   */
  void config( IAvTree aParams );

  /**
   * Запускает объект формирования и отправки события на сервер..
   *
   * @param aContext IDlmContext - контекст модулей, который может понадобитя для запуска.
   */
  void start( IDlmContext aContext );
}

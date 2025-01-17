package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Преобразователь сигналов с железа на данные сервера.
 *
 * @author dima
 */
public interface ITagable {

  /**
   * @return { {@link ITag } - тег
   */
  ITag tag();

}

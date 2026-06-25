package org.toxsoft.l2.dlm.tags.submodules.events;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Условие возникновения события.
 *
 * @author max
 */
public interface IOpcTagsCondition {

  /**
   * Проводит конфигурацию по конфигурационным параметрам.
   *
   * @param aParams IAvTree - дерево конфигурационных параметров в стандартной форме.
   */
  void config( IAvTree aParams );

  /**
   * Запускает Условие возникновения события.
   *
   * @param aTags IMap - теги драйвера OPC, ключ - идентификатор структуры тега из конфигурационного файла ("default" -
   *          если описание тега находится в корневой секции).
   */
  void start( IMap<String, IL2Tag> aTags );

  /**
   * Возвращает признак того, произошло ли событие или нет.
   *
   * @param aTime long - текущее время.
   * @return true - событие произошло, false - событие не произошло.
   */
  boolean isEventCondition( long aTime );
}

package ge.toxsoft.gwp.opcuabridge.server.events;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;

import ru.toxsoft.l2.thd.opc.*;

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
  void start( IMap<String, ITag> aTags );

  /**
   * Возвращает признак того, произошло ли событие или нет.
   *
   * @param aTime long - текущее время.
   * @return true - событие произошло, false - событие не произошло.
   */
  boolean isEventCondition( long aTime );
}

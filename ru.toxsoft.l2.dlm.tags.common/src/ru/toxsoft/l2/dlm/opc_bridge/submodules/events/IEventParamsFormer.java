package ru.toxsoft.l2.dlm.opc_bridge.submodules.events;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Формирователь параметров события, которые потом отправляются вместе с событием на сервер
 *
 * @author MAX
 */
public interface IEventParamsFormer {

  /**
   * Проводит конфигурацию формирователя параметров события по конфигурационным параметрам.
   *
   * @param aParams IAvTree - дерево конфигурационных параметров в стандартной форме.
   */
  void config( IAvTree aParams );

  /**
   * Запускает формирователь параметров события .
   *
   * @param aTags IMap - теги драйвера OPC, ключ - идентификатор структуры тега из конфигурационного файла ("default" -
   *          если описание тег находится в корневой секции).
   */
  void start( IMap<String, ITag> aTags );

  /**
   * Формирует параметры произошедшего события.
   *
   * @param aTime long - текущее время.
   * @return IStringMap - параметры произошедшего события, ключи - идентификаторы параметров события из системного
   *         описания.
   * @throws TsException - в случае невозможности формирования необходимых параметров.
   */
  IStringMap<IAtomicValue> getEventParamValues( long aTime )
      throws TsException;
}

package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * НУ исполнитель команды
 *
 * @author max
 */
public interface ICommandExec {

  /**
   * Проводит конфигурацию исполнителя команды по конфигурационным параметрам.
   *
   * @param aParams IAvTree - дерево конфигурационных параметров в стандартной форме.
   */
  void config( IAvTree aParams );

  /**
   * Запускает исполнителя команды.
   *
   * @param aTags IMap - теги драйвера OPC, ключ - идентификатор структуры тега из конфигурационного файла ("default" -
   *          если описание тега находится в корневой секции).
   * @param aCommandStateEditor ISkCommandService - редактор состояния выполнения команды.
   */
  void start( IStringMap<ITag> aTags, ISkCommandService aCommandStateEditor );

  /**
   * Исполняет команду. Нужно учитывать, что состояние команды уже изменено на {@link ESkCommandState#EXECUTING} с
   * формулировкой Command come for application
   *
   * @param aCmd ICommand - команда на исполнение.
   * @param aTime long - текущее время.
   */
  void execCommand( IDtoCommand aCmd, long aTime );

  /**
   * Выполняет текущую периодическую работу.
   *
   * @param aTime long - текущее время.
   */
  void doJob( long aTime );

  boolean isBusy();
}

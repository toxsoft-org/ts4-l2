package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.skf.rri.lib.*;

import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.IComplexTag.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Преобразователь сигналов с железа на НСИ атрибуты USkat сервера (OPC UA node -> USkat server RRI Gwid).
 *
 * @author dima
 */
public interface IRriDataTransmitter {

  /**
   * Проводит квант работы - передаёт сигналы с железа на сервер - возможно с неким анализом и преобразованием (например
   * при меандре), в общем случае - данные с N каналов железа передаются в M данных сервера..
   *
   * @param aTime - время передачи (едино для всех преобразователей-передатчиков - если за один квант работы всего
   *          подключаемого модуля)
   * @return boolean - true - данные поменялись и должны быть переданы наверх, false - данные не изменились и их
   *         передача не нужна.
   */
  boolean transmit( long aTime );

  /**
   * Проводит конфигурацию преобразователя передатчика по конфигурационным параметрам.
   *
   * @param aParams IAvTree - дерево конфигурационных параметров в стандартной форме.
   */
  void config( IAvTree aParams );

  /**
   * Запускает преобразователь-передатчик данных.
   *
   * @param aDataSetters - массив установщиков данных
   * @param aTags IList - список тегов, из которых поступают данные, необходимые для преобразования и передачи на
   *          сервер.
   * @param aComplexTag - комплексны тег на запись
   */
  void start( IRriSetter[] aDataSetters, IList<ITag> aTags, IComplexTag aComplexTag );

  /**
   * Карта описания привязок Gwid параметра НСИ -> НСИ секция где он описан
   *
   * @return { @link IMap<Gwid, ISkRriSection> } карта описания привязок
   */
  IMap<Gwid, ISkRriSection> gwid2Section();

  /**
   * Установить значение с сервера USkat на node OPC UA
   */
  void transmitUskat2OPC();

  /**
   * Текущее состояние процесса выполнения команды по записи НСИ значения в OPC UA дерево
   *
   * @return { {@link EComplexTagState} состояние выполнения команды
   */
  EComplexTagState getOpcCmdState();

}

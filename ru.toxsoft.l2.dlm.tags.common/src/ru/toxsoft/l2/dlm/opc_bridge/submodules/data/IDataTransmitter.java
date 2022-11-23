package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Преобразователь сигналов с железа на данные сервера.
 *
 * @author max
 * @param <T> - класс дата-сета на запись.
 */
public interface IDataTransmitter<T extends ISkRtdataChannel> {

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
   * @param aDataGwids IList - список данных в дата-сете, в который передаются данные.
   * @param aTags IList - список тегов, из которых поступают данные, необходимые для преобразования и передачи на
   *          сервер.
   * @param aWriteDataSet T - дата-сет на запись
   */
  void start( IDataSetter[] aDataSetindexes, IList<ITag> aTags, IMap<Gwid, T> aWriteDataSet );
}

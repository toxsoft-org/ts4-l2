package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

import ru.toxsoft.l2.core.dlm.*;

/**
 * Класс инициализации передатчиков данных от железа на сервер, призванный по конфиг информации сопоставить каналу НУ
 * данное системы, наладить эту связь (найти канал и найти данное), зарегистрировать всю необходимую информацию
 * (датасеты)
 *
 * @author max
 * @param <T> - класс датасета.
 */
public interface IDataTransmittersInitializer<T extends ISkRtdataChannel> {

  /**
   * Добавляет конфигурационную информацию для одного передатчика, метод нужно вызывать до вызова метода
   * {@link #initialize(IDlmContext)}, инче ошибка состояния.
   *
   * @param aDataConfig IAvTree - конфигурационная информация для передатчика в виде дерева.
   * @throws TsIllegalStateRtException - выбрасывается если метод вызывается после вызова
   *           {@link #initialize(IDlmContext)} .
   */
  void addDataConfigParamsForTransmitter( IAvTree aDataConfig )
      throws TsIllegalStateRtException;

  /**
   * Метод инициализирующий все связи, вызывется один раз. При повоторном вызове выбрасывается исключение.
   *
   * @param context IDlmContext - контекств, использующийся для инициализации.
   * @throws TsIllegalStateRtException - выбрасывается если метод вызывается повтроно.
   */
  void initialize( IDlmContext context )
      throws TsIllegalStateRtException;

  /**
   * После успешной инициализации возвращает датасет.
   *
   * @return T - датасет.
   * @throws TsIllegalStateRtException - выбрасывается, если метод вызывается до {@link #initialize(IDlmContext)} или
   *           если во время инициализации произошла ошибка (т.е. конфигурация закончилась ошибкой)
   */
  IMap<Gwid, IDataSetter> getDataSetters()
      throws TsIllegalStateRtException;

  /**
   * Возвращает набор передатчиков данных с железа на данные системы.
   *
   * @return IList - набор передатчиков данных.
   * @throws TsIllegalStateRtException - выбрасывается, если метод вызывается до {@link #initialize(IDlmContext)} или
   *           если во время инициализации произошла ошибка (т.е. конфигурация закончилась ошибкой)
   */
  IList<IDataTransmitter<T>> getDataTransmitters()
      throws TsIllegalStateRtException;

  /**
   * Возвращает список идентификаторов устройств, являющихся источниками данных. TODO - логика подсказывает, что лучше
   * разбить gwid на группы по устройствам, но пока - так
   *
   * @return IList<String> - список идентификаторов устройств.
   */
  IList<String> getTagsDevices();

  /**
   * Возвращает список {@link GwidList}, для которых тег является источником данных
   *
   * @param aTagId - id тега источником данных
   * @return {@link GwidList} - список Gwid.
   */
  IGwidList tag2GwidList( String aTagId );
}

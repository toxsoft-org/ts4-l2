package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Абстрактнася фабрика передатчиков, предназначенная для создания передатчиков и для выдачи метаинформации
 * конфигурационной информации.
 *
 * @author max
 * @param <T> - класс дата-сета.
 */
public abstract class AbstractTransmitterFactory<T extends ISkRtdataChannel> {

  private static IAvTreeInfo configInfo;

  /**
   * Открытый конструктор - для создания методами рефлексии.
   */
  public AbstractTransmitterFactory() {
    // nop
  }

  /**
   * Устанавливает описание конфигурационной информации.
   *
   * @param aCfgInfo IAvTreeInfo - описание конфигурационной информации.
   */
  protected void setConfigInfo( IAvTreeInfo aCfgInfo ) {
    configInfo = aCfgInfo;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Создаёт передатчик.
   *
   * @param aConfig IAvTree - конфигурационная информация.
   * @return IDataTransmitter - передатчик.
   */
  public abstract IDataTransmitter<T> createTransmitter( IAvTree aConfig );

  /**
   * Возвращает метаинформацию (описание) конфигурационной информации, необходимой для инициализации передатчика.
   *
   * @return IAvTreeInfo - метаинформация (описание) конфигурационной информации, необходимой для инициализации
   *         передатчика.
   */
  public IAvTreeInfo getConfigInfo() {
    return configInfo;
  }

}

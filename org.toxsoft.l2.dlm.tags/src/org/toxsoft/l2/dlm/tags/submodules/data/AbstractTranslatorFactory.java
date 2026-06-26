package org.toxsoft.l2.dlm.tags.submodules.data;

import org.toxsoft.core.tslib.av.avtree.*;

/**
 * TODO это на самом деле #IL2GwidTranslatorFactory <br>
 * Абстрактная фабрика передатчиков, предназначенная для создания передатчиков и для выдачи метаинформации
 * конфигурационной информации.
 *
 * @author max
 */
public abstract class AbstractTranslatorFactory {

  private static IAvTreeInfo configInfo;

  /**
   * Открытый конструктор - для создания методами рефлексии.
   */
  public AbstractTranslatorFactory() {
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
  public abstract IDataGwidTranslator createTransmitter( IAvTree aConfig );

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

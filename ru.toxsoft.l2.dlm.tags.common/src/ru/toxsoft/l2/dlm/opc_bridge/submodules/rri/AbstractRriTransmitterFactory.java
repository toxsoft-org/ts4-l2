package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.tslib.av.avtree.*;

/**
 * Абстрактнася фабрика передатчиков, предназначенная для создания передатчиков и для выдачи метаинформации
 * конфигурационной информации.
 *
 * @author dima
 */
public abstract class AbstractRriTransmitterFactory {

  private static IAvTreeInfo configInfo;

  /**
   * Открытый конструктор - для создания методами рефлексии.
   */
  public AbstractRriTransmitterFactory() {
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
  public abstract IRriDataTransmitter createTransmitter( IAvTree aConfig );

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

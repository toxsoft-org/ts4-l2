package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;

/**
 * Монитор статуса НСИ OPC UA сервер.<br>
 * Его задача "поймать" переход флага "статус НСИ" в ноль и начать процесс выгрузки НСИ с USkat сервера. Кроме этого
 * дает возможность внешнему коду установить/сбросить флаг статуса НСИ.
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
public interface IStatusRriMonitor {

  /**
   * TODO найти правильное место <br>
   * id device где node статуса НСИ.
   */
  String RRI_STATUS_DEVICE_ID = "status.rri.tag.dev.id";

  /**
   * node id чтения статуса НСИ.
   */
  String RRI_STATUS_READ_NODE_ID = "status.rri.read.tag.id";

  /**
   * аргумент aAddress в IComplexTag::setValue( int aAddress, IAtomicValue aValue ) для установки статуса НСИ.
   */
  String RRI_STATUS_CMD_SET_ID = "status.rri.cmd.set.id";

  /**
   * аргумент aAddress в IComplexTag::setValue( int aAddress, IAtomicValue aValue ) для сброса статуса НСИ.
   */
  String RRI_STATUS_CMD_RESET_ID = "status.rri.cmd.reset.id";

  /**
   * Complex node id записи статуса НСИ.
   */
  String RRI_STATUS_COMPLEX_NODE_ID = "status.rri.complex.tag.id";

  /**
   * Проводит конфигурацию монитора по конфигурационным параметрам.
   *
   * @param aParams IAvTree - дерево конфигурационных параметров в стандартной форме.
   */
  void config( IAvTree aParams );

  /**
   * Запускает монитор.
   *
   * @param aContext IDlmContext - контекст модулей, необходимый для получения тегов.
   * @param aComplexTagsContainer - контейнер комплексных тегов
   * @param aPinRriDataTransmitters - список передечтчиков значений атрибутов НСИ
   */
  void start( IDlmContext aContext, IComplexTagsContainer aComplexTagsContainer,
      IList<IRriDataTransmitter> aPinRriDataTransmitters );

  /**
   * Устанавливает значение тега статуса НСИ контроллера
   */
  void setStatus();

  /**
   * Сбрасывает значение тега статуса НСИ контроллера
   */
  void resetStatus();

  /**
   * Начинает процесс загрузки НСИ с верхнего уровня
   */
  void startDownload();

  /**
   * Осуществляет процесс загрузки НСИ с верхнего уровня
   */
  void processDownload();

  /**
   * Возвращает текущий статус состояния НСИ на контроллере.
   *
   * @return ERriControllerState - текущее состояние НСИ на контроллере, {@link ERriControllerState#UNKNOWN} - в случае
   *         отсутствия информации о статусе НСИ контроллера.
   */
  ERriControllerState getState();

  /**
   * Состояние НСИ контроллера
   *
   * @author dima
   */
  public enum ERriControllerState {
    /**
     * контроллер просит загрузить НСИ с сервера USkat
     */
    NEED_DOWNLOAD_USKAT_RRI,

    /**
     * идет процесс загрузки НСИ с сервера USkat
     */
    USKAT_RRI_LOADING,

    /**
     * НСИ контроллера в норме
     */
    RRI_CONTROLLER_OK,

    /**
     * состояние не определено
     */
    UNKNOWN
  }
}

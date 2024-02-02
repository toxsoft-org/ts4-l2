package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.tslib.av.avtree.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;

/**
 * Монитор статуса НСИ OPC UA сервер.<br>
 * Его задача "поймать" переход флага "статус НСИ" в ноль и начать процесс выгрузки НСИ с USkat сервера. Кроме этого
 * дает возможность внешнему коду установить/сбросить флаг статуса НСИ.
 *
 * @author dima
 */
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
   * аргумент aAddress в IComplexTag::setValue( int aAddress, IAtomicValue aValue ) для смены статуса НСИ.
   */
  String RRI_STATUS_CMD_ID = "status.rri.cmd.opc.id";

  /**
   * Complex node id записи статуса НСИ.
   */
  String RRI_STATUS_WRITE_NODE_ID = "status.rri.write.tag.id";

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
   * @param aComplexTagsContainer
   */
  void start( IDlmContext aContext, IComplexTagsContainer aComplexTagsContainer );

  /**
   * Устанавливает значение тега статуса НСИ контроллера
   *
   * @param aValue Integer - устанавливаемое значение, где 0 - "загрузи НСИ с USkat сервер", 1 - нормальные значения НСИ
   *          на контроллере
   */
  void setStatus( Integer aValue );

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

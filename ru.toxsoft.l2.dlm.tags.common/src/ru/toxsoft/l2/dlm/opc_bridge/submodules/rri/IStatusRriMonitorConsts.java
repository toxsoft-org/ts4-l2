package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

/**
 * Константы для процесса мониторинга и загрузки НСИ <br>
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
public interface IStatusRriMonitorConsts {

  /**
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

}

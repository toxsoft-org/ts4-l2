package ru.toxsoft.l2.core.reserve.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static ru.toxsoft.l2.core.reserve.impl.ISiResources.*;

import org.toxsoft.core.pas.server.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.utils.*;

import ru.toxsoft.l2.core.reserve.*;

/**
 * Конфигурационные параметры резервирования.
 *
 * @author mvk
 */
public interface IReserveParams
    extends IPasServerParams {

  // ------------------------------------------------------------------------------------
  // Конфигурационные параметры HotSwap
  //
  /**
   * Идентификатор локального узла HotSwap.
   */
  IDataDef OP_LOCAL_ID = create( "LocalID", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_LOCAL_ID, TSID_DESCRIPTION, STR_D_LOCAL_ID, TSID_DEFAULT_VALUE, avStr( "hotswap-node01" ) ); //$NON-NLS-1$

  /**
   * Вес локального узла HotSwap при выборе главного узла. Чем больше вес, тем больше вероятности стать главным узлом
   */
  IDataDef OP_LOCAL_WEIGHT = create( "LocalWeight", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_WEIGHT, TSID_DESCRIPTION, STR_D_WEIGHT, TSID_DEFAULT_VALUE, avInt( 0 ) );

  /**
   * IP-адрес или сетевое имя локального узла HotSwap.
   */
  IDataDef OP_LOCAL_ADDRESS = create( "LocalAddress", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_LOCAL_ADDRESS, TSID_DESCRIPTION, STR_D_LOCAL_ADDRESS, TSID_DEFAULT_VALUE, avStr( "127.0.0.1" ) ); //$NON-NLS-1$

  /**
   * Номер порта локального узла HotSwap.
   */
  IDataDef OP_LOCAL_PORT = create( "LocalPort", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_LOCAL_ADDRESS, TSID_DESCRIPTION, STR_D_LOCAL_ADDRESS, TSID_DEFAULT_VALUE, avInt( 15751 ) );

  /**
   * IP-адрес или сетевое имя удаленного узла HotSwap.
   */
  IDataDef OP_REMOTE_ADDRESS = create( "RemoteAddress", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_LOCAL_ADDRESS, TSID_DESCRIPTION, STR_D_REMOTE_ADDRESS, TSID_DEFAULT_VALUE,
      avStr( "127.0.0.1" ) ); //$NON-NLS-1$

  /**
   * Номер порта удаленного узла HotSwap.
   */
  IDataDef OP_REMOTE_PORT = create( "RemotePort", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_LOCAL_ADDRESS, TSID_DESCRIPTION, STR_D_REMOTE_ADDRESS, TSID_DEFAULT_VALUE, avInt( 15751 ) );

  /**
   * Тайм-аут [мс] получения сообщений от шкафа напарника, после превышения которого состояние напрника считается
   * неопределённым
   */
  IDataDef OP_PARTNER_MESSAGES_TIMEOUT = create( "PartnerTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_PARTNER_TIMEOUT, TSID_DESCRIPTION, STR_D_PARTNER_TIMEOUT, TSID_DEFAULT_VALUE, avInt( 3000 ) );

  /**
   * Промежуток [мс] между попытками зарегистрировать pas сервер на прослушивание
   */
  IDataDef OP_PAS_SERVER_REG_TRY_PERIOD = create( "PasServerRegTryPeriod", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_PAS_SERVER_REG_TRY_PERIOD, TSID_DESCRIPTION, STR_D_PAS_SERVER_REG_TRY_PERIOD, TSID_DEFAULT_VALUE,
      avInt( 3000 ) );

  //
  // -------------------------------------
  // Идентификатор метода обмена сообщениями

  /**
   * Вызов метода
   */
  String NODE_STATE_METHOD = "nodeState"; //$NON-NLS-1$

  /**
   * Текущее состояние узла {@link EPartnerBoxReserveState}
   */
  String STATE = "state"; //$NON-NLS-1$

  /**
   * Тайм-аут [мс] останова подгружаемых модулей.
   */
  IDataDef OP_DLMS_STOP_TIMEOUT = create( "DlmsStopTimeout", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_DLMS_STOP_TIMEOUT, TSID_DESCRIPTION, STR_D_DLMS_STOP_TIMEOUT, TSID_DEFAULT_VALUE,
      avInt( 5000 ) );

  /**
   * Интервал запуска системы резервирования, в течение которого алгоритм ещё стоит, а обмен уже работает
   */
  IDataDef OP_RESERVE_START_INTERVAL = create( "ReserveStartInterval", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_RESERVE_START_INTERVAL, TSID_DESCRIPTION, STR_D_RESERVE_START_INTERVAL, TSID_DEFAULT_VALUE,
      avInt( 500 ) );

  /**
   * Интервал после запуска, по истечении которого шкаф может перейти в состояние Главный
   */
  IDataDef OP_BECOME_MAIN_INTERVAL = create( "BecomeMainInterval", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_BECOME_MAIN_INTERVAL, TSID_DESCRIPTION, STR_D_BECOME_MAIN_INTERVAL, TSID_DEFAULT_VALUE,
      avInt( 5000 ) );

  /**
   * Признак того, что шкаф является единственным в системе.
   */
  IDataDef OP_IS_ONLY_BOX = create( "IsTheOnlyBox", BOOLEAN, //$NON-NLS-1$
      TSID_NAME, STR_N_IS_ONLY_BOX, TSID_DESCRIPTION, STR_D_IS_ONLY_BOX, TSID_DEFAULT_VALUE, avBool( false ) );

  /**
   * Идентификатор выходного пина, сигнализирующего что шкаф главный.
   */
  IDataDef OP_MAIN_CONTROL_PIN = create( "main.control.pin", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_MAIN_CONTROL_PIN, TSID_DESCRIPTION, STR_D_MAIN_CONTROL_PIN, TSID_DEFAULT_VALUE,
      avStr( TsLibUtils.EMPTY_STRING ) );

  /**
   * Идентификатор параметра - идентификатор входного пина с тремя состояниями (меандер), принимающего состояние
   * шкафа-партнёра.
   */
  IDataDef OP_RESERVE_MEANDER_PIN = create( "reserve.meander.pin", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_RESERVE_MEANDER_PIN, TSID_DESCRIPTION, STR_D_RESERVE_MEANDER_PIN, TSID_DEFAULT_VALUE,
      avStr( TsLibUtils.EMPTY_STRING ) );

  /**
   * Идентификатор параметра - пол периода меандра входного пина с тремя состояниями (меандер), принимающего состояние
   * шкафа-партнёра.
   */
  IDataDef OP_RESERVE_MEANDER_HALH_PERIOD = create( "reserve.meander.half.period", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_RESERVE_MEANDER_HALH_PERIOD, TSID_DESCRIPTION, STR_D_RESERVE_MEANDER_HALH_PERIOD,
      TSID_DEFAULT_VALUE, avInt( 250 ) );

  /**
   * Идентификатор параметра - минимальное количество тактов на пол периода меандра входного пина с тремя состояниями
   * (меандер), принимающего состояние шкафа-партнёра.
   */
  IDataDef OP_RESERVE_MEANDER_MIN_COUNT = create( "reserve.meander.min.count", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_RESERVE_MEANDER_MIN_COUNT, TSID_DESCRIPTION, STR_D_RESERVE_MEANDER_MIN_COUNT, TSID_DEFAULT_VALUE,
      avInt( 3 ) );

  /**
   * Интервал между попытками регистрации команд резервирования
   */
  IDataDef OP_RSRV_CMD_REREG_INTERVAL = create( "cmdRsrvReregInterval", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_RSRV_CMD_REREG_INTERVAL, TSID_DESCRIPTION, STR_D_RSRV_CMD_REREG_INTERVAL, TSID_DEFAULT_VALUE,
      avInt( 3000 ) );

}

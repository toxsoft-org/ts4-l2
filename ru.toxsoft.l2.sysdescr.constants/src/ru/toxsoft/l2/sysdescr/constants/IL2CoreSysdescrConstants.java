package ru.toxsoft.l2.sysdescr.constants;

/**
 * Константы системного описания, используемые в ядре НУ
 *
 * @author max
 */
public class IL2CoreSysdescrConstants {

  /**
   * Класс объекта резрвирования
   */
  public static String CLSID_L2_HOTSWAP = ""; //$NON-NLS-1$

  /**
   * Шкаф на котором работает алгоритм
   */
  public static String LNKID_CLSID_L2_HOTSWAP_BOX = ""; //$NON-NLS-1$

  /**
   * Состояние алгоритма
   */
  public static String RTDID_CLSID_L2_HOTSWAP_STATE = ""; //$NON-NLS-1$

  /**
   * Флаг болезни
   */
  public static String RTDID_CLSID_L2_HOTSWAP_SICK = ""; //$NON-NLS-1$

  /**
   * Флаг связи
   */
  public static String RTDID_CLSID_L2_HOTSWAP_CONNECTION = ""; //$NON-NLS-1$

  /**
   * Состояние партнера
   */
  public static String RTDID_CLSID_L2_HOTSWAP_PARTNER = ""; //$NON-NLS-1$

  /**
   * Переключение в резервный
   */
  public static String RTDID_CLSID_L2_HOTSWAP_TRANSITIONRESERV = ""; //$NON-NLS-1$

  /**
   * Переключение в главный
   */
  public static String RTDID_CLSID_L2_HOTSWAP_TRANSITIONMAIN = ""; //$NON-NLS-1$

  /**
   * true когда имеет статус "основной", false - когда имеет статус "резервный"
   */
  public static String RTDID_CLSID_L2_HOTSWAP_IS_MAIN = ""; //$NON-NLS-1$

  /**
   * true когда нет никаких ошибок, false - когда есть какие-либо ошибки
   */
  public static String RTDID_CLSID_L2_HOTSWAP_IS_OK = ""; //$NON-NLS-1$

  /**
   * Перейти в состояние «Основной»
   */
  public static String CMDID_CLSID_L2_HOTSWAP_CMDMAIN = ""; //$NON-NLS-1$

  /**
   * Перейти в состояние «Основной инвалид»
   */
  public static String CMDID_CLSID_L2_HOTSWAP_CMDMAININVALID = ""; //$NON-NLS-1$

  /**
   * Перейти в состояние «Резерв»
   */
  public static String CMDID_CLSID_L2_HOTSWAP_CMDRESERVE = ""; //$NON-NLS-1$

  /**
   * Состояние изменилось
   */
  public static String EVEID_CLSID_L2_HOTSWAP_STATECHANGED = ""; //$NON-NLS-1$

  /**
   * было
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_OLDSTATE = ""; //$NON-NLS-1$

  /**
   * стало
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_NEWSTATE = ""; //$NON-NLS-1$

  /**
   * причина
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_EVPID_REASON = ""; //$NON-NLS-1$

  /**
   * Не могу изменить состояние
   */
  public static String EVEID_CLSID_L2_HOTSWAP_CANTCHANGE = ""; //$NON-NLS-1$

  /**
   * Текущее состояние
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_STATE = ""; //$NON-NLS-1$

  /**
   * причина
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_REASON = ""; //$NON-NLS-1$

  /**
   * Подтвердить переход в состояние главного
   */
  public static String EVEID_CLSID_L2_HOTSWAP_CONFIRMMAIN = ""; //$NON-NLS-1$

  /**
   * флаг болезни
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_SICK = ""; //$NON-NLS-1$

  /**
   * состояние партнера
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_PARTNERSTATE = ""; //$NON-NLS-1$

  /**
   * Связь с локальным сервером
   */
  public static String EVEID_CLSID_L2_HOTSWAP_CONNECTION = ""; //$NON-NLS-1$

  /**
   * соединение пропало
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_LOST = ""; //$NON-NLS-1$

  /**
   * Отсутствие главного в системе
   */
  public static String EVEID_CLSID_L2_HOTSWAP_NOMAIN = ""; //$NON-NLS-1$

  /**
   * Состояние партнера стало неизвестным
   */
  public static String EVEID_CLSID_L2_HOTSWAP_PARTNERUNDEF = ""; //$NON-NLS-1$

  /**
   * Стал больным
   */
  public static String EVEID_CLSID_L2_HOTSWAP_BECOMESICK = ""; //$NON-NLS-1$

  /**
   * причина
   */
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_REASON = ""; //$NON-NLS-1$
}

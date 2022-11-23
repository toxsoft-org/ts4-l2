package ru.toxsoft.l2.sysdescr.constants;

/**
 * Константы системного описания, используемые в ядре НУ
 * 
 * @author max
 */
@SuppressWarnings( { "nls", "javadoc" } )
public class IL2CoreSysdescrConstants {

  public static String CLSID_L2_HOTSWAP = ""; //
  public static String LNKID_CLSID_L2_HOTSWAP_BOX = ""; // Шкаф на котором работает алгоритм
  public static String RTDID_CLSID_L2_HOTSWAP_STATE = ""; // состояние алгоритма
  public static String RTDID_CLSID_L2_HOTSWAP_SICK = ""; // Флаг болезни
  public static String RTDID_CLSID_L2_HOTSWAP_CONNECTION = ""; // Флаг связи
  public static String RTDID_CLSID_L2_HOTSWAP_PARTNER = ""; // состояние партнера
  public static String RTDID_CLSID_L2_HOTSWAP_IS_MAIN = ""; // true когда имеет статус "основной", false - когда имеет
                                                            // статус "резервный"
  public static String RTDID_CLSID_L2_HOTSWAP_IS_OK = ""; // true когда нет никаких ошибок, false - когда есть
                                                          // какие-либо ошибки
  public static String CMDID_CLSID_L2_HOTSWAP_CMDMAIN = ""; // Перейти в состояние «Основной»
  public static String CMDID_CLSID_L2_HOTSWAP_CMDMAININVALID = ""; // Перейти в состояние «Основной инвалид»
  public static String CMDID_CLSID_L2_HOTSWAP_CMDRESERVE = ""; // Перейти в состояние «Резерв»
  public static String EVEID_CLSID_L2_HOTSWAP_STATECHANGED = ""; // Состояние изменилось
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_OLDSTATE = ""; // было
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_NEWSTATE = ""; // стало
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_EVPID_REASON = ""; // причина
  public static String EVEID_CLSID_L2_HOTSWAP_CANTCHANGE = ""; // Не могу изменить состояние
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_STATE = ""; // Текущее состояние
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_REASON = ""; // причина
  public static String EVEID_CLSID_L2_HOTSWAP_CONFIRMMAIN = ""; // Не могу изменить состояние
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_SICK = ""; // флаг болезни
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_PARTNERSTATE = ""; // состояние партнера
  public static String EVEID_CLSID_L2_HOTSWAP_CONNECTION = ""; // Связь с локальным сервером
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_LOST = ""; // соединение пропало
  public static String EVEID_CLSID_L2_HOTSWAP_NOMAIN = ""; // Отсутствие главного в системе
  public static String EVEID_CLSID_L2_HOTSWAP_PARTNERUNDEF = ""; // Состояние партнера стало неизвестным
  public static String EVEID_CLSID_L2_HOTSWAP_BECOMESICK = ""; // Стал больным
  public static String EVEID_CLSID_L2_HOTSWAP_EVPID_REASON = ""; // причина
}

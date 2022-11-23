package ru.toxsoft.l2.core.reserve;

import static ru.toxsoft.l2.core.reserve.IL2Resources.*;
import static ru.toxsoft.l2.sysdescr.constants.IL2CoreSysdescrConstants.*;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;

/**
 * События отправляемые наверх
 *
 * @author max
 */
public enum EReserveEvent {

  // Все параметры сущностей сервера из файла, сгенерированного skide
  /**
   * Состояние изменилось
   */
  STATE_CHANGED( EVEID_CLSID_L2_HOTSWAP_STATECHANGED, EVENT_VIS_NAME_STATE_CHANGED,
      new StringArrayList( EVEID_CLSID_L2_HOTSWAP_EVPID_OLDSTATE, EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_NEWSTATE,
          EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_EVPID_REASON ) ),

  /**
   * Связь с локальным сервером
   */
  CONNECTION_BREAK( EVEID_CLSID_L2_HOTSWAP_CONNECTION, EVENT_VIS_NAME_LOST_CONN,
      new StringArrayList( EVEID_CLSID_L2_HOTSWAP_EVPID_LOST ) ),

  /**
   * Стал больным
   */
  BECOME_SICK( EVEID_CLSID_L2_HOTSWAP_BECOMESICK, EVENT_VIS_NAME_CRITICAL_MALFUNC,
      new StringArrayList( EVEID_CLSID_L2_HOTSWAP_EVPID_REASON ) ),
  /**
   * Не могу изменить состояние
   */
  CANT_CHANGE( EVEID_CLSID_L2_HOTSWAP_CANTCHANGE, EVENT_VIS_NAME_CANT_CHANGE_STATE,
      new StringArrayList( EVEID_CLSID_L2_HOTSWAP_EVPID_STATE, EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_REASON ) ),

  /**
   * Подтвердить переход в состояние главного
   */
  CONFIRM_MAIN( EVEID_CLSID_L2_HOTSWAP_CONFIRMMAIN, EVENT_VIS_NAME_CONFIRM_MAIN,
      new StringArrayList( EVEID_CLSID_L2_HOTSWAP_EVPID_SICK, EVEID_CLSID_L2_HOTSWAP_EVPID_EVPID_PARTNERSTATE ) ),

  /**
   * Отсутствие главного в системе
   */
  NO_MAIN( EVEID_CLSID_L2_HOTSWAP_NOMAIN, EVENT_VIS_NAME_MAIN_ABSENT ),

  /**
   * Состояние партнера стало неизвестным
   */
  PARTNER_UNDEF( EVEID_CLSID_L2_HOTSWAP_PARTNERUNDEF, EVENT_VIS_NAME_PARTNER_UNKNOWN_STATE );

  private String id;

  private String name;

  private IStringList paramsIds;

  /**
   * Конструктор по идентификатору события (в системе), имени события и списку идентификаторов параметров события.
   *
   * @param aId String - идентификатор события в системе,
   * @param aName String - имя события,
   * @param aParamsIds IStringList - список идентификаторов параметров события.
   */
  EReserveEvent( String aId, String aName, IStringList aParamsIds ) {
    this.id = aId;
    this.name = aName;
    paramsIds = aParamsIds;
  }

  /**
   * Конструктор по идентификатору события (в системе) и имени события.
   *
   * @param aId String - идентификатор события в системе,
   * @param aName String - имя события,
   */
  EReserveEvent( String id, String name ) {
    this( id, name, new StringArrayList() );
  }

  /**
   * Возвращает идентификатор события
   *
   * @return String - идентификатор события
   */
  public synchronized String getId() {
    return id;
  }

  /**
   * Возвращает отображаемое имя события
   *
   * @return String - отображаемое имя события
   */
  public synchronized String getName() {
    return name;
  }

  /**
   * Возвращает список параметров события
   *
   * @return IStringList - список параметров события
   */
  public synchronized IStringList getParamsIds() {
    return paramsIds;
  }

}

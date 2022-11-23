package ru.toxsoft.l2.core.reserve;

/**
 * Локализуемые ресурсы реализации работы с резервированием.
 *
 * @author max
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  String EVENT_VIS_NAME_MAIN_ABSENT = "Предположение об отсутствии главного";

  String EVENT_VIS_NAME_CONFIRM_MAIN = "Требование подтвердить в состояние главного";

  String EVENT_VIS_NAME_CANT_CHANGE_STATE = "Невозможность смены состояния";

  String EVENT_VIS_NAME_CRITICAL_MALFUNC = "Критическая потеря функциональности";

  String EVENT_VIS_NAME_LOST_CONN = "Потеря/восстановление связи с локальным сервером";

  String EVENT_VIS_NAME_STATE_CHANGED = "Состояние изменилось";

  String EVENT_VIS_NAME_PARTNER_UNKNOWN_STATE = "Неизвествное состояние напарника";
}

package ru.toxsoft.l2.thd.opc;

/**
 * Локализуемы ресурсы.
 *
 * @author max
 * @author dima
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  /**
   * Тип тега "только на чтение".
   */
  String E_READ_ONLY = "Tag read only";

  /**
   * Тип тега "только на запись".
   */
  String E_WRITE_ONLY = "Tag write only";

  /**
   * Тип тега "на чтение и запись".
   */
  String E_READ_WRITE = "Tag read and write";

  /**
   * unknown state of health (just created)
   */
  String E_HEALTH_UNKNOWN = "unknown state of health (just created)";

  /**
   * tag is working
   */
  String E_HEALTH_OK = "tag is working";

  /**
   * tag is just broken
   */
  String E_JUST_BROKEN = "tag is just broken";

  /**
   * tag is just recovered
   */
  String E_JUST_RECOVERED = "tag is just recovered";

  /**
   * tag is dead
   */
  String E_DEAD = "tag is dead";

}

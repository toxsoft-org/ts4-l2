package ru.toxsoft.l2.thd.opc;

/**
 * Локализуемы ресурсы.
 *
 * @author max
 */
@SuppressWarnings( "nls" )
interface IL2Resources {

  /**
   * Тип тега "только на чтение".
   */
  String E_READ_ONLY  = "Tag read only";
  /**
   * Тип тега "только на запись".
   */
  String E_WRITE_ONLY = "Tag write only";
  /**
   * Тип тега "на чтение и запись".
   */
  String E_READ_WRITE = "Tag read and write";
}

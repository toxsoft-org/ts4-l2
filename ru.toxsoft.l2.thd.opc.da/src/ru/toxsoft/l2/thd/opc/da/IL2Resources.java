package ru.toxsoft.l2.thd.opc.da;

/**
 * Локализуемы ресурсы.
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
public interface IL2Resources {

  String ERROR_TRY_SET_READ_ONLY_TAG = "Try to set read only OPC tag";
  String ERROR_EXPECTED_TAG_TYPE     = "Expected ru.toxsoft.l2.core.hal.devices.impl.opc.impl.Tag type";
  String CANT_CONNECT_OPC            = "Can't connect OPC. Error: %s";
  String ERROR_ADD_SYNC_TAGS         = "Error add sync tags";
  String ERR_ADD_SYNC_GROUP          = "Error add sync OPC group";
  String ERR_ADD_ASYNC_GROUP         = "Error add async OPC group";
  String ERR_ADD_OUTPUT_GROUP        = "Error add output OPC group";
  String ERROR_ADD_ASYNC_TAGS        = "Error add async tags";
  String CANT_ADD_SYNC_TAG           = "Can't add sync OPC tag: %s";
  String CANT_ADD_ASYNC_TAG          = "Can't add async OPC tag: %s";
  String CANT_ADD_OUTPUT_TAG         = "Can't add output OPC tag: %s";
  String ERROR_ADD_OUTPUT_TAGS       = "Error add output tags";
  String ERROR_OPENSCADA_INIT        = "Error openSCADA init. Error message: %s";
  String UNKNOWN_TAG_VALUE_TYPE      = "Unknown openSCADA tag value type. Type : %d";
  String UNKNOWN_BRIDGE_TYPE         = "Unknown OPC2S5 bridge type: %s";
  String TAG_READ_FAIL               = "Tag id: %s fail to read";
  String TAG_NULL_VAL                = "Tag id: %s has NULL value";
  String TAG_VAL_INCORRECT_TYPE      = "Tag id: %s. Expected value type - %s, fact type -%d";

  /**
   * Тег на запись дублируется
   */
  String ERR_WRITE_TAG_DUPLICATE = "Write tag  %s duplicated";
  String AUTO_RECONNECT_STATE    = "Auto reconnect state: %s";
  String ERR_OPC_TRY_DISCONNECT  = "Try to disconnect OPC server";
  String ERR_OPC_TRY_RECONNECT   = "Try to reconnect OPC server";
}

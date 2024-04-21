package org.toxsoft.l2.thd.modbus.common;

/**
 * Константы универсального modbus драйвера.
 *
 * @author Max
 */
@SuppressWarnings( { "nls", "javadoc" } )
public interface IModbusThdConstants {

  String DEVICES_PARAM_ID = "devices";

  String TAGS_PARAM_ID = "tags";

  String DEV_ADDRESS_PARAM_ID = "dev.address";

  String ID_PARAM_ID = "id";

  String REGISTER_PARAM_ID = "register";

  String REQUEST_TYPE_PARAM_ID = "request.type";

  String IS_OUTPUT_PARAM_ID = "is.output";

  String WORDS_COUNT_PARAM_ID = "words.count";

  String TRANSLATOR_PARAMS_PARAM_ID = "translator.params";

  String TRANSLATOR_PARAM_ID = "translator";

  String MODBUS_TYPE_PARAM_ID = "type";

  String MODBUS_TIMEOUT_PARAM_ID = "modbus.timeout";

  String PARITY_PARAM_ID = "parity";

  String BAUD_RATE_PARAM_ID = "baud.rate";

  String PORT_NAME_PARAM_ID = "port.name";

  String CONNECTIONS_PARAM_ID = "connections";

  String PORT_PARAM_ID = "port";

  String IP_PARAM_ID = "ip";

  //
  // ----------------------------------------------------
  // ETransactionCreators

  String RTU_TRANSACTION_CREATOR_ID = "rtu";

  String RTU_TRANSACTION_CREATOR_DESCR = "Creator of rtu transactions";

  String TCP_TRANSACTION_CREATOR_ID = "tcp";

  String TCP_TRANSACTION_CREATOR_DESCR = "Creator of tcp transactions";
}

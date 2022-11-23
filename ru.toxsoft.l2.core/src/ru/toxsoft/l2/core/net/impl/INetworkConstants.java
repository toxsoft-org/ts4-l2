package ru.toxsoft.l2.core.net.impl;

/**
 * Константы реализации работы с сетью.
 *
 * @author max
 */
@SuppressWarnings( "nls" )
public interface INetworkConstants {

  //
  // -----------------------------------------------------------------------------------------------
  // Константы - имена параметров в файле конфигурации сети.
  /**
   * Адрес сервера.
   */
  String CGF_PARAM_HOST = "host";

  /**
   * Порт сервера.
   */
  String CGF_PARAM_PORT = "port";

  /**
   * Имя класса инициализатора приложение-специфичных служб
   */
  String CGF_PARAM_API_INITIALIZER_NAME = "apiInitializerName";

  /**
   * Пароль соединения с сервером.
   */
  String CGF_PARAM_PASSWORD = "password";

  /**
   * Логин соединения с сервером.
   */
  String CGF_PARAM_LOGIN = "login";

  /**
   * Идентификатор команды завершения работы НУ.
   */
  String CGF_PARAM_QUIT_CMD_ID = "quitCmdId";

  /**
   * Идентификатор объекта, отправляющего команду завершения работы НУ.
   */
  String CGF_PARAM_QUIT_CMD_OBJ_NAME = "quitCmdObjName";

  /**
   * Идентификатор класса команды завершения работы НУ.
   */
  String CGF_PARAM_QUIT_CMD_CLASS_ID = "quitCmdClassId";

  //
  // ----------------------------------------------------------------
  // Идентификаторы параметров команды завершения работы НУ
  // TODO Эти идентификаторы вынести в настроечный файл - а то получается они жётско зашиты в тексте

  /**
   * Сообщение, сопровождающее команду завершения работы НУ.
   */
  String QUIT_CMD_MSG_ARG_ID = "Msg";

  /**
   * Код завершения работы НУ.
   */
  String QUIT_CMD_RET_CODE_ARG_ID = "RetCode";
}

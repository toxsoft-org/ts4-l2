package org.toxsoft.l2.main.old_refactor.l10n;

/**
 * Локализуемые ресурсы точки входа в программу нижнего уровня.
 *
 * @author goga
 */
@SuppressWarnings( { "nls", "javadoc" } )
public interface IL2MainSharedResources {

  // ------------------------------------------------------------------------------------
  // Main
  //

  String MSG_STARTUP = "\nПрограмма нижнего уровня, вер.%s, ТоксСофт.\n" + "  Начало выполнения: %2$tF %2$tT\n";

  String MSG_CREATE_CONTEXT = "Создание контекста программы";

  String MSG_CREATE_HAL = "Создание модуля HAL(оборудование)";
  String MSG_START_HAL  = "Запуск модуля HAL(оборудование)";

  String MSG_CREATE_NETWORK = "Создание сетевых модулей";
  String MSG_START_NETWORK  = "Запуск сетевых модулей";

  String MSG_CREATE_APP = "Создание модуля приложения";
  String MSG_START_APP  = "Запуск модуля приложения";

  String MSG_CREATE_DLM = "Создание модуля менеджера DLM";
  String MSG_START_DLM  = "Запуск модуля менеджера DLM";

  String MSG_HOOK_SHUTDOWN = "Установка перехватчика остановки приложения (ctrl+C)";

  String MSG_MAIN_LOOP_STARTED                 = "Запуск главного цикла программы";
  String MSG_GOODBYE                           = "Программа НУ завершила работу.\n" +                              //
      "  Причина завершения: %s \n" +                                                                              //
      "  Код завершения: %d \n" +                                                                                  //
      "  Общее время работы %d дн. %02d:%02d:%02d \n";
  String MSG_MAIN_LOOP_INTERRUPTED             = "Внимание; главный цикл был прерван";
  String MSG_CANT_START                        = "Ошибка запуска программы НУ";
  String MSG_UNEXPECTED_SHUTDOWN               = "Аварийное завершение НУ";
  String MSG_CANT_SHUTDOWN                     = "Ошибка завершения работы НУ";
  String MSG_SHUTDOWN                          = "Программа НУ завершила работу. Код завершения: %d";
  String MSG_SHUTDOWN_BY_CTRL_C                = "Shutdown by ShutdownHook (CTRL+C)";
  String MSG_CFG_FILE_READ_OK                  = "Считан конфигурационный файл '%s'";
  String MSG_ERR_CANT_READ_CFG_FILE            = "Ошибка чтения конфигурационного файла '%s'";
  String MSG_ERR_UNEVEN_CMD_LINE_ARGS          = "Командная строка должна состоять из пар '-paramName paramValue'";
  String MSG_ERR_NO_HYPHEN_IN_CMD_LINE_OP_NAME =
      "Название параметра командной строки '%s' должно начинаться с тире '-'";
  String MSG_ERR_NO_OP_NAME                    = "За тире '-' не указано название параметра командной строки";
  String MSG_ERR_INV_OP_NAME                   =
      "Неверное имя параметра '%s', имя должно состоять из a-z, A-Z, 0-9, '_', '.' и начинаться с буквы";
  String MSG_ERR_INV_CMD_LINE                  = "Неврно задана командная строка";

  String MSG_WRITE_DEVICES_DELAY = "WRITE_DEVICES_DELAY %d";

  String MSG_READ_DEVICES_DELAY = "READ_DEVICES_DELAY %d";

}

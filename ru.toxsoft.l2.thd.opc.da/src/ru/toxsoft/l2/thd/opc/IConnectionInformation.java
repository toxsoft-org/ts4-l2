package ru.toxsoft.l2.thd.opc;

/**
 * @author Dima Данные необходимые для подключения к OPC серверу
 */
public interface IConnectionInformation {

  /**
   * @return String OPC location host
   */
  String host();

  /**
   * @return String OPC domain
   */
  String domain();

  /**
   * @return String user
   */
  String user();

  /**
   * @return String password
   */
  String password();

  /**
   * @return String OPC progId
   */
  String progId();

  /**
   * @return String OPC clsId
   */
  String clsId();

}

package ru.toxsoft.l2.thd.opc.da;

import ru.toxsoft.l2.thd.opc.*;

/**
 * @author Dima Настройка соединения с OPC server
 */
class ConnectionInformation
    implements IConnectionInformation {

  /**
   * host OPC сервера
   */
  private String host;
  /**
   * ProgId OPC сервера
   */
  private String progId;
  private String clsId;

  public void setClsId( String aClsId ) {
    clsId = aClsId;
  }

  public void setPassword( String aPassword ) {
    password = aPassword;
  }

  public void setUser( String aUser ) {
    user = aUser;
  }

  public void setDomain( String aDomain ) {
    domain = aDomain;
  }

  private String password;
  private String user;
  private String domain;

  @Override
  public String host() {
    return host;
  }

  @Override
  public String progId() {
    return progId;
  }

  public void setHost( String aHost ) {
    host = aHost;
  }

  public void setProgId( String aProgId ) {
    progId = aProgId;
  }

  @Override
  public String domain() {
    return domain;
  }

  @Override
  public String user() {
    return user;
  }

  @Override
  public String password() {
    return password;
  }

  @Override
  public String clsId() {
    return clsId;
  }

}

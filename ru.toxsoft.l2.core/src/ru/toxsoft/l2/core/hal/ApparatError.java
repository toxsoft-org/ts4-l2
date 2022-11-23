package ru.toxsoft.l2.core.hal;

import org.toxsoft.core.tslib.av.opset.*;

/**
 * Ошибка аппарата, возникшая во время обмена данными с физ. устройством.
 *
 * @author max
 */
public class ApparatError {

  /**
   * Идентификатор аппарата - источника ошибки.
   */
  private String apparatId;

  /**
   * Идентификатор ошибки.
   */
  private String errorId;

  /**
   * Параметры ошибки.
   */
  private IOptionSet params;

  /**
   * java-ошибка, ставшая причиной аппаратной ошибки.
   */
  private Exception exception;

  /**
   * Конструктор аппаратной ошибки.
   *
   * @param aApparatId - идентификатор аппарата - источника ошибки.
   * @param aErrorId - идентификатор ошибки.
   * @param aParams - параметры ошибки, может быть null.
   * @param aException - java-ошибка - причина аппаратной ошибки, может быть null.
   */
  public ApparatError( String aApparatId, String aErrorId, IOptionSet aParams, Exception aException ) {
    super();
    this.apparatId = aApparatId;
    this.errorId = aErrorId;
    this.params = aParams;
    this.exception = aException;
  }

  /**
   * Возвращает идентификатор аппарата - источника ошибки.
   *
   * @return String - идентификатор аппарата - источника ошибки.
   */
  public String getApparatId() {
    return apparatId;
  }

  /**
   * Возвращает идентификатор ошибки.
   *
   * @return String - идентификатор ошибки.
   */
  public String getErrorId() {
    return errorId;
  }

  /**
   * Возвращает параметры ошибки.
   *
   * @return IOptionSet - параметры ошибки.
   */
  public IOptionSet getParams() {
    return params;
  }

  /**
   * Возвращает java-ошибку, ставшую причиной аппаратной ошибки.
   *
   * @return Exception - java-ошибка, ставшая причиной аппаратной ошибки.
   */
  public Exception getException() {
    return exception;
  }

}

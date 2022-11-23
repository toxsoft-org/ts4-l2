package ru.toxsoft.l2.core.hal.devices;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.core.hal.*;

/**
 * Множественное исключение: может содержать в себе более одной исходной ошибки.
 *
 * @author max
 */
public class TsMultipleApparatRtException
    extends TsRuntimeException {

  /**
   * Для корректной сериализации.
   */
  private static final long serialVersionUID = 1L;

  private ElemArrayList<ApparatError> causes = new ElemArrayList<>();

  /**
   * Конструктор.
   *
   * @param aCauses IList - набор первоначальных аппаратных ошибок.
   * @param aMessageFormat - сообщение.
   * @param aMsgArgs - параметры сообщения.
   */
  public TsMultipleApparatRtException( IList<ApparatError> aCauses, String aMessageFormat, Object[] aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
    causes.addAll( aCauses );
  }

  /**
   * Конструктор.
   *
   * @param aCause ApparatError - первоначальная аппаратная ошибка.
   * @param aMessageFormat - сообщение.
   * @param aMsgArgs - параметры сообщения.
   */
  public TsMultipleApparatRtException( ApparatError aCause, String aMessageFormat, Object[] aMsgArgs ) {
    super( aMessageFormat, aMsgArgs );
    causes = new ElemArrayList<>( aCause );
  }

  /**
   * Возвращает набор исходных ошибок.
   *
   * @return IList - набор первоначальных ошибок.
   */
  public IList<ApparatError> getCauses() {
    return causes;
  }

  /**
   * Добавляет ошибки к уже сформированным ошибкам нижнего уровня.
   *
   * @param aErrors IList - список добавляемых ошибок.
   */
  public void addErrors( IList<ApparatError> aErrors ) {
    causes.addAll( aErrors );
  }

  /**
   * Добавляет ошибку к уже сформированным ошибкам нижнего уровня.
   *
   * @param aError ApparatError - добавляемая ошибка.
   */
  public void addError( ApparatError aError ) {
    causes.add( aError );
  }

}

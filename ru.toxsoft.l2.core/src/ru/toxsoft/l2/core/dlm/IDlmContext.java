package ru.toxsoft.l2.core.dlm;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.invokable.*;
import ru.toxsoft.l2.core.net.*;

/**
 * Контекст выполнения подключаемых модулей.
 *
 * @author max
 */
public interface IDlmContext {

  /**
   * Возвращает логгер.
   *
   * @return ILogger главный логгер системы.
   */
  @Deprecated
  ILogger logger();

  /**
   * Возвращает API работы с HAL
   *
   * @return IHal - API работы с HAL.
   */
  IHal hal();

  /**
   * Возвращает API работы с NET (сетью, ВУ)
   *
   * @return INetwork - API работы с NET (сетью, ВУ).
   */
  INetwork network();

  /**
   * Возвращает API работы с функционалом, специфичным для конкретного проекта.
   *
   * @return Object - API работы с функционалом, специфичным для конкретного проекта.
   */
  Object appApi();

  /**
   * Add by Dima, 30.11.11 Получить значения контекстных конфигурационных параметров
   *
   * @return набор значений параметров конфигурации
   */
  @Deprecated
  IOptionSet config();

  /**
   * Возвращает описания методов и переменных модулей в виде соответствия: идентификатор модуля - описание модуля.
   *
   * @return IStringMap - описания методов и переменных модулей в виде соответствия: идентификатор модуля - описание
   *         модуля.
   */
  IStringMap<IInvokableInfo> dlmInfoes();

  /**
   * Возвращает значение переменной модуля.
   *
   * @param aDlmId - идентификатор модуля.
   * @param aVarId - идентификатор переменной.
   * @return значение переменной модуля.
   * @throws TsNullArgumentRtException - один из аргументов - null.
   * @throws TsItemNotFoundRtException - модуль не загружен, нет переменной с таким идентификатором
   */
  IAtomicValue getVar( String aDlmId, String aVarId )
      throws TsNullArgumentRtException,
      TsItemNotFoundRtException;

  /**
   * Возвращает значение структуры модуля.
   *
   * @param aDlmId - идентификатор модуля.
   * @param aStructId - идентификатор структуры.
   * @return значение структуры модуля.
   * @throws TsNullArgumentRtException - один из аргументов - null.
   * @throws TsItemNotFoundRtException - модуль не загружен, нет структуры с таким идентификатором
   */
  IOptionSet getStructVar( String aDlmId, String aStructId )
      throws TsNullArgumentRtException,
      TsItemNotFoundRtException;

  /**
   * Устанавливает значение переменной модуля.
   *
   * @param aDlmId - идентификатор модуля.
   * @param aVarId - идентификатор переменной.
   * @param aValue - устанавливаемое значение переменной.
   * @throws TsNullArgumentRtException - один из аргументов - null.
   * @throws TsItemNotFoundRtException - модуль не загружен, нет переменной с таким идентификатором
   * @throws TsIllegalArgumentRtException - при попытке присвоить значение неизменяемой переменной, при несоответствии
   *           типа аргумента типу, указанному в описании {@link IVarInfo#dataType()}
   */
  void setVar( String aDlmId, String aVarId, IAtomicValue aValue )
      throws TsNullArgumentRtException,
      TsItemNotFoundRtException,
      TsIllegalArgumentRtException;

  /**
   * Вызывает метод модуля.
   *
   * @param aDlmId - идентификатор модуля.
   * @param aMethodId - идентификатор метода модуля.
   * @param aArgsValues - аргументы вызываемого метода, может быть null - еслм link {@link IMethodInfo#argsInfo() }
   *          .size() == 0.
   * @return ECallStatus - состояние результата вызова метода.
   * @throws TsNullArgumentRtException - аргумент aDlmId или aMethodId - null.
   * @throws TsItemNotFoundRtException - модуль не загружен, нет метода с таким идентификатором.
   * @throws TsIllegalArgumentRtException - aArgsValues - не соответствует описанию {@link IMethodInfo#argsInfo()} .
   */
  ECallStatus call( String aDlmId, String aMethodId, IOptionSet aArgsValues );
}

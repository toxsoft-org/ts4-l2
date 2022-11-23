package ru.toxsoft.l2.core.dlm.impl;

import static ru.toxsoft.l2.core.dlm.impl.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.invokable.*;
import ru.toxsoft.l2.core.main.*;
import ru.toxsoft.l2.core.net.*;

/**
 * Реализация контекста выполнения подключаемых модулей
 *
 * @author max
 */
class DlmContext
    implements IDlmContext {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  private final IHal hal;

  private final INetwork network;

  private final Object appApi;

  private final IDlmManager dlmManager;

  DlmContext( IGlobalContext aContext ) {
    hal = aContext.hal();
    network = aContext.network();
    appApi = aContext.appApi();
    dlmManager = aContext.dlmManager();
  }

  @Deprecated
  @Override
  public ILogger logger() {
    return logger;
  }

  @Override
  public IHal hal() {
    return hal;
  }

  @Override
  public INetwork network() {
    return network;
  }

  @Override
  public Object appApi() {
    return appApi;
  }

  @Override
  @Deprecated
  public IOptionSet config() {
    // нет реализации
    return null;
  }

  @Override
  public IStringMap<IInvokableInfo> dlmInfoes() {
    IStringMapEdit<IInvokableInfo> result = new StringMap<>();

    for( IDlm dlm : dlmManager.modules() ) {
      result.put( dlm.info().moduleId(), dlm.invokableInfo() );
    }

    return result;
  }

  @Override
  public IAtomicValue getVar( String aDlmId, String aVarId )
      throws TsNullArgumentRtException,
      TsItemNotFoundRtException,
      TsIllegalArgumentRtException {
    TsNullArgumentRtException.checkNulls( aDlmId, aVarId );
    IDlm dlm = findByModuleId( aDlmId );
    dlm.invokableInfo().listVarInfoes().getByKey( aVarId );

    return dlm.getVar( aVarId );
  }

  @Override
  public void setVar( String aDlmId, String aVarId, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aDlmId, aVarId, aValue );
    IDlm dlm = findByModuleId( aDlmId );

    // проверки аргументов

    IVarInfo varInfo = dlm.invokableInfo().listVarInfoes().getByKey( aVarId );

    if( varInfo.isReadOnly() ) {
      throw new TsIllegalArgumentRtException( ERR_UNMUTABLE_VAL_ATTAMPT, aVarId );
    }

    if( !aValue.equals( IAtomicValue.NULL ) && !varInfo.dataType().atomicType().equals( aValue.atomicType() ) ) {
      throw new TsIllegalArgumentRtException( ERR_WRONG_TYPE_VAL_ATTAMPT, aVarId );
    }

    dlm.setVar( aVarId, aValue );
  }

  @SuppressWarnings( "null" )
  @Override
  public ECallStatus call( String aDlmId, String aMethodId, IOptionSet aArgsValues ) {
    TsNullArgumentRtException.checkNulls( aDlmId, aMethodId );
    IDlm dlm = findByModuleId( aDlmId );

    // проверки аргументов

    IMethodInfo methodInfo = dlm.invokableInfo().listMethodInfoes().getByKey( aMethodId );

    if( aArgsValues == null && methodInfo.argsInfo().size() > 0 ) {
      throw new TsIllegalArgumentRtException( ERR_NULL_ARGUMENTS );
    }

    for( String argKey : methodInfo.argsInfo().keys() ) {
      IAtomicValue value = aArgsValues.getValue( argKey );
      IDataType type = methodInfo.argsInfo().getByKey( argKey );

      if( value != IAtomicValue.NULL && value.atomicType() != type.atomicType() ) {
        throw new TsIllegalArgumentRtException( ERR_ARG_VALUE_EMPTY, argKey );
      }
    }

    // проверки завершены - вызываем метод
    return dlm.call( aMethodId, aArgsValues );
  }

  @Override
  public IOptionSet getStructVar( String aDlmId, String aStructId )
      throws TsIllegalArgumentRtException {
    TsNullArgumentRtException.checkNulls( aDlmId, aStructId );
    IDlm dlm = findByModuleId( aDlmId );
    dlm.invokableInfo().listStructInfoes().getByKey( aStructId );

    return dlm.getStructVar( aStructId );
  }

  //
  // -----------------------------------------------------------------------------------------
  // внутренние методы

  /**
   * Находит модуль по его идентификатору.
   *
   * @param aModuleId - идентификатор модуля.
   * @return модуль.
   * @throws TsItemNotFoundRtException - в случае отсутствия модуля с таким идентификатором.
   */
  IDlm findByModuleId( String aModuleId )
      throws TsItemNotFoundRtException {
    for( IDlm dlm : dlmManager.modules() ) {
      if( dlm.info().moduleId().equals( aModuleId ) ) {
        return dlm;
      }
    }
    throw new TsItemNotFoundRtException( MSG_ERR_DLM_NOT_FOUND, aModuleId );
  }

}

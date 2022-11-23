package ru.toxsoft.l2.core.dlm.impl;

import static ru.toxsoft.l2.core.dlm.impl.IL2Resources.*;
import static ru.toxsoft.l2.core.main.IL2HardConstants.*;

import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.plugins.*;

import ru.toxsoft.l2.core.dlm.*;

/**
 * Абстрактный базовый класс реализации фабрик DLM-модулей.
 * <p>
 * Реализация конструктора в наследнике должна быть без <b>без аргументов</b>, чтобы менеджер плагино мог создать ее
 * экземпляр с помощью {@link Class#newInstance()}.
 *
 * @author goga
 */
public abstract class AbstractDlmFactory
    implements IDlmFactory {

  private IDlmInfo info = null;

  /**
   * Защищенный конструктор - только для наследников.
   */
  protected AbstractDlmFactory() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDlmFactory
  //

  @Override
  public void setPluginIngfo( IPluginInfo aPluginInfo ) {
    String moduleName = aPluginInfo.userProperties().getByKey( MF_ATTR_DLM_NAME );
    if( moduleName == null ) {
      moduleName = TsLibUtils.EMPTY_STRING;
    }
    String devPersons = aPluginInfo.userProperties().getByKey( MF_ATTR_DLM_DEVELOPER_PERSONS );
    if( devPersons == null ) {
      devPersons = TsLibUtils.EMPTY_STRING;
    }
    String devCompany = aPluginInfo.userProperties().getByKey( MF_ATTR_DLM_DEVELOPER_COMPANY );
    if( devCompany == null ) {
      devCompany = TsLibUtils.EMPTY_STRING;
    }
    info = new DlmInfo( aPluginInfo.pluginId(), moduleName, aPluginInfo.pluginVersion(), devPersons, devCompany );
  }

  @Override
  public IDlmInfo info() {
    return info;
  }

  @Override
  final public IDlm createDlm( IDlmContext aDlmContext ) {

    IDlm dlm;
    try {
      dlm = doCreateDlm( aDlmContext );
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, MSG_ERR_DLM_CREATION_EXCEPTION, info.moduleId(), info.moduleName() );
    }
    if( dlm == null ) {
      throw new TsInternalErrorRtException( MSG_ERR_NULL_DLM_CREATED, info.moduleId(), info.moduleName() );
    }
    if( !info.equals( dlm.info() ) ) {
      throw new TsInternalErrorRtException( MSG_ERR_DLM_CREATED_WITH_BAD_INFO, info.moduleId(), info.moduleName() );
    }
    return dlm;
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //

  /**
   * Реализация в наследниках должна создать и вернуть экзмпляр DLM-модуля.
   *
   * @param aDlmContext {@link IDlmContext} - контекст выполнения создаваемого модуля
   * @return {@link IDlm} - экземпляр созданного модуля
   */
  abstract protected IDlm doCreateDlm( IDlmContext aDlmContext );

}

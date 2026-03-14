package org.toxsoft.l2.lib.dlm.impl;

import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.dlm.*;

/**
 * Неизменяемая реализация {@link IDlmInfo}.
 *
 * @author goga
 */
public class DlmInfo
    implements IDlmInfo {

  private final String    moduleId;
  private final String    moduleName;
  private final TsVersion version;
  private final String    developerPersons;
  private final String    developerCompany;

  /**
   * Создает объект со всеми инвариантами.
   *
   * @param aModuleId String - идентификатор (ИД-путь) модуля
   * @param aModuleName String - название модуля
   * @param aVersion {@link TsVersion} - версия модуля
   * @param aDeveloperPersons String - авторы - люди
   * @param aDeveloperCompany String - автор - организация
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aModuleType или aModuleName - пустая строка
   */
  public DlmInfo( String aModuleId, String aModuleName, TsVersion aVersion, String aDeveloperPersons,
      String aDeveloperCompany ) {
    moduleId = TsErrorUtils.checkNonEmpty( aModuleId );
    moduleName = TsErrorUtils.checkNonEmpty( aModuleName );
    version = TsNullArgumentRtException.checkNull( aVersion );
    developerPersons = TsNullArgumentRtException.checkNull( aDeveloperPersons );
    developerCompany = TsNullArgumentRtException.checkNull( aDeveloperCompany );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IDlmInfo
  //

  @Override
  public String moduleId() {
    return moduleId;
  }

  @Override
  public String moduleName() {
    return moduleName;
  }

  @Override
  public TsVersion version() {
    return version;
  }

  @Override
  public String developerPersons() {
    return developerPersons;
  }

  @Override
  public String developerCompany() {
    return developerCompany;
  }

  // ------------------------------------------------------------------------------------
  // Переопределение методов класса Object
  //

  @Override
  public boolean equals( Object obj ) {
    if( obj == this ) {
      return true;
    }
    if( !(obj instanceof IDlmInfo info) ) {
      return false;
    }
    return moduleName.equals( info.moduleName() ) && moduleId.equals( info.moduleId() )
        && version.equals( info.version() ) && developerPersons.equals( info.developerPersons() )
        && developerCompany.equals( info.developerCompany() );
  }

  @Override
  public int hashCode() {
    int hc = TsLibUtils.INITIAL_HASH_CODE;
    hc += TsLibUtils.PRIME * hc + moduleId.hashCode();
    hc += TsLibUtils.PRIME * hc + version.hashCode();
    hc += TsLibUtils.PRIME * hc + moduleName.hashCode();
    hc += TsLibUtils.PRIME * hc + developerPersons.hashCode();
    hc += TsLibUtils.PRIME * hc + developerPersons.hashCode();
    return hc;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return moduleId + " - " + TsVersion.getVersionNumber( version ) + " - " + moduleName;
  }

}

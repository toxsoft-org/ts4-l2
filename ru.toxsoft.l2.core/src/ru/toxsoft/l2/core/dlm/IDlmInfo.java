package ru.toxsoft.l2.core.dlm;

import org.toxsoft.core.tslib.utils.*;

/**
 * Информация о динамически подгружаемом модуле (DLM).
 * <p>
 * Информация о модуле состоит из двух частей:
 * <ul>
 * <li>идентификация модуля - идентификатор {@link #moduleId()} и версия {@link #version()} модуля. Эти данные совпадают
 * с идентификационной информацией плагина {@link IPluginInfo#pluginId()} и {@link IPluginInfo#pluginVersion()}.
 * <li>дополнительная информация о модуле - удобочитаемое имя {@link #moduleName()}, и информация об авторах
 * {@link #developerCompany()} и {@link #developerPersons()}.</li>
 * </ul>
 * Два модуля с одинаковым идентификатором {@link #moduleId()} считаются одним и тем же, и отличаться могут только
 * версией {@link #version()}. При этом, не допускается нахождение в одной директории двух модулей с одинаковым
 * идентификатором.
 *
 * @author goga
 */
public interface IDlmInfo {

  /**
   * Возвращает идентификатор модуля.
   * <p>
   * Совпалает с идентификатором плагина {@link IPluginInfo#pluginId()}.
   *
   * @return String - идентификатор (ИД-путь) модуля
   */
  String moduleId();

  /**
   * Возвращает версию модуля.
   * <p>
   * Совпадает с версией модуля.
   *
   * @return {@link ITsVersion} - версия модуля
   */
  TsVersion version();

  /**
   * Возвращает удобочитаемое название модуля.
   *
   * @return String - название модуля
   */
  String moduleName();

  /**
   * Возвращает информацию о людях - авторах модуля.
   *
   * @return String - информация о людях - авторах
   */
  String developerPersons();

  /**
   * Возвращает информацию об организации - авторе модуля.
   *
   * @return String - информация об организации - авторе модуля
   */
  String developerCompany();

}

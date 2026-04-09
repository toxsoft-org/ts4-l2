package org.toxsoft.l2.lib.cfg;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Считанная из файла конфигурация конфигурируемой единицы.
 * <p>
 * Этот интерфейс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный среди всех единиц определённого типа идентификатор (ИД-путь), задан и загружается из
 * файла конфигурации;</li>
 * <li><b>description</b>() - необязательно удобочитаемое описание (тоже, конечно, загружается из файла
 * конфигурации).</li>
 * </ul>
 *
 * @author goga
 */
public interface IUnitConfig
    extends IStridable {

  /**
   * Набор параметров конфигурации, считанный из файла в виде стандартного дерева.
   * <p>
   * Интерпретация смысла параметров - полностью за конфигурируемой единицей, в методе
   * {@link IConfigurableUnit#configYourself(IUnitConfig)}.
   *
   * @return IStringList&lt;IOptionSet&gt; - параметров конфигурации, считанный из файла
   */
  IAvTree params();
}

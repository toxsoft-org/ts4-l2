package org.toxsoft.l2.lib.cfg;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Конфигурируемая единица, для конфигурации которой используется объект {@link IUnitConfig}.
 *
 * @author max
 */
public interface IConfigurableUnit {

  /**
   * По конфигурационной инфорации единица настраивает себя.
   *
   * @param aConfig - конфигурационная информация.
   * @throws TsIllegalArgumentRtException - в случае, если не удалось корректно провести конфигурацию по поданной
   *           конфигурационной информации.
   */
  void configYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException;
}

package ru.toxsoft.l2.core.cfg.impl;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.core.cfg.*;

/**
 * Реализация конфигурации по умолчанию.
 *
 * @author max
 */
public class DefaultUnitConfig
    extends Stridable

    implements IUnitConfig {

  private final IAvTree params;

  protected DefaultUnitConfig( String aId, String aDescription, IAvTree aParams ) {
    super( aId, aDescription, aDescription, true );
    TsNullArgumentRtException.checkNull( aParams );
    params = aParams;
  }

  @Override
  public IAvTree params() {
    return params;
  }

}

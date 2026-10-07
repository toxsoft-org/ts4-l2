package org.toxsoft.l2.dlm.tags;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

public abstract class AbstractGwidValueSetter
    implements IGwidValueSetter {

  private Gwid gwid;

  public AbstractGwidValueSetter( Gwid aGwid ) {
    TsIllegalArgumentRtException.checkNull( aGwid );
    gwid = aGwid;
  }

  @Override
  public Gwid gwid() {
    return gwid;
  }

}

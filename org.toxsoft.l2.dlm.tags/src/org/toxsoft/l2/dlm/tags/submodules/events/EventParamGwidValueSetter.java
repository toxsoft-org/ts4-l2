package org.toxsoft.l2.dlm.tags.submodules.events;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.l2.dlm.tags.*;

public class EventParamGwidValueSetter
    extends AbstractGwidValueSetter {

  private IOptionSetEdit paramValues;

  public EventParamGwidValueSetter( Gwid aEventParamGwid, IOptionSetEdit aParamValues ) {
    super( aEventParamGwid );
    paramValues = aParamValues;
  }

  @Override
  public boolean setGwidValue( IAtomicValue aValue, long aTime ) {
    paramValues.put( gwid().subPropId(), aValue );
    return true;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

}

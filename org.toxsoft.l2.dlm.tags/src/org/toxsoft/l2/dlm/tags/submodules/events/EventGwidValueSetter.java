package org.toxsoft.l2.dlm.tags.submodules.events;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.uskat.core.api.evserv.*;

public class EventGwidValueSetter
    extends AbstractGwidValueSetter {

  private ISkEventService eventService;

  private IOptionSet paramValues;

  public EventGwidValueSetter( ISkEventService aEventService, Gwid aEventGwid, IOptionSet aParamValues ) {
    super( aEventGwid );
    eventService = aEventService;

    paramValues = aParamValues;
  }

  @Override
  public boolean setGwidValue( IAtomicValue aValue, long aTime ) {

    SkEvent ev = new SkEvent( aTime, gwid(), paramValues );

    eventService.fireEvent( ev );
    return true;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

}

package org.toxsoft.l2.dlm.tags.submodules.events;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.l2.dlm.tags.*;

public abstract class AbstarctEventSender
    implements IDataGwidTranslator {

  protected Gwid eventGwid;

  protected Gwid getEventGwid() {
    return eventGwid;
  }

  protected abstract IList<String[]> getTagIds();
}

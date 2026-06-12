package org.toxsoft.l2.lib.serv;

import org.toxsoft.uskat.core.api.evserv.*;

/**
 * Listens to the L2 application lifetime events.
 *
 * @author hazard157
 */
public interface ISkL2ApplicationServiceListener {

  void onL2ApplicationEvent( SkEvent aEvent );

}

package org.toxsoft.l2.lib.net;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * L2 Network - communication to the USkat server via network.
 *
 * @author hazard157
 */
public interface IL2Network {

  /**
   * Returns the connection to the USkat server.
   * <p>
   * Connection may be used only from L2 application main thread, only from {@link ICooperativeMultiTaskable#doJob()}.
   *
   * @return {@link ISkConnection} - connection used for network communication
   */
  ISkConnection getSkConnection();

  // interface IStarter {
  //
  // IStridablesList<ISkClassInfo> listClasses();
  //
  // // TODO objects service
  //
  // String registerForCommands( IGwidList aCmdGwids );
  //
  // void queryReadCurrDataChannels( IGwidList aGwids );
  //
  // void queryWriteCurrDataChannels( IGwidList aGwids );
  //
  // }
  //
  // // ------------------------------------------------------------------------------------
  // //
  //
  // ISkReadCurrDataChannel getReadCurrDataChannel( Gwid aGwid );
  //
  // ISkWriteCurrDataChannel getWriteCurrDataChannel( Gwid aGwid );
  //
  // IList<IDtoCommand> getCommands( String aRegistrationId );
  //
  // void changeCommandState( String aRegistrationId, DtoCommandStateChangeInfo aStateChangeInfo );
  //
  // ISkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs );
  //
  // void sendEvent( SkEvent aEvent );
  //
  // String readClob( Gwid aGwid );
  //
  // void writeClob( Gwid aGwid, String aClob );

}

package ge.toxsoft.gwp.opcuabridge.server.data;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * Установщик значений в канал данных из тега
 *
 * @author max
 */
public class CurrDataChannelSetter
    implements IChannelDataSetter {

  private ISkWriteCurrDataChannel channel;

  // private IReadTag srcTag;

  @Override
  public void setDataToChannel( IAtomicValue aValue ) {
    channel.setValue( aValue );

  }
}

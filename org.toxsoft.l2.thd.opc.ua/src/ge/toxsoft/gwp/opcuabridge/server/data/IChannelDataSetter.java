package ge.toxsoft.gwp.opcuabridge.server.data;

import org.toxsoft.core.tslib.av.*;

/**
 * Установщик значений в канал данных
 *
 * @author max
 */
public interface IChannelDataSetter {

  /**
   * Устанавливает значение в канал.
   */
  void setDataToChannel( IAtomicValue aValue );
}

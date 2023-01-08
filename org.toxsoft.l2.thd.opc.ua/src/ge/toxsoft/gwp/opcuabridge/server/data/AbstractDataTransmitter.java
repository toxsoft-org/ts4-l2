package ge.toxsoft.gwp.opcuabridge.server.data;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;

import ru.toxsoft.l2.thd.opc.*;

public abstract class AbstractDataTransmitter {

  private IList<IChannelDataSetter>[] setters;

  private ITag[] tags;

  private int outputCount;

  public void transmit() {
    IAtomicValue[] values = calcValues();

    for( int i = 0; i < outputCount; i++ ) {
      IList<IChannelDataSetter> outputSetters = setters[i];
      IAtomicValue outputValue = values[i];

      for( IChannelDataSetter dataSetter : outputSetters ) {
        dataSetter.setDataToChannel( outputValue );
      }
    }
  }

  protected abstract IAtomicValue[] calcValues();
}

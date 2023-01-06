package ge.toxsoft.gwp.opcuabridge.server.data;

import org.toxsoft.core.tslib.coll.*;

/**
 * Писатель в датасет сервера
 *
 * @author max
 */
public class DataSetWriter {

  private IList<IChannelDataSetter> dataSetters;

  void insertToDataSet() {
    for( IChannelDataSetter dSetter : dataSetters ) {
      // dSetter.setDataToChannel();
    }
  }
}

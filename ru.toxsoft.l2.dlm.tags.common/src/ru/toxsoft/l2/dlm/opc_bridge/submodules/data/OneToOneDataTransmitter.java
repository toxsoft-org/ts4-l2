package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация простейшего передатчика - один тег на одно данное.
 *
 * @author max
 * @param <T> - класс дата-сета
 */
public class OneToOneDataTransmitter<T extends ISkRtdataChannel>
    implements IDataTransmitter<T>, ITagable {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( OneToOneDataTransmitter.class.getName() );

  /**
   * Тег.
   */
  protected ITag tag;

  // private IAtomicValue oldVal = IAtomicValue.NULL;

  /**
   * Номер данного в дата-сете
   */
  protected IDataSetter dataSetter;

  @Override
  public boolean transmit( long aTime ) {
    IAtomicValue newVal = tag.get();

    if( newVal == null || newVal.equals( IAtomicValue.NULL ) || !newVal.isAssigned() ) {
      return false;
    }

    // 2023.02.01 Убрана проверка, потому что она ещё присутствует в setter
    // if( !oldVal.equals( newVal ) ) {
    // logger.debug( "Value tag %s, changed: %s", tag.tagId(), newVal.toString() );
    // oldVal = newVal;

    boolean result = false;
    try {
      result = dataSetter.setDataValue( newVal, aTime );
    }
    catch( Exception e ) {
      logger.error( e, "Set data error: gwid: %s, tag: %s, error: %s", dataSetter.toString(), tag.tagId(),
          e.getMessage() );
    }
    return result;

    // }
    // oldVal = newVal;
    // return false;
  }

  @Override
  public void config( IAvTree aParams ) {
    // без реализации

  }

  @Override
  public void start( IDataSetter[] aDataSetindexes, IList<ITag> aTags, IMap<Gwid, T> aWriteDataSet ) {
    dataSetter = aDataSetindexes[0];
    tag = aTags.get( 0 );
  }

  protected ITag getTag() {
    return tag;
  }

  protected IDataSetter getInDataSetIndex() {
    return dataSetter;
  }

  @Override
  public ITag tag() {
    return tag;
  }

}

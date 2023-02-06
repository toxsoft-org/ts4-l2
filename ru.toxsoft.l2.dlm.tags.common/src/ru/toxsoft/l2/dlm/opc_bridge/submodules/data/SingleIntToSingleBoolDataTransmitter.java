package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация передатчика - один бит из Integer тег на одно Boolean данное.
 *
 * @author max
 * @param <T> - класс дата-сета
 */
public class SingleIntToSingleBoolDataTransmitter<T extends ISkRtdataChannel>
    implements IDataTransmitter<T> {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( SingleIntToSingleBoolDataTransmitter.class.getName() );

  /**
   * Integer Тег.
   */
  protected ITag tag;

  /**
   * Установщик значений в дата-сет
   */
  protected IDataSetter dataSetter;

  /**
   * Установщик инверсного значения в дата-сет
   */
  protected IDataSetter invDataSetter;

  /**
   * Номер значащего бита (от 0)
   */
  protected int bitIndex = -1;

  @Override
  public boolean transmit( long aTime ) {
    IAtomicValue tagValue = tag.get();

    if( bitIndex < 0 || tagValue == null || tagValue.equals( IAtomicValue.NULL ) || !tagValue.isAssigned() ) {
      return false;
    }

    int value = tagValue.asInt();

    boolean val = ((value >> bitIndex) & 1) == 1;

    boolean result = false;
    try {
      result = dataSetter.setDataValue( AvUtils.avBool( val ), aTime );
    }
    catch( Exception e ) {
      logger.error( e, "Set data error: gwid: %s, tag: %s, error: %s", dataSetter.toString(), tag.tagId(),
          e.getMessage() );
    }

    if( invDataSetter != null ) {
      try {
        result = result || invDataSetter.setDataValue( AvUtils.avBool( !val ), aTime );
      }
      catch( Exception e ) {
        logger.error( e, "Set data error: gwid: %s, tag: %s, error: %s", invDataSetter.toString(), tag.tagId(),
            e.getMessage() );
      }
    }
    return result;
  }

  @Override
  public void config( IAvTree aParams ) {
    if( aParams.fields().hasKey( BIT_INDEX )
        && aParams.fields().getValue( BIT_INDEX ).atomicType() == EAtomicType.INTEGER ) {
      bitIndex = aParams.fields().getInt( BIT_INDEX );
    }
    else {
      logger.error(
          "Structure '%s': if tag processor is SingleIntToSingleBoolDataTransmitter, then the integer field '%s' must be filled", //$NON-NLS-1$
          aParams.structId(), BIT_INDEX );
    }

  }

  @Override
  public void start( IDataSetter[] aDataSetindexes, IList<ITag> aTags, IMap<Gwid, T> aWriteDataSet ) {
    dataSetter = aDataSetindexes[0];
    tag = aTags.get( 0 );

    if( aDataSetindexes.length > 1 ) {
      invDataSetter = aDataSetindexes[1];
      logger.info( "ADD INVERSE DATA: %s", invDataSetter.toString() );
    }
  }

  protected ITag getTag() {
    return tag;
  }

}

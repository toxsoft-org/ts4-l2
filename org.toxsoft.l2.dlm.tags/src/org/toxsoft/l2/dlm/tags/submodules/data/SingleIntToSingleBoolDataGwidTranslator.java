package org.toxsoft.l2.dlm.tags.submodules.data;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Реализация передатчика - один бит из Integer тег на одно Boolean данное.
 *
 * @author max
 */
public class SingleIntToSingleBoolDataGwidTranslator
    implements IDataGwidTranslator {

  /**
   * Журнал работы
   */
  private static ILogger logger;
  /**
   * Integer Тег.
   */
  protected IL2Tag       tag;

  /**
   * Установщик значений в дата-сет
   */
  protected IGwidValueSetter dataSetter;

  /**
   * Установщик инверсного значения в дата-сет
   */
  protected IGwidValueSetter invDataSetter;

  /**
   * Номер значащего бита (от 0)
   */
  protected int bitIndex = -1;

  @Override
  public boolean translate( long aTime ) {
    IAtomicValue tagValue = tag.get();

    if( bitIndex < 0 || tagValue == null || tagValue.equals( IAtomicValue.NULL ) || !tagValue.isAssigned() ) {
      return false;
    }

    int value = tagValue.asInt();

    boolean val = ((value >> bitIndex) & 1) == 1;

    boolean result = false;
    try {
      result = dataSetter.setGwidValue( AvUtils.avBool( val ), aTime );
    }
    catch( Exception e ) {
      logger.error( e, "Set data error: gwid: %s, tag: %s, error: %s", dataSetter.toString(), tag.id(),
          e.getMessage() );
    }

    if( invDataSetter != null ) {
      try {
        result = result || invDataSetter.setGwidValue( AvUtils.avBool( !val ), aTime );
      }
      catch( Exception e ) {
        logger.error( e, "Set data error: gwid: %s, tag: %s, error: %s", invDataSetter.toString(), tag.id(),
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
  public void start( IGwidValueSetter[] aDataSetindexes, IList<IL2Tag> aTags ) {
    dataSetter = aDataSetindexes[0];
    tag = aTags.get( 0 );

    if( aDataSetindexes.length > 1 ) {
      invDataSetter = aDataSetindexes[1];
      logger.info( "ADD INVERSE DATA: %s", invDataSetter.toString() );
    }
  }

  protected IL2Tag getTag() {
    return tag;
  }

}

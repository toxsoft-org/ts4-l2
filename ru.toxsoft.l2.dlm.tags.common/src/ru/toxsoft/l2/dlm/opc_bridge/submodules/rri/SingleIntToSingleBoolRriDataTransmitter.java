package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.skf.rri.lib.*;

import ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.RriDataTransmittersInitializer.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация передатчика - один бит из Integer тег на одно Boolean RRI данное.
 *
 * @author dima
 */
public class SingleIntToSingleBoolRriDataTransmitter
    implements IRriDataTransmitter {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( SingleIntToSingleBoolRriDataTransmitter.class.getName() );

  /**
   * Integer Тег.
   */
  protected ITag tag;

  /**
   * Установщик значений в секцию НСИ
   */
  protected IRriSetter rriSetter;

  /**
   * Установщик инверсного значения в секцию НСИ
   */
  protected IRriSetter invRriSetter;

  /**
   * Карта привязки Gwid к НСИ секциям
   */
  protected IMap<Gwid, ISkRriSection> gwid2SectionMap;

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
      result = rriSetter.setRriValue( AvUtils.avBool( val ), aTime );
    }
    catch( Exception e ) {
      logger.error( e, "Set rri data error: gwid: %s, tag: %s, error: %s", rriSetter.toString(), tag.tagId(),
          e.getMessage() );
    }

    if( invRriSetter != null ) {
      try {
        result = result || invRriSetter.setRriValue( AvUtils.avBool( !val ), aTime );
      }
      catch( Exception e ) {
        logger.error( e, "Set rri data error: gwid: %s, tag: %s, error: %s", invRriSetter.toString(), tag.tagId(),
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
          "Structure '%s': if tag processor is SingleIntToSingleBoolRriDataTransmitter, then the integer field '%s' must be filled", //$NON-NLS-1$
          aParams.structId(), BIT_INDEX );
    }

  }

  @Override
  public void start( IRriSetter[] aRriSetters, IList<ITag> aTags ) {
    rriSetter = aRriSetters[0];
    tag = aTags.get( 0 );
    gwid2SectionMap = rriSetter.gwid2Section();

    if( aRriSetters.length > 1 ) {
      invRriSetter = aRriSetters[1];
      logger.info( "ADD INVERSE RRI SETTER: %s", invRriSetter.toString() );
    }
  }

  protected ITag getTag() {
    return tag;
  }

  @Override
  public IMap<Gwid, ISkRriSection> gwid2Section() {
    return gwid2SectionMap;
  }

  @Override
  public boolean writeBack2OpcNode( Gwid aRriGwid, IAtomicValue aNewValue ) {

    if( bitIndex < 0 || aNewValue == null || aNewValue.equals( IAtomicValue.NULL ) || !aNewValue.isAssigned() ) {
      return false;
    }
    // проверяем что это мой Gwid
    Gwid myGwid = gwid2SectionMap.keys().first();
    if( !myGwid.equals( aRriGwid ) ) {
      return false;
    }

    ValueCommandExec.setTagBit( tag, bitIndex, aNewValue, logger );
    return true;
  }

  @Override
  public void transmitAnyWay() {
    IAtomicValue tagValue = tag.get();

    if( bitIndex < 0 || tagValue == null || tagValue.equals( IAtomicValue.NULL ) || !tagValue.isAssigned() ) {
      return;
    }

    int value = tagValue.asInt();

    boolean val = ((value >> bitIndex) & 1) == 1;

    boolean result = false;
    try {
      result = ((RriSetter)rriSetter).setDataValueAnyway( AvUtils.avBool( val ), System.currentTimeMillis() );
    }
    catch( Exception e ) {
      logger.error( e, "Set rri data error: gwid: %s, tag: %s, error: %s", rriSetter.toString(), tag.tagId(),
          e.getMessage() );
    }

    if( invRriSetter != null ) {
      try {
        result = result || invRriSetter.setRriValue( AvUtils.avBool( !val ), System.currentTimeMillis() );
      }
      catch( Exception e ) {
        logger.error( e, "Set rri data error: gwid: %s, tag: %s, error: %s", invRriSetter.toString(), tag.tagId(),
            e.getMessage() );
      }
    }

  }

}
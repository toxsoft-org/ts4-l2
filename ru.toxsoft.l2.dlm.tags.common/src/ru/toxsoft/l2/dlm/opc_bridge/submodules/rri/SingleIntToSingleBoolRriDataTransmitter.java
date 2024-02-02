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

import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.IComplexTag.*;
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

  /**
   * Универсальный тег для записи значений в OPC UA
   */
  protected IComplexTag сomplexTag;

  /**
   * index of OPC command to turn bit ON
   */
  protected int opcCmdIndexOn = -1;

  /**
   * index of OPC command to turn bit OFF
   */
  protected int opcCmdIndexOff = -1;

  /**
   * timestamp of last OPC command
   */
  protected long lastOPCCmdTimestamp = -1;

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
    // читаем коды команды OPC
    if( aParams.fields().hasKey( OPC_CMD_INDEX_ON )
        && aParams.fields().getValue( OPC_CMD_INDEX_ON ).atomicType() == EAtomicType.INTEGER ) {
      opcCmdIndexOn = aParams.fields().getInt( OPC_CMD_INDEX_ON );
    }
    else {
      logger.error(
          "Structure '%s': if tag processor is SingleIntToSingleBoolRriDataTransmitter, then the integer field '%s' must be filled", //$NON-NLS-1$
          aParams.structId(), OPC_CMD_INDEX_ON );
    }

    // читаем коды команды OPC
    if( aParams.fields().hasKey( OPC_CMD_INDEX_OFF )
        && aParams.fields().getValue( OPC_CMD_INDEX_OFF ).atomicType() == EAtomicType.INTEGER ) {
      opcCmdIndexOff = aParams.fields().getInt( OPC_CMD_INDEX_OFF );
    }
    else {
      logger.error(
          "Structure '%s': if tag processor is SingleIntToSingleBoolRriDataTransmitter, then the integer field '%s' must be filled", //$NON-NLS-1$
          aParams.structId(), OPC_CMD_INDEX_OFF );
    }

  }

  @Override
  public void start( IRriSetter[] aRriSetters, IList<ITag> aTags, IComplexTag aComplexTag ) {
    rriSetter = aRriSetters[0];
    tag = aTags.get( 0 );
    gwid2SectionMap = rriSetter.gwid2Section();
    сomplexTag = aComplexTag;
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
  public void transmitUskat2OPC( IAtomicValue aNewVal ) {
    if( !сomplexTag.isBusy() ) {
      // читаем актуальное значение с сервера uSkat
      Gwid gwid = gwid2SectionMap.keys().first();
      ISkRriSection section = gwid2SectionMap.getByKey( gwid );
      IAtomicValue val =
          aNewVal.equals( IAtomicValue.NULL ) ? section.getAttrParamValue( gwid.skid(), gwid.propId() ) : aNewVal;
      // пишем его в OPC
      if( val.asBool() ) {
        lastOPCCmdTimestamp = сomplexTag.setValue( opcCmdIndexOn, val );

      }
      else {
        lastOPCCmdTimestamp = сomplexTag.setValue( opcCmdIndexOff, val );
      }
    }
  }

  @Override
  public EComplexTagState getOpcCmdState() {
    if( сomplexTag == null ) {
      return EComplexTagState.ERROR;
    }
    if( сomplexTag.isBusy() ) {
      return EComplexTagState.PROCESS;
    }

    return lastOPCCmdTimestamp < 0 ? EComplexTagState.UNKNOWN : сomplexTag.getState( lastOPCCmdTimestamp, true );
  }

}

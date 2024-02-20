package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.skf.rri.lib.*;

import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.IComplexTag.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация простейшего передатчика - один тег на одно rri данное. <br>
 * FIXME сделать базовый класс BaseRriDataTransmitter
 *
 * @author dima
 */
public class OneToOneRriDataTransmitter
    implements IRriDataTransmitter {

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( OneToOneRriDataTransmitter.class.getName() );

  /**
   * Тег.
   */
  protected ITag tag;

  /**
   * Установщик нового значения НСИ
   */
  protected IRriSetter rriSetter;

  /**
   * Карта привязки Gwid к НСИ секциям
   */
  protected IMap<Gwid, ISkRriSection> gwid2SectionMap;

  /**
   * Универсальный тег для записи значений в OPC UA
   */
  protected IComplexTag сomplexTag;

  /**
   * index of OPC command
   */
  protected int opcCmdIndex = -1;

  /**
   * timestamp of last OPC command
   */
  protected long lastOPCCmdTimestamp = -1;

  @Override
  public boolean transmit( long aTime ) {
    IAtomicValue newVal = tag.get();

    if( newVal == null || newVal.equals( IAtomicValue.NULL ) || !newVal.isAssigned() ) {
      return false;
    }

    boolean result = false;
    try {
      result = rriSetter.setRriValue( newVal, aTime );
    }
    catch( Exception e ) {
      logger.error( e, "Set RRI attribute error: gwid: %s, tag: %s, error: %s", rriSetter.toString(), tag.tagId(),
          e.getMessage() );
    }
    return result;
  }

  @Override
  public void config( IAvTree aParams ) {
    // читаем код команды OPC
    if( aParams.fields().hasKey( OPC_CMD_INDEX )
        && aParams.fields().getValue( OPC_CMD_INDEX ).atomicType() == EAtomicType.INTEGER ) {
      opcCmdIndex = aParams.fields().getInt( OPC_CMD_INDEX );
    }
    else {
      logger.error(
          "Structure '%s': if tag processor is SingleIntToSingleBoolRriDataTransmitter, then the integer field '%s' must be filled", //$NON-NLS-1$
          aParams.structId(), OPC_CMD_INDEX );
    }
  }

  @Override
  public void start( IRriSetter[] aRriSetters, IList<ITag> aTags, IComplexTag aComplexTag ) {
    rriSetter = aRriSetters[0];
    tag = aTags.get( 0 );
    gwid2SectionMap = rriSetter.gwid2Section();
    сomplexTag = aComplexTag;
  }

  /**
   * @return tag
   */
  public ITag getTag() {
    return tag;
  }

  /**
   * @return dataSetter
   */
  public IRriSetter getRriSetter() {
    return rriSetter;
  }

  @Override
  public IMap<Gwid, ISkRriSection> gwid2Section() {
    return gwid2SectionMap;
  }

  @Override
  public void transmitUskat2OPC() {
    if( !сomplexTag.isBusy() ) {
      // читаем актуальное значение с сервера uSkat
      Gwid gwid = gwid2SectionMap.keys().first();
      ISkRriSection section = gwid2SectionMap.getByKey( gwid );
      IAtomicValue val = section.getAttrParamValue( gwid.skid(), gwid.propId() );
      // пишем его в OPC
      lastOPCCmdTimestamp = сomplexTag.setValue( opcCmdIndex, val );
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

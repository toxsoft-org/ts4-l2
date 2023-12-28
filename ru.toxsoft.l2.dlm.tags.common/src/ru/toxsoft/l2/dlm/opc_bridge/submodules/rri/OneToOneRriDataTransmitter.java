package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.skf.rri.lib.*;

import ru.toxsoft.l2.dlm.opc_bridge.submodules.data.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.RriDataTransmittersInitializer.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация простейшего передатчика - один тег на одно rri данное.
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
   * Установщик нового значения данного
   */
  protected IDataSetter dataSetter;

  /**
   * Карта привязки Gwid к НСИ секциям
   */
  protected IMap<Gwid, ISkRriSection> gwid2SectionMap;

  @Override
  public boolean transmit( long aTime ) {
    IAtomicValue newVal = tag.get();

    if( newVal == null || newVal.equals( IAtomicValue.NULL ) || !newVal.isAssigned() ) {
      return false;
    }

    boolean result = false;
    try {
      result = dataSetter.setDataValue( newVal, aTime );
    }
    catch( Exception e ) {
      logger.error( e, "Set rri data error: gwid: %s, tag: %s, error: %s", dataSetter.toString(), tag.tagId(),
          e.getMessage() );
    }
    return result;
  }

  @Override
  public void config( IAvTree aParams ) {
    // без реализации

  }

  @Override
  public void start( IDataSetter[] aDataSetters, IList<ITag> aTags, IMap<Gwid, ISkRriSection> aGwid2SectionMap ) {
    dataSetter = aDataSetters[0];
    tag = aTags.get( 0 );
    gwid2SectionMap = aGwid2SectionMap;
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
  public IDataSetter getDataSetter() {
    return dataSetter;
  }

  @Override
  public IMap<Gwid, ISkRriSection> gwid2Section() {
    return gwid2SectionMap;
  }

  @Override
  public boolean write2Node( Gwid aRriGwid, IAtomicValue aNewValue ) {
    if( aNewValue == null || aNewValue.equals( IAtomicValue.NULL ) || !aNewValue.isAssigned() ) {
      return false;
    }
    // проверяем что это мой Gwid
    Gwid myGwid = gwid2SectionMap.keys().first();
    if( !myGwid.equals( aRriGwid ) ) {
      return false;
    }
    tag.set( aNewValue );
    return true;
  }

  @Override
  public void transmitAnyWay() {
    IAtomicValue newVal = tag.get();

    if( newVal == null || newVal.equals( IAtomicValue.NULL ) || !newVal.isAssigned() ) {
      return;
    }

    try {
      ((RriSetter)dataSetter).setDataValueAnyway( newVal, System.currentTimeMillis() );
    }
    catch( Exception e ) {
      logger.error( e, "Set rri data error: gwid: %s, tag: %s, error: %s", dataSetter.toString(), tag.tagId(),
          e.getMessage() );
    }

  }

}

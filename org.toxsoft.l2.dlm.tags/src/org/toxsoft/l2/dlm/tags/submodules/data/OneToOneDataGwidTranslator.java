package org.toxsoft.l2.dlm.tags.submodules.data;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Реализация простейшего передатчика - один тег на одно данное.
 *
 * @author max
 */
public class OneToOneDataGwidTranslator
    implements IDataGwidTranslator {

  /**
   * Журнал работы
   */
  private static ILogger logger;

  /**
   * Тег.
   */
  protected IL2Tag tag;

  // private IAtomicValue oldVal = IAtomicValue.NULL;

  /**
   * Номер данного в дата-сете
   */
  protected IGwidValueSetter dataSetter;

  @Override
  public boolean translate( long aTime ) {
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
      result = dataSetter.setGwidValue( newVal, aTime );
    }
    catch( Exception e ) {
      logger.error( e, "Set data error: gwid: %s, tag: %s, error: %s", dataSetter.toString(), tag.id(),
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
  public void start( IGwidValueSetter[] aDataSetindexes, IGwidValueGetter[] aDataGetters, IList<IL2Tag> aTags ) {
    dataSetter = aDataSetindexes[0];
    tag = aTags.get( 0 );
  }

  protected IL2Tag getTag() {
    return tag;
  }

  protected IGwidValueSetter getInDataSetIndex() {
    return dataSetter;
  }

}

package ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация синтетического тега.
 *
 * @author max
 */
public class ComplexTagImpl
    implements IComplexTag {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ComplexTagImpl.class );

  /**
   * Идентификатор тега
   */
  private String id;

  /**
   * Текущий адрес
   */
  private int address;

  /**
   * Текущий адрес
   */
  private long currSetTime = 0;

  /**
   * Теги для установки значений - по одному тегу на тип
   */
  private IMapEdit<EAtomicType, ITag> valTags = new ElemMap<>();

  /**
   * Состояния выполнения установок значений
   */
  private IMapEdit<Long, EComplexTagState> states = new ElemMap<>();

  /**
   * Адресный тег - в него устанавливается идентификатор (адрес), для которого записывается значение
   */
  private ITag addressTag;

  /**
   * Тег обратной связи - адрес считается принявшим значение, если его ид появляется на теге обрантной связи
   */
  private ITag feedbackTag;

  /**
   * Конструктор по вдресному тегу (для передачи адреса или идентификатора) и тегу обратной связи
   *
   * @param aId String - идентификатор
   * @param aAddressTag - Адресный тег
   * @param aFeedbackTag - Тег обратной связи
   */

  public ComplexTagImpl( String aId, ITag aAddressTag, ITag aFeedbackTag ) {
    super();
    id = aId;
    addressTag = aAddressTag;
    feedbackTag = aFeedbackTag;
  }

  @Override
  public boolean isBusy() {
    return addressTag.isDirty() || feedbackTag.isDirty() || feedbackTag.get().asInt() != 0;
  }

  @Override
  public long setValue( int aAddress, IAtomicValue aValue ) {
    if( isBusy() ) {
      return 0;
    }
    currSetTime = System.currentTimeMillis();
    address = aAddress;
    states.put( Long.valueOf( currSetTime ), EComplexTagState.PROCESS );
    addressTag.set( AvUtils.avInt( address ) );
    if( aValue != null && aValue.isAssigned() && aValue != IAtomicValue.NULL
        && valTags.hasKey( aValue.atomicType() ) ) {
      valTags.getByKey( aValue.atomicType() ).set( aValue );

      logger.debug( "Value = %s", aValue.asString() ); //$NON-NLS-1$
    }

    logger.debug( "in do exec isDirty = %s", String.valueOf( addressTag.isDirty() ) ); //$NON-NLS-1$

    return currSetTime;
  }

  @Override
  public EComplexTagState getState( long aSetTime, boolean aDelIfCan ) {
    if( states.hasKey( Long.valueOf( aSetTime ) ) ) {
      EComplexTagState result = states.getByKey( Long.valueOf( aSetTime ) );

      if( aDelIfCan && result.isCanBeDeleted() ) {
        states.removeByKey( Long.valueOf( aSetTime ) );
      }
      return result;
    }

    return EComplexTagState.UNKNOWN;
  }

  protected void doJob() {
    if( address != 0 ) {
      logger.debug( "DoJob  currentCmd != null" ); //$NON-NLS-1$
      logger.debug( "in do job isDirty = %s", String.valueOf( addressTag.isDirty() ) ); //$NON-NLS-1$

      if( !addressTag.isDirty() && !feedbackTag.isDirty() && feedbackTag.get().asInt() == address ) {
        addressTag.set( AvUtils.avInt( 0 ) );
        states.put( Long.valueOf( currSetTime ), EComplexTagState.DONE );
        address = 0;
        currSetTime = 0;
      }
    }

  }

  protected void setValueTypeTag( EAtomicType aType, ITag aValTag ) {
    valTags.put( aType, aValTag );
  }

  @Override
  public String id() {
    return id;
  }
}

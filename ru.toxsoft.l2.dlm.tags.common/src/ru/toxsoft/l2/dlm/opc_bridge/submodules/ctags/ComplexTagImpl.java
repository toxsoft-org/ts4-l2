package ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация синтетического тега.
 *
 * @author max
 */
public class ComplexTagImpl
    extends AbstractComplexTag {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( ComplexTagImpl.class );

  public static final String СT_WRITE_ID_TAG         = "write.id.tag";
  public static final String СT_WRITE_VAL_TAG_PREFIX = "write.param.";
  public static final String СT_WRITE_VAL_TAG_FORMAT = СT_WRITE_VAL_TAG_PREFIX + "%s.tag";
  public static final String СT_READ_FEEDBACK_TAG    = "read.feedback.tag";

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

  private IAvTree cfgData;

  /**
   * Конструктор по вдресному тегу (для передачи адреса или идентификатора) и тегу обратной связи
   *
   * @param aId String - идентификатор
   */

  public ComplexTagImpl( String aId ) {
    super( aId );

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

  @Override
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
      else {
        // dima 25.09.2024
        // Тег в работе, проверяем не пора ли по таймату заканчивать
        if( System.currentTimeMillis() - currSetTime > 3000 ) {
          addressTag.set( AvUtils.avInt( 0 ) );
          states.put( Long.valueOf( currSetTime ), EComplexTagState.TIMEOUT );
          address = 0;
          currSetTime = 0;
        }
      }
    }

  }

  /**
   * @param aFeedbackTag - Тег обратной связи
   */
  private void setFeedbackTag( ITag aFeedbackTag ) {
    feedbackTag = aFeedbackTag;
  }

  /**
   * @param aAddressTag - Адресный тег
   */
  private void setAddressTag( ITag aAddressTag ) {
    addressTag = aAddressTag;
  }

  private void setValueTypeTag( EAtomicType aType, ITag aValTag ) {
    valTags.put( aType, aValTag );
  }

  @Override
  protected void config( IAvTree aCfg ) {
    cfgData = aCfg;
  }

  @Override
  protected void start( IDlmContext aContext ) {
    // Search Device
    String deviceId = cfgData.fields().getStr( TAG_DEVICE_ID );
    ITsOpc tagsDevice = (ITsOpc)aContext.hal().listSpecificDevices().getByKey( deviceId );

    // tag for address write
    ITag wIdTag = tagsDevice.tag( cfgData.fields().getStr( СT_WRITE_ID_TAG ) );
    setAddressTag( wIdTag );

    // feedback tag
    ITag fbTag = tagsDevice.tag( cfgData.fields().getStr( СT_READ_FEEDBACK_TAG ) );
    setFeedbackTag( fbTag );

    IStringList cfgKeys = cfgData.fields().keys();

    for( String cfgkey : cfgKeys ) {
      if( cfgkey.startsWith( СT_WRITE_VAL_TAG_PREFIX ) ) {
        EAtomicType atomType = null;
        for( EAtomicType aType : EAtomicType.values() ) {
          if( cfgkey.equals( String.format( СT_WRITE_VAL_TAG_FORMAT, aType.id().toLowerCase() ) ) ) {
            atomType = aType;
            break;
          }
        }
        TsIllegalArgumentRtException.checkNull( atomType, "cant find any type for : " + cfgkey );

        // atomic type tag
        ITag wAtomTypeValTag = tagsDevice.tag( cfgData.fields().getStr( cfgkey ) );
        // dima 08.09.25 situation when there is no tag of type float/integer is acceptable
        if( wAtomTypeValTag != null ) {
          setValueTypeTag( atomType, wAtomTypeValTag );
        }
      }
    }
    // dima 08.09.25 force 0 to address tag
    addressTag.set( AvUtils.avInt( 0 ) );
  }

}

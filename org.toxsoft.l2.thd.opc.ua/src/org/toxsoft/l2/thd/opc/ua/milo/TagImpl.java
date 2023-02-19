package org.toxsoft.l2.thd.opc.ua.milo;

import org.toxsoft.core.log4j.*;

//import static ru.toxsoft.l2.thd.opc.da.IL2Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Тег OPC сервера
 *
 * @author Dima
 */
public class TagImpl
    implements ITag {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * id тега в OPC сервере
   */
  private String tagId;

  /**
   * Тип тега
   */
  private EKind kind;

  /**
   * Тип значения тега
   */
  private EAtomicType valType;

  /**
   * Текущее значение тега
   */
  IAtomicValue value = IAtomicValue.NULL;

  /**
   * Новое значение тега которое пытемся установить
   */
  IAtomicValue newValue = IAtomicValue.NULL;

  /**
   * Признак установки нового значения тега
   */
  private boolean dirty = false;

  /**
   * имя тега (опционально)
   */
  private String name;

  /**
   * описание тега (опционально)
   */
  private String description;

  /**
   * @param aTagId id тега на OPC
   * @param aKind тип тега (на чтение/запись/туда-сюда)
   * @param aValType тип значения
   */
  public TagImpl( String aTagId, EKind aKind, EAtomicType aValType ) {
    tagId = aTagId;
    kind = aKind;
    valType = aValType;
  }

  @Override
  public String tagId() {
    return tagId;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public EKind kind() {
    return kind;
  }

  void setKind( EKind aKind ) {
    kind = aKind;
  }

  @Override
  public EAtomicType valueType() {
    return valType;

  }

  @Override
  public IAtomicValue get() {
    return value;
  }

  @Override
  public void set( IAtomicValue aVal ) {
    if( kind.compareTo( EKind.R ) == 0 ) {
      throw new TsIllegalStateRtException( "ERROR_TRY_SET_READ_ONLY_TAG" );
    }
    // Имеет смысл напрягатся, только если новое значение отличается от текущего
    if( value.compareTo( aVal ) != 0 ) {
      setDirty( true );
      newValue = aVal;
    }
  }

  @Override
  public String id() {
    return tagId;
  }

  @Override
  public String description() {
    return description;
  }

  /**
   * @return признак установки нового значения
   */
  @Override
  public boolean isDirty() {
    return dirty;
  }

  /**
   * @param aDirty установка/сброс признака нового занчения
   */
  void setDirty( boolean aDirty ) {
    dirty = aDirty;
    // if( !dirty ) {
    // newValue = IAtomicValue.NULL;
    // }
  }

  /**
   * Обновляет значение тега значением считанным с OPC
   *
   * @param aVal значение считанное с OPC
   */
  void updateVal( IAtomicValue aVal ) {
    if( !isDirty() ) {
      if( aVal != null && !value.equals( aVal ) ) {
        logger.debug( "node %s changed value = %s", tagId, aVal.asString() );
      }
      value = aVal != null ? aVal : IAtomicValue.NULL;
    }
  }

  @Override
  public String nmName() {
    return tagId;
  }
}

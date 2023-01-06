package org.toxsoft.l2.thd.opc.ua.milo;

import org.toxsoft.core.tslib.av.*;

import ge.toxsoft.gwp.opcuabridge.*;

/**
 * Реализация тега на запись, потокобезопасная
 *
 * @author max
 */
public class WriteOpcUaTag
    implements IWriteTag {

  private IAtomicValue value;

  private String id;

  private String name;

  private String description;

  private EAtomicType type;

  /**
   * Конструктор тега по идентификатору, визуальному имени, описанию и типу
   *
   * @param aId
   * @param aName
   * @param aDescription
   * @param aType
   */
  public WriteOpcUaTag( String aId, String aName, String aDescription, EAtomicType aType ) {
    super();
    id = aId;
    name = aName;
    description = aDescription;
    type = aType;
  }

  /**
   * Конструктор тега по идентификатору, визуальному имени, описанию и типу
   *
   * @param aId
   * @param aName
   * @param aType
   */
  public WriteOpcUaTag( String aId, String aName, EAtomicType aType ) {
    this( aId, aName, aName, aType );

  }

  @Override
  public EAtomicType type() {
    return type;
  }

  protected IAtomicValue getValue() {
    return value;
  }

  @Override
  public void setValue( IAtomicValue aValue ) {
    value = aValue;
  }

  @Override
  public String id() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String nmName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String description() {
    // TODO Auto-generated method stub
    return null;
  }

}

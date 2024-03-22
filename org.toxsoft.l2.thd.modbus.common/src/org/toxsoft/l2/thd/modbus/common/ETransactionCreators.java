package org.toxsoft.l2.thd.modbus.common;

import static org.toxsoft.l2.thd.modbus.common.IModbusThdConstants.*;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.l2.thd.modbus.common.connections.*;

/**
 * Список создателей modbus транзакций (через modbus соединение).
 *
 * @author max
 */
public enum ETransactionCreators
    implements IStridable {

  /**
   * Rtu соединие modbus
   */
  RTU_TRANSACTION_CREATOR( RTU_TRANSACTION_CREATOR_ID, RTU_TRANSACTION_CREATOR_DESCR, RtuTransactionCreator.class )

  ;

  private String id;

  private String descr;

  private Class<? extends ITransactionCreator> creatorClass;

  ETransactionCreators( String aId, String aDescr, Class<? extends ITransactionCreator> aCreatorClass ) {
    this.id = aId;
    this.descr = aDescr;

    this.creatorClass = aCreatorClass;

  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String description() {
    return descr;
  }

  /**
   * Создаёт создателя транзакций (через modbus соединение)
   *
   * @param aCreator String - идентификатор modbus соединения (rtu, tcp, ...) или имя java-класса.
   * @return ITransactionCreator - создатель транзакций.
   * @throws Exception - ошибка создания транзакции (любая)
   */
  @SuppressWarnings( { "unchecked" } )
  public static ITransactionCreator createTransactionCreator( String aCreator )
      throws Exception {
    // if(aCreator.equals("rtu")) {
    // return new RtuTransactionCreator();
    // }
    // return null;

    Class<? extends ITransactionCreator> creatorClass = null;

    for( ETransactionCreators creator : values() ) {
      if( creator.id().equals( aCreator ) ) {
        creatorClass = creator.creatorClass;
        break;
      }
    }

    if( creatorClass == null ) {
      creatorClass = (Class<ITransactionCreator>)Class.forName( aCreator );
    }

    // return creatorClass.newInstance();
    return creatorClass.getConstructor().newInstance();
  }

  @Override
  public String nmName() {
    return id;
  }
}

package org.toxsoft.l2.thd.opc.ua.milo;

import org.toxsoft.core.tslib.av.avtree.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация UA моста opc 2 S5 на движке milo для проекта Байконур
 *
 * @author dima
 */
public class BknOpcUaMiloDriver
    extends OpcUaMiloDriver {

  /**
   * @param aId String - строковый идентификатор.
   * @param aDescription String - описание.
   * @param aErrorProcessor {@link IHalErrorProcessor} - обработчик ошибок, возникающих при работе с НУ.
   * @param aCfgInfo - configuration tree
   */
  public BknOpcUaMiloDriver( String aId, String aDescription, IHalErrorProcessor aErrorProcessor, IAvTree aCfgInfo ) {
    super( aId, aDescription, aErrorProcessor, aCfgInfo );
  }

  @Override
  public int getHealth() {
    int currHealth = super.getHealth();
    if( currHealth == 0 ) {
      return currHealth;
    }
    // dima 24.04.25 read tags noLink_TKA*
    ITag noLinkTKA1 = opcUaNodesReader.getTag( "ns=3;s=\"noLink_TKA1\"" );
    if( noLinkTKA1.get().asBool() ) {
      currHealth -= 33;
    }
    ITag noLinkTKA2 = opcUaNodesReader.getTag( "ns=3;s=\"noLink_TKA2\"" );
    if( noLinkTKA2.get().asBool() ) {
      currHealth -= 33;
    }
    ITag noLinkTKA3 = opcUaNodesReader.getTag( "ns=3;s=\"noLink_TKA3\"" );
    if( noLinkTKA3.get().asBool() ) {
      currHealth -= 33;
    }
    return currHealth;
  }

}

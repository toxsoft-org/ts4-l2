package ru.toxsoft.l2.thd.opc.da;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Инкапсуляция нескольких мостов opc2S5 подключенных к контроллеру. Просто контейнер который содержит все мосты opc2S5
 * c которыми идет работа программы НУ
 *
 * @author dima
 */
public class Opc2S5Collection
    extends AbstractSpecificDevice
    implements ITsOpc {

  /**
   * Мосты подключенные к контроллеру
   */
  private IListEdit<IOpc2S5Bridge> bridges;
  /**
   * Полный список OPC тегов системы
   */
  private StringMap<ITag>          tags = new StringMap<>();

  /**
   * Конструктор по идентификатору, описанию и обработчику ошибок.
   *
   * @param aId String - идентификатор.
   * @param aDescription String - описание.
   * @param aErrorProcessor IHalErrorProcessor - обработчик ошибок.
   * @param aBridges список подключенных мостов с OPC
   */
  public Opc2S5Collection( String aId, String aDescription, IHalErrorProcessor aErrorProcessor,
      IList<IOpc2S5Bridge> aBridges ) {
    super( aId, aDescription, aErrorProcessor );
    bridges = new ElemArrayList<>( false );
    bridges.addAll( aBridges );
    for( IOpc2S5Bridge bridge : bridges ) {
      tags.putAll( bridge.tags() );
    }
  }

  @Override
  protected void readValuesFromLL()
      throws TsMultipleApparatRtException {
    for( IOpc2S5Bridge bridge : bridges ) {
      bridge.readValuesFromLL();
    }
  }

  @Override
  protected void writeValuesOnLL()
      throws TsMultipleApparatRtException {
    for( IOpc2S5Bridge bridge : bridges ) {
      bridge.writeValuesOnLL();
    }
  }

  @Override
  protected void putInBufferOutputValues() {
    for( IOpc2S5Bridge bridge : bridges ) {
      bridge.putInBufferOutputValues();
    }
  }

  @Override
  protected void getFromBufferInputValues() {
    for( IOpc2S5Bridge bridge : bridges ) {
      bridge.getFromBufferInputValues();
    }
  }

  @Override
  protected void closeApparatResources()
      throws Exception {
    for( IOpc2S5Bridge bridge : bridges ) {
      bridge.closeApparatResources();
    }
  }

  @Override
  public ITag tag( String aTagId ) {
    return tags.getByKey( aTagId );
  }

  @Override
  public IStringMap<ITag> tags() {
    return tags;
  }
}

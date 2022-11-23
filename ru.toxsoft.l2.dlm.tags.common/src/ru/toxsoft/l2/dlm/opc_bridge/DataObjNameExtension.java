package ru.toxsoft.l2.dlm.opc_bridge;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.tslib.av.opset.*;

import ru.toxsoft.l2.core.util.*;

/**
 * Класс, расширяющий класс описания данного = добавлены признаки историчности и текущих данных.
 *
 * @author MAX
 */
public class DataObjNameExtension
    extends DataObjName {

  private boolean hist = false;

  private boolean curr = false;

  private int synch_period = 0;

  private boolean synch = false;

  /**
   * Конструктор по параметрам инициализации, содержащихся в наборе.
   *
   * @param aInitParams {@link IOptionSet} - набор параметров инициализации.
   */
  public DataObjNameExtension( IOptionSet aInitParams ) {
    super( aInitParams.getStr( CLASS_ID ), aInitParams.getStr( OBJ_NAME ), aInitParams.getStr( DATA_ID ) );
    synch_period = aInitParams.hasValue( SYNCH_PERIOD ) ? aInitParams.getInt( SYNCH_PERIOD ) : 0;
    synch = synch_period > 0;
    curr = aInitParams.hasValue( IS_CURR ) ? aInitParams.getBool( IS_CURR ) : false;
    hist = aInitParams.hasValue( IS_HIST ) ? aInitParams.getBool( IS_HIST ) : false;
  }

  /**
   * Возвращает признак, что данное является историческим.
   *
   * @return <code>true</code> - данное является историческим, <code>false</code> - данное не является историческим.
   */
  public boolean isHist() {
    return hist;
  }

  /**
   * Возвращает признак, что данное является текущим.
   *
   * @return <code>true</code> - данное является текущим, <code>false</code> - данное не является текущим.
   */
  public boolean isCurr() {
    return curr;
  }

  /**
   * Возвращает период обновления синхронных данных.
   *
   * @return int - период обновления синхронных данных, если <=0, то данное не является синхронным.
   */
  public int getSynchPeriod() {
    return synch_period;
  }

  /**
   * Возвращает признак, что данное является синхронным.
   *
   * @return <code>true</code> - данное является синхронным, <code>false</code> - данное не является синхронным.
   */
  public boolean isSynch() {
    return synch;
  }

}

package org.toxsoft.l2.dlm.tags;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * TODO - на самом деле это или должен его использовать - #L2GwidTranslatorCfg <br>
 * Класс данного, содержащий признаки историчности, синхронности и текущих данных.
 *
 * @author MAX
 */
public class GwidTranslatorCfgExtension {

  /**
   * Журнал работы
   */
  private ILogger logger;

  private boolean hist = false;

  private boolean curr = false;

  private long synch_period = 0;

  private boolean synch = false;

  private Gwid gwid;

  private IOptionSet options;

  /**
   * Конструктор по параметрам инициализации, содержащихся в наборе.
   *
   * @param aInitParams {@link IOptionSet} - набор параметров инициализации.
   */
  public GwidTranslatorCfgExtension( IOptionSet aOptions ) {
    options = aOptions;
  }

  public void loadOptionsFromSkConn( IL2SharedContext aContext ) {
    gwid = Gwid.createRtdata( options.getStr( CLASS_ID ), options.getStr( OBJ_NAME ), options.getStr( DATA_ID ) );

    ISkConnection conn = aContext.net().getSkConnection();

    ISkClassInfo clsInfo = conn.coreApi().sysdescr().findClassInfo( gwid.classId() );

    if( clsInfo == null ) {
      logger.error( "Class %s not found", gwid.classId() );
      // Продолжаем цикл, чтобы получить все ошибки за один проход
      return;
    }
    if( !clsInfo.rtdata().list().hasKey( gwid.propId() ) ) {
      logger.error( "RtData %s - %s not found", gwid.classId(), gwid.propId() );
      // Продолжаем цикл, чтобы получить все ошибки за один проход
      return;
    }
    // описание данного
    IDtoRtdataInfo dataInfo = clsInfo.rtdata().list().getByKey( gwid.propId() );

    synch_period = options.hasValue( SYNCH_PERIOD ) ? options.getInt( SYNCH_PERIOD ) : dataInfo.syncDataDeltaT();
    synch = synch_period > 0;
    curr = options.hasValue( IS_CURR ) ? options.getBool( IS_CURR ) : dataInfo.isCurr();
    hist = options.hasValue( IS_HIST ) ? options.getBool( IS_HIST ) : dataInfo.isHist();
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
  public long getSynchPeriod() {
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

  public Gwid getGwid() {
    return gwid;
  }

  public String getClassId() {
    return gwid.classId();
  }

  public String getObjName() {
    return gwid.strid();
  }

  public String getDataId() {
    return gwid.propId();
  }

}

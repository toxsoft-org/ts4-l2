package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.data.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;

/**
 * Модуль работы с историческими данными.
 *
 * @author max
 */
public class OpcHistDataModule
    extends ConfigurableWorkerModuleBase {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст..
   */
  IDlmContext context;

  /**
   * Информация о модуле DLM
   */
  private IDlmInfo dlmInfo;

  /**
   * Инициализатор.
   */
  private IDataTransmittersInitializer<ISkWriteHistDataChannel> initializer;

  /**
   * Набор описаний данных, полученный из конфиг информации.
   */
  private IList<IDataTransmitter<ISkWriteHistDataChannel>> pinDataTransmitters;

  /**
   * Набор выходных исторических данных.
   */
  IMap<Gwid, IDataSetter> wHistDataSet;

  /**
   * Метка времени последней записи данных на сервер
   */
  private long lastWriteStamp  = System.currentTimeMillis();
  /**
   * Период между записью данными на сервер
   */
  private long dataWritePeriod = 15000L;

  /**
   * Конструктор по DLM контексту
   *
   * @param aContext IDlmContext - контекст.
   * @param aDlmInfo IDlmInfo - информация о DLM
   * @param aIinitializer - инициализатор пинов.
   */
  public OpcHistDataModule( IDlmContext aContext, IDlmInfo aDlmInfo,
      IDataTransmittersInitializer<ISkWriteHistDataChannel> aIinitializer ) {
    dlmInfo = aDlmInfo;
    context = aContext;
    initializer = aIinitializer;
  }

  @Override
  protected void doConfigYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {

    IAvTree dataDefs = aConfig.params().nodes().findByKey( DATA_DEFS );

    if( dataDefs != null && dataDefs.isArray() ) {
      for( int i = 0; i < dataDefs.arrayLength(); i++ ) {
        // описание команд одного класса
        IAvTree oneDataDef = dataDefs.arrayElement( i );

        // try {
        // if( oneDataDef.fields().hasValue( IS_HIST ) && oneDataDef.fields().getBool( IS_HIST ) ) {
        initializer.addDataConfigParamsForTransmitter( oneDataDef );
        // }
        // }
        // catch( TsItemNotFoundRtException | TsUnsupportedFeatureRtException | DvTypeCastRtException e ) {
        // LoggerUtils.defaultLogger().error( e );
        // // при наступлении ошибки выйти - конфигурация завершена не была
        // return;
        // }
      }
    }

  }

  @Override
  protected void doStartComponent() {

    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_HISTORY_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    // IS5Connection connection = context.network().getConnection();
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    // инициализирует с помощью конфигуратора основные сущности (на данном этапе идёт выборка информации с сервера)
    initializer.initialize( context );

    // создаём датасет текущих данных на запись
    wHistDataSet = initializer.getDataSetters();
    // wHistDataSet.setTimeInterval( System.currentTimeMillis(), TimeUtils.MAX_TIMESTAMP );

    pinDataTransmitters = initializer.getDataTransmitters();

    // Обнуление за ненадобностью
    initializer = null;

    logger.info( MSG_HISTORY_DATA_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );
  }

  @Override
  protected void doDoJob() {

    // установка корректного интервала записи
    long currTime = System.currentTimeMillis();

    boolean doHistWrite = false;

    // wHistDataSet.setTimeInterval( currTime, currTime );

    for( IDataTransmitter<ISkWriteHistDataChannel> transmitter : pinDataTransmitters ) {
      doHistWrite |= transmitter.transmit( currTime );
    }

    // if( doHistWrite ) {
    // wHistDataSet.write();
    // }

    if( currTime - lastWriteStamp > dataWritePeriod || currTime < lastWriteStamp ) {

      // mvk: асинхронная передача накопленных значений
      // wHistDataSet.writeAsync(); //TODO
      // wHistDataSet.setInterval( new QueryInterval( EQueryIntervalType.CSCE, currTime, TimeUtils.MAX_TIMESTAMP ) );

      lastWriteStamp = currTime;

      if( doHistWrite ) {
        // long s0 = System.currentTimeMillis();
        // // передача накопленных значений
        for( IDataSetter setter : wHistDataSet.values() ) {
          setter.sendOnServer();
        }
        // long s1 = System.currentTimeMillis();
        // logger.info( "wHistDataSet.write() COUNT SET = %d in %s", testSetCount, dlmInfo.moduleId() );
        // testSetCount = 0;
        // if( s1 - s0 > 900L ) {
        // logger.error( "HIST WRITE DURATION %d in %s", new Long( s1 - s0 ), dlmInfo.moduleId() );
        // }
        // }
        //
        // long s1 = System.currentTimeMillis();
        //
        // if( s1 - s0 > 900L ) {
        // logger.error( "HIST WRITE DURATION %d in %s", new Long( s1 - s0 ), dlmInfo.moduleId() ); //$NON-NLS-1$
      }
    }

  }

  @Override
  protected boolean doQueryStop() {
    if( wHistDataSet != null ) {
      for( IDataSetter c : wHistDataSet.values() ) {
        c.close();
      }
    }
    return true;
  }

}

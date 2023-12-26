package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.skf.legacy.*;
import org.toxsoft.skf.rri.lib.*;
import org.toxsoft.skf.rri.lib.impl.*;
import org.toxsoft.uskat.concurrent.*;
import org.toxsoft.uskat.core.api.evserv.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.data.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Модуль работы с RRI данными.
 *
 * @author dima
 */
public class OpcRriDataModule
    extends ConfigurableWorkerModuleBase
    implements ISkRriSectionListener {

  /**
   * Журнал работы
   */
  ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

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
  private IRriDataTransmittersInitializer initializer;

  /**
   * Набор описаний данных, полученный из конфиг информации.
   */
  private IList<IRriDataTransmitter> pinRriDataTransmitters;

  /**
   * Карта Gwid -> IDataSetter.
   */
  IMap<Gwid, IDataSetter> gwid2DataSetterMap;

  /**
   * Конструктор по DLM контексту
   *
   * @param aContext {@link IDlmContext} - контекст.
   * @param aDlmInfo {@link IDlmInfo} - информация о DLM
   * @param aInitializer {@link IRriDataTransmittersInitializer} - инициализатор пинов.
   */
  public OpcRriDataModule( IDlmContext aContext, IDlmInfo aDlmInfo, IRriDataTransmittersInitializer aInitializer ) {
    dlmInfo = aDlmInfo;
    context = aContext;
    initializer = aInitializer;
  }

  @Override
  protected void doConfigYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {

    IAvTree rriDefs = aConfig.params().nodes().findByKey( RRI_DEFS );

    // наполнение конфигуратора данными (для данных НСИ)
    if( rriDefs != null && rriDefs.isArray() ) {
      for( int i = 0; i < rriDefs.arrayLength(); i++ ) {
        // описание одного НСИ даннного
        IAvTree oneDataDef = rriDefs.arrayElement( i );

        initializer.addDataConfigParamsForTransmitter( oneDataDef );
      }
    }

  }

  @Override
  protected void doStartComponent() {

    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_RRI_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    // регистрируем службу НСИ
    S5SynchronizedRegRefInfoService rriService =
        new S5SynchronizedRegRefInfoService( (S5SynchronizedConnection)context.network().getSkConnection() );
    logger.info( "%s", rriService );

    // инициализирует с помощью конфигуратора основные сущности (на данном этапе идёт выборка информации с сервера)
    initializer.initialize( context );

    // получение карты Gwid -> IDataSetter
    gwid2DataSetterMap = initializer.getDataSetters();

    // получение объектов, отвечающих за передачу сигнала с единичного пина на единичное данное
    pinRriDataTransmitters = initializer.getDataTransmitters();
    logger.debug( "PinRriDataTransmitters: %s ", String.valueOf( pinRriDataTransmitters.size() ) );
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      // подписываемся по изменения в своих секциях НСИ
      for( ISkRriSection section : transmitter.gwid2Section().values() ) {
        section.eventer().addListener( this );
      }
      if( transmitter instanceof OneToOneRriDataTransmitter ) {
        ITag tag = ((OneToOneRriDataTransmitter)transmitter).getTag();
        IDataSetter dataSetter = ((OneToOneRriDataTransmitter)transmitter).getDataSetter();
        logger.debug( "Tag: %s, IDataSetter: %s", tag.id(), dataSetter.toString() );
      }
    }

    // Обнуление за ненадобностью
    initializer = null;
    logger.info( MSG_RRI_DATA_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );
  }

  @Override
  protected void doDoJob() {

    // текущее время - чтоб у всех данных было одно время
    long currTime = System.currentTimeMillis();

    boolean doCurrWrite = false;
    // выполнение работы с каждым передатчиком с проверкой изменения данных
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      try {
        doCurrWrite |= transmitter.transmit( currTime );
      }
      catch( Exception e ) {
        logger.error( e.getMessage() );
      }
    }
    if( doCurrWrite ) {
      // wCurrDataSet.write(); //TODO

      // try {
      // context.network().getSkConnection().coreApi().rtdService().writeCurrValues();
      // }
      // catch( Exception e ) {
      // logger.error( e, "Cant transfer curr data to server" ); //$NON-NLS-1$
      // }
    }

  }

  @Override
  protected boolean doQueryStop() {
    if( gwid2DataSetterMap != null ) {
      for( IDataSetter c : gwid2DataSetterMap.values() ) {
        c.close();
      }
      logger.info( "RRI data setters are closed, size = %d", Integer.valueOf( gwid2DataSetterMap.size() ) ); //$NON-NLS-1$
      gwid2DataSetterMap = null;
    }
    // отписываемся от нотификаций
    pinRriDataTransmitters = initializer.getDataTransmitters();
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      // подписываемся по изменения в своих секциях НСИ
      for( ISkRriSection section : transmitter.gwid2Section().values() ) {
        section.eventer().removeListener( this );
      }
    }

    return true;
  }

  @Override
  public void onSectionPropsChanged( ISkRriSection aSource ) {
    // nop
  }

  @Override
  public void onClassParamInfosChanged( ISkRriSection aSource, String aClassId ) {
    // nop
  }

  @Override
  public void onParamValuesChanged( ISkRriSection aSource, IList<SkEvent> aEvents ) {
    // находим свой tracmitter и пишем в него новое значение
    pinRriDataTransmitters = initializer.getDataTransmitters();
    for( IRriDataTransmitter transmitter : pinRriDataTransmitters ) {
      for( ISkRriSection section : transmitter.gwid2Section().values() ) {
        if( section.equals( aSource ) ) {
          for( SkEvent event : aEvents ) {
            Gwid parGwid = event.eventGwid();
            IAtomicValue newVal = event.paramValues().findByKey( ISkRegRefServiceHardConstants.EVPRMID_NEW_VAL_ATTR );
            transmitter.write2Node( parGwid, newVal );
          }
        }
      }
    }
  }

}

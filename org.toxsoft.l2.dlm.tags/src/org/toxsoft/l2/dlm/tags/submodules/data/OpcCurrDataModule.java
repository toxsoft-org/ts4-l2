package org.toxsoft.l2.dlm.tags.submodules.data;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;
import static org.toxsoft.l2.dlm.tags.submodules.data.IL2Resources.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.dlms.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Модуль работы с текущими данными.
 *
 * @author max
 */
public class OpcCurrDataModule
    extends AbstractTsCoopCompMultiUse {

  /**
   * Журнал работы
   */
  private ILogger logger;

  /**
   * Контекст..
   */
  private IL2SharedContext context;

  /**
   * Информация о модуле DLM
   */
  private DlmInfo dlmInfo;

  /**
   * Инициализатор.
   */
  private IDataTranslatorsInitializer initializer;

  /**
   * Набор описаний данных, полученный из конфиг информации.
   */
  private IList<IDataGwidTranslator> pinDataTransmitters;

  /**
   * Набор выходных текущих данных.
   */
  IList<IDataSetter> wCurrDataSet;

  /**
   * Конструктор по DLM контексту
   *
   * @param aContext IDlmContext - контекст.
   * @param aDlmInfo IDlmInfo - информация о DLM
   * @param aInitializer IPinDataInitializer - инициализатор пинов.
   */
  public OpcCurrDataModule( IL2SharedContext aContext, DlmInfo aDlmInfo ) {
    dlmInfo = aDlmInfo;
    context = aContext;
  }

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    IAvTree dataDefs = (IAvTree)aArgs.get( DATA_DEFS );

    // наполнение конфигуратора данными (для текущих данных)
    if( dataDefs != null && dataDefs.isArray() ) {
      for( int i = 0; i < dataDefs.arrayLength(); i++ ) {
        // описание одного даннного класса
        IAvTree oneDataDef = dataDefs.arrayElement( i );

        try {
          initializer.addDataConfigParamsForTransmitter( oneDataDef );
        }
        catch( Exception e ) {
          logger.error( e );
          // при наступлении ошибки выйти - конфигурация завершена не была
          return ValidationResult.error( e );
        }
      }
    }
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doStart() {
    boolean isConfigured = true;
    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured, ERR_MSG_CURR_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    ISkConnection connection = context.net().getSkConnection();
    TsIllegalStateRtException.checkFalse( connection.state() != ESkConnState.ACTIVE, "Not connected" );

    // инициализирует с помощью конфигуратора основные сущности (на данном этапе идёт выборка информации с сервера)
    initializer.initialize( context );

    // получение датасета текущих данных на запись
    wCurrDataSet = initializer.getDataSetters().values();

    // получение объектов, отвечающих за передачу сигнала с единичного пина на единичное данное
    pinDataTransmitters = initializer.getDataGwidTranslators();

    // test log
    logger.debug( "PinDataTransmitters: %s ", String.valueOf( pinDataTransmitters.size() ) );
    for( IDataGwidTranslator transmitter : pinDataTransmitters ) {
      if( transmitter instanceof OneToOneDataGwidTranslator ) {
        IL2Tag tag = null;// = ((OneToOneDataTransmitter)transmitter).getTag();
        IDataSetter dataSet = ((OneToOneDataGwidTranslator)transmitter).getInDataSetIndex();
        if( tag != null ) {
          logger.debug( "Tag: %s, Set: %s", tag.id(), dataSet.toString() );
        }
      }
    }

    // регистрация слушателя состояния соединеня с целью обозначения данных в сервисе качества
    // context.network().getSkConnection().addConnectionListener( connectionListener );

    logger.info( MSG_CURR_DATA_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );

  }

  @Override
  protected void doDoJob() {

    // String tagsSpecDev = "aTransTagsParams.getStr( TAG_DEVICE_ID )";// TODO list
    // IHealthMeasurable tagsDevice = (IHealthMeasurable)context.hal().listSpecificDevices().getByKey( tagsSpecDev );

    // текущее время - чтоб у всех данных было одно время
    long currTime = System.currentTimeMillis();

    boolean doCurrWrite = false;
    // выполнение работы каждым передатчиком с проверкой изменения данных
    for( IDataGwidTranslator transmitter : pinDataTransmitters ) {
      try {
        doCurrWrite |= transmitter.transmit( currTime );
      }
      catch( Exception e ) {
        logger.error( e.getMessage() );
      }
    }

  }

  @Override
  protected boolean doQueryStop() {
    if( wCurrDataSet != null ) {
      for( IDataSetter c : wCurrDataSet ) {
        c.close();
      }
      logger.info( "Curr data channels are closed, size = %d", Integer.valueOf( wCurrDataSet.size() ) ); //$NON-NLS-1$
      wCurrDataSet = null;
    }
    return true;
  }

}

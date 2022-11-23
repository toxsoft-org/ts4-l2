package ru.toxsoft.l2.core.hal.impl;

import static ru.toxsoft.l2.core.hal.IHalHardConstants.*;
import static ru.toxsoft.l2.core.hal.impl.IL2Resources.*;

import java.io.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.cfg.impl.*;
import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.core.main.*;
import ru.toxsoft.l2.core.main.impl.*;

/**
 * Реализация уровня абстрагирования от железа блока управления.
 *
 * @author max
 */
public class HalImpl
    extends AbstractL2Component
    implements IHalComponent {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Глобальный контекст.
   */
  private IGlobalContext globalContext;

  /**
   * Список всех зарегистрированных специфических устройств.
   */
  private IStridablesListEdit<ISpecificDevice> specificDevices;

  /**
   * Список всех зарегистрированных уникальных аппаратов в виде их абстрактной реализации - для доступа к обязательному,
   * но не API-шному функционалу - а именно для вызова метода работы с низкоуровневыми протоколами устройств.
   */
  private IStridablesListEdit<AbstractHalIoDevice> operationDevices;

  /**
   * Список всех зарегистрированных пинов в виде их абстрактной реализации - для доступа к обязательному, но не
   * API-шному функционалу - а именно для вызова метода работы с низкоуровневыми протоколами устройств.
   */
  private IStridablesListEdit<AbstractPin> pins;

  /**
   * Список всех зарегистрированных аналоговых входов в виде их абстрактной реализации.
   */
  private IStridablesListEdit<AbstractPin> aiPins;

  /**
   * Список всех зарегистрированных дискретных входов в виде их абстрактной реализации.
   */
  private IStridablesListEdit<AbstractPin> diPins;

  /**
   * Список всех зарегистрированных аналоговых выходов в виде их абстрактной реализации.
   */
  private IStridablesListEdit<AbstractPin> aoPins;

  /**
   * Список всех зарегистрированных дикретных выходов в виде их абстрактной реализации.
   */
  private IStridablesListEdit<AbstractPin> doPins;

  /**
   * Параметры настройки HALa.
   */
  private final OptionSet halOps;

  /**
   * Список резервируемых устройств.
   */
  private IStringListEdit reservantDevicesIds = new StringArrayList();

  /**
   * Конструктор по глобальному контексту.
   *
   * @param aGlobalContext GlobalContext - глобальный контекст.
   */
  public HalImpl( GlobalContext aGlobalContext ) {
    super( aGlobalContext );
    globalContext = aGlobalContext;

    // инициализация списков.
    specificDevices = new StridablesList<>();
    operationDevices = new StridablesList<>();

    pins = new StridablesList<>();

    aiPins = new StridablesList<>();
    diPins = new StridablesList<>();
    aoPins = new StridablesList<>();
    doPins = new StridablesList<>();

    // регистрация hal в глобальном контексте
    aGlobalContext.setHal( this );

    halOps = new OptionSet( EHalOps.asOptionSet() );
    halOps.addAll( readConfigFile( new File( IL2HardConstants.L2_HAL_CFG_FILE_NAME ) ) );

  }

  //
  // ---------------------------------------------------------------
  // методы основного интерфейса IHal

  @Override
  public IStridablesList<? extends IPin> listPins() {
    return pins;
  }

  @Override
  public IStridablesList<? extends IAIPin> listAIPins() {
    return aiPins;
  }

  @Override
  public IStridablesList<? extends IAOPin> listAOPins() {
    return aoPins;
  }

  @Override
  public IStridablesList<? extends IDIPin> listDIPins() {
    return diPins;
  }

  @Override
  public IStridablesList<? extends IDOPin> listDOPins() {
    return doPins;
  }

  @Override
  public IStridablesList<? extends ISpecificDevice> listSpecificDevices() {
    return specificDevices;
  }

  //
  // ----------------------------------------------------------------------------
  // Методы компоненты контейнера

  @Override
  protected void processStart() {
    // Старт компоненты - инициализация устройств - выдача реализаций пинов
    // и спец. устройств

    // Обобщённое время, которое отводится на чтение с физ устройств в отдельных потоках,
    // вычсляется следующим образом:
    // первоначальное значение берётся из настроек HALa
    // далее проверяются значения каждого аппарата (из настроек) и берётся максимальное
    // полученное значение умножается на коэффициент (например 1.5)
    int commoMaxReadTime = halOps.getInt( EHalOps.MAX_TYPICAL_READ_DEVICE_TIME );

    // тоже самое для записи
    int commoMaxWriteTime = halOps.getInt( EHalOps.MAX_TYPICAL_WRITE_DEVICE_TIME );

    // коэффициент увеличения времени
    float multyCoef = 1.5f;

    // получение конфигураций устройств по данным из конфигурационной
    // директории
    File dir = new File( globalContext.globalOps().getStr( EGlobalOps.THD_CONFIG_DIR ) );

    // инициализация загрузчика конфигураций
    DefaultUnitConfigLoader loader = new DefaultUnitConfigLoader( DEVCFG, DEVICE_CONFIG );

    // загрузка конфигураций
    try {
      loader.loadConfig( dir );
    }
    catch( TsIoRtException e ) {
      logger.error( e, ERR_HAL_ISNT_STARTED_DUE_TO_CFG_FAIL );
      return;
    }

    // получение конфигураций
    IStridablesList<IUnitConfig> cfgs = loader.listThDriverConfigs();

    logger.info( "Loaded config size - %d", Integer.valueOf( cfgs.size() ) );

    // по каждой конфигурации инициализируется создатель пинов и спец.
    // устройств
    for( IUnitConfig unitConfig : cfgs ) {
      logger.info( "Loaded config - %s", unitConfig.id() );
      IDevicesProducer devProducer = null;

      // признак того, что драйвер нужно отключать в состоянии резервирования
      boolean mustReserve = false;
      if( unitConfig.params().fields().hasValue( MUST_RESERVE ) ) {
        mustReserve = unitConfig.params().fields().getBool( MUST_RESERVE );
      }

      try {

        // создание продюсера по конфиг информации
        devProducer = createDevicesProducer( unitConfig );
        logger.info( "Producer %screated", (devProducer != null) ? TsLibUtils.EMPTY_STRING : "not " );

        // определение характерных временных интервалов работы аппарата: время записи/чтения с физ уровня
        if( unitConfig.params().fields().hasValue( MAX_READ_TIME ) ) {
          int maxReadTime = (int)(multyCoef * unitConfig.params().fields().getInt( MAX_READ_TIME ));
          commoMaxReadTime = Math.max( commoMaxReadTime, maxReadTime );
        }

        if( unitConfig.params().fields().hasValue( MAX_WRITE_TIME ) ) {
          int maxWriteTime = (int)(multyCoef * unitConfig.params().fields().getInt( MAX_WRITE_TIME ));
          commoMaxWriteTime = Math.max( commoMaxWriteTime, maxWriteTime );
        }

      }
      catch( TsIllegalArgumentRtException e ) {
        logger.error( e, ERR_PRODUCER_CREATION_FAILED );
      }

      if( devProducer == null ) {
        continue;
      }

      // Создание пинов устройства
      // TODO - ловить ошибку создания пинов - в момент создания пинов - должен быть первый обмен с физ устройствами
      // (или раньше)?
      try {
        AbstractPinsDevice pinsDevice = devProducer.createPinsDevice();

        if( pinsDevice != null ) {
          operationDevices.add( pinsDevice );
          if( mustReserve ) {
            reservantDevicesIds.add( pinsDevice.id() );
          }
          IStridablesList<AbstractPin> devicePins = pinsDevice.getPins();
          addPinsToAppropriateTypeGroups( devicePins );
        }
      }
      catch( Exception e ) {
        logger.error( e, ERR_APPARATUS_PINS_CREATION_FAILED );
      }

      // Создание спец. устройств устройства
      // ловить ошибку создания (первый обмен с физ устройством)
      IStridablesList<AbstractSpecificDevice> deviceDevices = null;

      try {
        deviceDevices = devProducer.createSpecificDevices();
        if( deviceDevices != null ) {
          for( AbstractSpecificDevice specDev : deviceDevices ) {
            if( !operationDevices.hasKey( specDev.id() ) ) {
              operationDevices.add( specDev );
              logger.info( "SpecificDevice %s is created and added", specDev.id() );
              if( mustReserve ) {
                reservantDevicesIds.add( specDev.id() );
              }
            }

            // добавление спец. устройства в список
            specificDevices.add( specDev );
          }

        }
      }
      catch( Exception e ) {
        logger.error( e, ERR_SPEC_DEV_APPARATS_CREATION_FAILED );
      }

    }

    // Корректировка значений времён чтения-записи с физ уровня как параметров HALa

    halOps.setInt( EHalOps.MAX_TYPICAL_READ_DEVICE_TIME, commoMaxReadTime );
    halOps.setInt( EHalOps.MAX_TYPICAL_WRITE_DEVICE_TIME, commoMaxWriteTime );

    // запуск потоков аппратов - по одному потоку на аппарат
    startApparatsThreads();
  }

  /**
   * Разбивает список пинов на группы в соответствии с их типом {@link EPinKind} и добавляет группы в соответствующие
   * глобальные коллекции
   *
   * @param devicePins IStridablesList - список пинов.
   */
  private void addPinsToAppropriateTypeGroups( IStridablesList<AbstractPin> devicePins ) {
    if( devicePins == null ) {
      return;
    }
    // Добавление пинов в списки - общий и соответствующий типу пина
    // - частный
    for( AbstractPin pin : devicePins ) {
      pins.add( pin );
      switch( pin.kind() ) {
        case AI:
          aiPins.add( pin );
          break;
        case AO:
          aoPins.add( pin );
          break;
        case DI:
          diPins.add( pin );
          break;
        case DO:
          doPins.add( pin );
          break;
        default:
          break;
      }

    }
  }

  /**
   * Запускает у аппаратов вспомогательные потоки записи-чтения на физ уровень.
   */
  private void startApparatsThreads() {
    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      if( !reservantDevicesIds.hasElem( pinsDev.id() ) ) {
        pinsDev.startThread();
      }
    }

    // for( AbstractHalIoDevice specDev : specificDevices ) {
    // if( operationDevices.hasKey( specDev.id() ) ) {
    // continue;
    // }
    // specDev.startThread();
    // }

  }

  @Override
  protected void processRunStep() {
    // данный метод не используется - вместо него три метода
    // doReadDevices()
    // doReadValues()
    // doWriteValues()
  }

  @Override
  public void onApparatError( IList<ApparatError> aErrors ) {
    // ApparatError ae = aErrors.get( 0 );
    // globalContext.logger().error( ae.getException(), "Ошибка НУ '%s' в аппарате '%s'", ae.getErrorId(),
    // ae.getApparatId() );
    // globalContext.appApi(); // передавать ошибки сюда TODO
  }

  @Override
  public void doReadDevices() {
    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      if( !pinsDev.isClosed() ) {
        pinsDev.readDevices();
      }
    }

    // for( AbstractHalIoDevice specDev : specificDevices ) {
    // if( operationDevices.hasKey( specDev.id() ) ) {
    // continue;
    // }
    // specDev.readDevices();
    // }

  }

  @Override
  public void doReadValues() {
    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      if( !pinsDev.isClosed() ) {
        pinsDev.readValues();
      }
    }

    // for( AbstractHalIoDevice specDev : specificDevices ) {
    // if( operationDevices.hasKey( specDev.id() ) ) {
    // continue;
    // }
    // specDev.readValues();
    // }
  }

  @Override
  public void doWriteValues() {
    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      if( !pinsDev.isClosed() ) {
        pinsDev.writeValues();
      }
    }

    // for( AbstractHalIoDevice specDev : specificDevices ) {
    // if( operationDevices.hasKey( specDev.id() ) ) {
    // continue;
    // }
    // specDev.writeValues();
    // }
  }

  @Override
  protected boolean processStopQuery() {
    // Запуск отключения устройств

    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      if( !pinsDev.isClosed() ) {
        pinsDev.close();
      }
    }

    // for( AbstractHalIoDevice specDev : specificDevices ) {
    // if( operationDevices.hasKey( specDev.id() ) ) {
    // continue;
    // }
    // specDev.close();
    // }

    // проверка выполнения отключения
    return processStopStep();
  }

  @Override
  protected boolean processStopStep() {
    // опрос об отключении
    // пинов
    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      boolean isClosed = pinsDev.isClosed();
      if( !isClosed ) {
        return false;
      }
    }

    // спец. устройств
    // for( AbstractHalIoDevice specDev : specificDevices ) {
    // if( operationDevices.hasKey( specDev.id() ) ) {
    // continue;
    // }
    // boolean isClosed = specDev.isClosed();
    // if( !isClosed ) {
    // return false;
    // }
    // }

    return true;
  }

  //
  // --------------------------------------------------------------------------
  // Методы контроля исправности

  @Override
  public int getHealth() {

    if( operationDevices.size() == 0 ) {
      return 100;
    }

    int numerator = 0;
    int denominator = 0;
    for( AbstractHalIoDevice pinsDev : operationDevices ) {

      int devHealth = pinsDev.getHealth();
      int devWeight = pinsDev.getWeight();

      numerator += devHealth * devWeight;
      denominator += devWeight;

    }

    int result = (int)(((double)numerator) / ((double)denominator));

    if( result == 0 ) {
      result = 1;
    }
    return result;
  }

  @Override
  public int getWeight() {
    return 10;
  }

  //
  // --------------------------------------------------------------------------
  // Вспомогательные внутренние методы

  /**
   * Создаёт и возвращает экземпляр создателя пинов и спец. оборудования по конфигурационной информации
   *
   * @param aCfg {@link IUnitConfig} - конфигурационная информация
   * @return {@link IDevicesProducer} - создатель пинов и спец. оборудования.
   * @throws TsNullArgumentRtException
   * @throws TsIllegalArgumentRtException
   */
  private IDevicesProducer createDevicesProducer( IUnitConfig aCfg ) {
    TsNullArgumentRtException.checkNull( aCfg );
    IAvTree params = aCfg.params();
    IOptionSet oSet = params.fields();

    try {
      String javaClassName = oSet.getStr( JAVA_CLASS_NAME );

      Class<?> producerClass = Class.forName( javaClassName );

      IDevicesProducer producer = (IDevicesProducer)producerClass.newInstance();
      producer.setHalErrorProcessor( globalContext.appApi() );
      producer.configYourself( aCfg );
      return producer;
    }
    catch( TsItemNotFoundRtException | TsIllegalArgumentRtException | ClassNotFoundException | InstantiationException
        | IllegalAccessException e ) {
      logger.error( e );
      throw new TsIllegalArgumentRtException( e, ERR_PRODUCER_CREATION_N_CONFIG_FAILED );
    }
  }

  @Override
  public IOptionSet options() {
    return halOps;
  }

  /**
   * Считывает набор параметров из файла конфигурации.
   * <p>
   * Формат файла конфигурации соответствует {@link IDvWriter#writeTypeConstraints(IOptionSet)} и не допускает никаких
   * комментариев или других данных.
   * <p>
   * Метод не выбрасывает исключений, все исключения ловятся и логируются. В случае ошибки возвращает пустой список
   * параметров.
   *
   * @param aFile {@link File} - файл для чтения конфигурации
   * @return {@link IOptionSetEdit} - счтанный или пустой набор параметров
   */
  private IOptionSet readConfigFile( File aFile ) {
    try {
      // mvk: буферизированное чтение из текстового файла
      // ICharInputStream chIn = new CharInputStreamFile( aFile );
      // ICharInputStream chIn = loadCharInputStreamFromFile( aFile );

      // IStridReader sr = new StridReader( chIn );
      // IDvReader dr = new DvReader( sr );

      IOptionSet result = OptionSetKeeper.KEEPER.read( aFile );
      logger.info( MSG_HAL_CFG_FILE_READ_OK, aFile.getAbsolutePath() );
      return result;
    }
    catch( Exception e ) {
      logger.warning( e, MSG_HAL_ERR_CANT_READ_CFG_FILE, aFile.getAbsolutePath() );
      return IOptionSet.NULL;
    }

  }

  private boolean isWorkAsReserve = true;

  @Override
  public boolean isWorkAsReserve() {
    boolean loacalIsWorkAsReserve = isWorkAsReserve;
    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      if( reservantDevicesIds.hasElem( pinsDev.id() ) ) {
        loacalIsWorkAsReserve = loacalIsWorkAsReserve && pinsDev.isClosed();
      }
    }
    return loacalIsWorkAsReserve;
  }

  @Override
  public void queryReserve() {
    isWorkAsReserve = true;
    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      if( reservantDevicesIds.hasElem( pinsDev.id() ) ) {
        logger.info( "query reserve closing device: %s", pinsDev.id() ); //$NON-NLS-1$
        pinsDev.close();
        logger.info( "query reserve closed device: %s", pinsDev.id() ); //$NON-NLS-1$
      }
    }
  }

  @Override
  public void queryMain() {
    isWorkAsReserve = false;
    for( AbstractHalIoDevice pinsDev : operationDevices ) {
      if( reservantDevicesIds.hasElem( pinsDev.id() ) ) {
        logger.info( "query main start device: %s", pinsDev.id() ); //$NON-NLS-1$
        pinsDev.startThread();
        logger.info( "query main started device: %s", pinsDev.id() ); //$NON-NLS-1$
      }
    }
  }

}

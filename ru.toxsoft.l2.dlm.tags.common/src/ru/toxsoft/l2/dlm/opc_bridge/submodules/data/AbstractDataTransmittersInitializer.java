package ru.toxsoft.l2.dlm.opc_bridge.submodules.data;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.data.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Абстрактная реализация инициализатора передатчиков данных от железа на сервер, призванный по конфиг информации
 * сопоставить каналу НУ данное системы, наладить эту связь (найти канал и найти данное), зарегистрировать всю
 * необходимую информацию (датасеты)
 *
 * @author max
 * @param <T> - класс датасета.
 */
public abstract class AbstractDataTransmittersInitializer<T extends ISkRtdataChannel>
    implements IDataTransmittersInitializer<T> {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Конфигурационная информация данных сервера.
   */
  private IListEdit<IListEdit<DataObjNameExtension>> dataDefs = new ElemArrayList<>();

  /**
   * Список идентификаторов тегов.
   */
  private IListEdit<IListEdit<IOptionSet>> tagsIds = new ElemArrayList<>();

  /**
   * Дата-сет.
   */
  private IMap<Gwid, T> wDataSet;

  /**
   * Список передатчиков.
   */
  private IListEdit<IDataTransmitter<T>> dataTransmitters = new ElemArrayList<>();

  private IListEdit<IDataTransmitter<T>> startedDataTransmitters = new ElemArrayList<>();

  /**
   * Признак окончания инициализации.
   */
  private boolean initialized = false;

  private IMapEdit<Gwid, IDataSetter> dataSetters = new ElemMap<>();

  // Заплатка от повторяющихся COD-ов
  private IListEdit<String> codStrs = new ElemArrayList<>();

  private GwidList currGwids;

  @Override
  public void addDataConfigParamsForTransmitter( IAvTree aTransConfig )
      throws TsIllegalStateRtException {
    TsIllegalStateRtException.checkTrue( initialized, ERR_MSG_ADD_PARAM_METHOD_AFTER_CONFIG_FORMAT,
        getClass().getName() );

    // создание и инициализация всех передатчиков

    // Список данных сервера, участвующих в передатчике - получение из конфигурации единообразно
    IListEdit<DataObjNameExtension> transDataDefs = getTransDataDefs( aTransConfig );

    // проверка на повторяющиеся COD-ы
    for( DataObjNameExtension dataObjName : transDataDefs ) {
      String codStr = dataObjName.getClassId() + " - " + dataObjName.getObjName() + " - " + dataObjName.getDataId(); //$NON-NLS-1$ //$NON-NLS-2$

      if( codStrs.hasElem( codStr ) ) {
        // Заплатка от повторяющихся COD-ов
        // здесь должно быть выброшено исключение а не продолжение работы
        logger.error( "Duplicated COD: %s", codStr ); //$NON-NLS-1$
        return;
      }

      codStrs.add( codStr );
    }

    dataDefs.add( transDataDefs );

    // передатчик
    IDataTransmitter<T> transmitter = createTransmitter( aTransConfig );
    transmitter.config( aTransConfig );
    dataTransmitters.add( transmitter );

    // Список параметров тегов (включая идентификаторы), участвующих в передатчике - получение из конфигурации
    // единообразно
    IListEdit<IOptionSet> transTagsIds = getTransTagsParams( aTransConfig );
    tagsIds.add( transTagsIds );

  }

  /**
   * Возвращает параметры для получения тегов, участвующих в передатчике.
   *
   * @param aTransConfig IAvTree - конфигурационная информация из файла.
   * @return IListEdit - список параметров для получения тегов.
   */
  private IListEdit<IOptionSet> getTransTagsParams( IAvTree aTransConfig ) {
    IAtomicValue defaultDevId = IAtomicValue.NULL;

    IOptionSet transmitterParams = aTransConfig.fields();
    if( transmitterParams.hasValue( TAG_DEVICE_ID ) ) {
      defaultDevId = transmitterParams.getValue( TAG_DEVICE_ID );
    }

    // если есть несколько тегов
    if( aTransConfig.nodes().hasKey( TRANSMITTER_TAGS_ARRAY ) ) {
      IListEdit<IOptionSet> result = new ElemArrayList<>();
      IAvTree tagsTree = aTransConfig.nodes().getByKey( TRANSMITTER_TAGS_ARRAY );
      for( int i = 0; i < tagsTree.arrayLength(); i++ ) {
        IAvTree tagParamsTree = tagsTree.arrayElement( i );

        IOptionSetEdit tagParams = new OptionSet();

        try {
          copyAsNeed( tagParamsTree.fields(), tagParams, TAG_DEVICE_ID, defaultDevId );
        }
        catch( TsIllegalArgumentRtException e ) {
          throw new TsIllegalArgumentRtException( e, ERR_MSG_DURING_CONFIG_TAG_TRANSMITTER_FORMAT,
              tagParamsTree.structId(), aTransConfig.structId() );
        }

        tagParams.setStr( TAG_ID, tagParamsTree.fields().getStr( TAG_ID ) );

        // tagParams.setStr( TAG_TRANS_ID, tagParamsTree.structId() );

        result.add( tagParams );
      }
      return result;
    }

    // в случае одного тега
    IOptionSetEdit result = new OptionSet();
    result.setValue( TAG_DEVICE_ID, defaultDevId );
    result.setStr( TAG_ID, transmitterParams.getStr( TAG_ID ) );
    // result.setStr( TAG_TRANS_ID, SINGLE );
    return new ElemArrayList<>( result );

  }

  /**
   * Возвращает конфигурационную информацию для данных, участвующих в передатчике
   *
   * @param aTransConfig IAvTree - конфигурационная информация из файла.
   * @return IListEdit - список конфигураций для данных сервера.
   */
  private IListEdit<DataObjNameExtension> getTransDataDefs( IAvTree aTransConfig ) {
    // IListEdit<DataObjNameExtension> result = new ElemArrayList<>();

    IAtomicValue defaultClassId = IAtomicValue.NULL;
    IAtomicValue defaultObjName = IAtomicValue.NULL;
    IAtomicValue defaultCurr = IAtomicValue.NULL;
    IAtomicValue defaultHist = IAtomicValue.NULL;

    IAtomicValue defaultSynchPeriod = IAtomicValue.NULL;

    IOptionSet transmitterParams = aTransConfig.fields();
    if( transmitterParams.hasValue( CLASS_ID ) ) {
      defaultClassId = transmitterParams.getValue( CLASS_ID );
    }
    if( transmitterParams.hasValue( OBJ_NAME ) ) {
      defaultObjName = transmitterParams.getValue( OBJ_NAME );
    }
    if( transmitterParams.hasValue( IS_CURR ) ) {
      defaultCurr = transmitterParams.getValue( IS_CURR );
    }
    else {
      defaultCurr = avBool( false );
    }
    if( transmitterParams.hasValue( IS_HIST ) ) {
      defaultHist = transmitterParams.getValue( IS_HIST );
    }
    else {
      defaultHist = avBool( false );
    }

    if( transmitterParams.hasValue( SYNCH_PERIOD ) ) {
      defaultSynchPeriod = transmitterParams.getValue( SYNCH_PERIOD );
    }
    else {
      defaultSynchPeriod = avInt( 0 );
    }

    // если есть несколько данных
    if( aTransConfig.nodes().hasKey( TRANSMITTER_DATA_ARRAY ) ) {
      IListEdit<DataObjNameExtension> result = new ElemArrayList<>();
      IAvTree dataTree = aTransConfig.nodes().getByKey( TRANSMITTER_DATA_ARRAY );
      for( int i = 0; i < dataTree.arrayLength(); i++ ) {
        IAvTree dParamsTree = dataTree.arrayElement( i );

        IOptionSetEdit dParams = new OptionSet();

        try {
          copyAsNeed( dParamsTree.fields(), dParams, CLASS_ID, defaultClassId );
          copyAsNeed( dParamsTree.fields(), dParams, OBJ_NAME, defaultObjName );
          copyAsNeed( dParamsTree.fields(), dParams, IS_CURR, defaultCurr );
          copyAsNeed( dParamsTree.fields(), dParams, IS_HIST, defaultHist );
          copyAsNeed( dParamsTree.fields(), dParams, SYNCH_PERIOD, defaultSynchPeriod );
        }
        catch( TsIllegalArgumentRtException e ) {
          throw new TsIllegalArgumentRtException( e, ERR_MSG_DURING_CONFIG_DATA_TRANSMITTER_FORMAT,
              dParamsTree.structId(), aTransConfig.structId() );
        }

        dParams.setStr( DATA_ID, dParamsTree.fields().getStr( DATA_ID ) );

        result.add( new DataObjNameExtension( dParams ) );
      }
      return result;
    }

    // в случае одного данного
    IListEdit<DataObjNameExtension> oneResult = new ElemArrayList<>();

    IOptionSetEdit dParams = new OptionSet();
    dParams.setValue( CLASS_ID, defaultClassId );
    dParams.setValue( OBJ_NAME, defaultObjName );
    dParams.setValue( DATA_ID, transmitterParams.getValue( DATA_ID ) );
    dParams.setValue( IS_CURR, defaultCurr );
    dParams.setValue( IS_HIST, defaultHist );
    dParams.setValue( SYNCH_PERIOD, defaultSynchPeriod );

    oneResult.add( new DataObjNameExtension( dParams ) );

    // Лютый костыль для инверсионного сигнала
    if( transmitterParams.hasValue( INV_DATA_ID ) ) {
      dParams = new OptionSet();
      dParams.setValue( CLASS_ID, defaultClassId );
      dParams.setValue( OBJ_NAME, defaultObjName );
      dParams.setValue( DATA_ID, transmitterParams.getValue( INV_DATA_ID ) );
      dParams.setValue( IS_CURR, defaultCurr );
      dParams.setValue( IS_HIST, defaultHist );
      dParams.setValue( SYNCH_PERIOD, defaultSynchPeriod );

      oneResult.add( new DataObjNameExtension( dParams ) );
    }

    return oneResult;
  }

  @SuppressWarnings( "static-method" )
  private void copyAsNeed( IOptionSet aSource, IOptionSetEdit aTarget, String aId, IAtomicValue aDefault ) {
    if( aSource.hasValue( aId ) ) {
      aTarget.setValue( aId, aSource.getValue( aId ) );
    }
    else {
      if( aDefault == null || aDefault == IAtomicValue.NULL ) {
        throw new TsIllegalArgumentRtException( ERR_MSG_DEFAULT_VALUE_OF_PARAM_IS_ABSENT_FORMAT, aId );
      }
      aTarget.setValue( aId, aDefault );
    }
  }

  /**
   * Создаёт передатчик по конфигурационной информации
   *
   * @param aTransConfig
   * @return
   */
  @SuppressWarnings( "unchecked" )
  private IDataTransmitter<T> createTransmitter( IAvTree aTransConfig ) {
    // тип передатчика - из конфигурации
    String transFactoryClassType = aTransConfig.fields().getStr( JAVA_CLASS );

    try {
      Class<? extends AbstractTransmitterFactory<T>> factoryClass =
          (Class<AbstractTransmitterFactory<T>>)Class.forName( transFactoryClassType );

      AbstractTransmitterFactory<T> factory = factoryClass.newInstance();

      IDataTransmitter<T> transmitter = factory.createTransmitter( aTransConfig );
      return transmitter;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_TRANSMITTER_FORMAT,
          aTransConfig.structId() );
    }

  }

  @Override
  public void initialize( IDlmContext aContext )
      throws TsIllegalStateRtException {
    TsIllegalStateRtException.checkTrue( initialized, ERR_MSG_CONFIG_IS_CALLED_TWICE_FORMAT, getClass().getName() );

    currGwids = new GwidList();

    int[][] dataIndexes = new int[dataDefs.size()][];
    int codIndex = 0;
    // перебираем все данные и при встрече исторического получаем Cod и ставим в соответствие индекс в общем списке
    for( int j = 0; j < dataDefs.size(); j++ ) {
      IListEdit<DataObjNameExtension> transDataDefs = dataDefs.get( j );
      dataIndexes[j] = new int[transDataDefs.size()];
      for( int i = 0; i < transDataDefs.size(); i++ ) {
        // на случай ошибки инициализации - устанавливаем значение по умолчанию
        dataIndexes[j][i] = -1;
        DataObjNameExtension dataObjName = transDataDefs.get( i );
        // Dima, 26.02.16
        // Добавляем проверку на существование объектов и параметров с распечаткой всех ошибок в лог
        try {
          ISkObject bsObject = aContext.network().getSkConnection().coreApi().objService()
              .find( new Skid( dataObjName.getClassId(), dataObjName.getObjName() ) );
          if( bsObject == null ) {
            logger.error( ERR_MSG_INVALID_OBJ, dataObjName.getObjName(), dataObjName.getClassId() );
            // Продолжаем цикл, чтобы получить все ошибки за один проход
            continue;
          }
          ISkClassInfo clsInfo =
              aContext.network().getSkConnection().coreApi().sysdescr().findClassInfo( dataObjName.getClassId() );

          if( clsInfo == null ) {
            logger.error( "Class %s not found", dataObjName.getClassId() );
            // Продолжаем цикл, чтобы получить все ошибки за один проход
            continue;
          }
          if( !clsInfo.rtdata().list().hasKey( dataObjName.getDataId() ) ) {
            logger.error( ERR_MSG_INVALID_DATA_ID, dataObjName.getClassId(), dataObjName.getDataId() );
            // Продолжаем цикл, чтобы получить все ошибки за один проход
            continue;
          }
        }
        catch( TsIllegalArgumentRtException ex ) {
          logger.error( ex );
          return;
        }
        if( needsToAddCodToDataSet( dataObjName ) ) {
          Gwid gwid = dataObjName.convertToGwid();
          currGwids.add( gwid );
          dataIndexes[j][i] = codIndex;
          codIndex++;
        }
        // else {
        // dataIndexes[j][i] = -1;
        // }
      }
    }

    // создаём датасет текущих данных на запись
    wDataSet = createWriteDataSet( aContext.network().getSkConnection(), currGwids );

    // перечислить гвиды, по которым не были сформированы каналы
    for( Gwid currGwid : currGwids ) {
      if( !wDataSet.hasKey( currGwid ) ) {
        logger.error( "For gwid '%s' curdata channel was not created",currGwid.asString() );
      }
    }

    // context.network().getConnection().serverApi().currDataService().createWriteCurrDataSet( currCods.keys() );

    // получаем список зарегистрированных кодов
    // IList<Cod> regCurrCods = wDataSet.cods();

    // формируем массив индексов текущих данных
    // int[] currDataIndexes = new int[currGwids.size()];
    // for( int i = 0; i < regCurrCods.size(); i++ ) {
    // Cod cod = regCurrCods.get( i );
    // Integer index = currCods.getByKey( cod );
    // currDataIndexes[i] = index.intValue();
    // }

    for( int j = 0; j < dataTransmitters.size(); j++ ) {
      // передатчик
      IDataTransmitter<T> transmitter = dataTransmitters.get( j );

      int[] transDataIndexes = dataIndexes[j];
      IListEdit<DataObjNameExtension> transDataDefs = dataDefs.get( j );
      IDataSetter[] realDataIndexes = new IDataSetter[transDataIndexes.length];
      for( int i = 0; i < transDataIndexes.length; i++ ) {
        DataObjNameExtension dataObjName = transDataDefs.get( i );
        int index = transDataIndexes[i];

        IDataSetter setter = IDataSetter.NULL;
        if( index >= 0 ) {
          Gwid setterGwid = currGwids.get( index );
          setter = transDataIndexes[i] < 0 ? IDataSetter.NULL : createSetter( setterGwid, dataObjName, wDataSet );
          dataSetters.put( setterGwid, setter );
        }
        realDataIndexes[i] = setter;

      }

      IListEdit<IOptionSet> transTagsParams = tagsIds.get( j );

      IListEdit<ITag> tags = new ElemArrayList<>();

      for( int i = 0; i < transTagsParams.size(); i++ ) {
        IOptionSet tagOptSet = transTagsParams.get( i );
        ITag tag = getTag( aContext, tagOptSet );

        if( tag == null ) {
          String tagsSpecDev = tagOptSet.getStr( TAG_DEVICE_ID );
          String tagId = tagOptSet.getStr( TAG_ID );
          logger.error( "Tag '%s' not found", tagsSpecDev + " | " + tagId );
        }
        else {
          tags.add( tag );
        }
      }

      if( transTagsParams.size() != tags.size() ) {
        continue;
      }

      // запуск передатчика
      try {
        long t1 = System.currentTimeMillis();
        transmitter.start( realDataIndexes, tags, wDataSet );
        long t2 = System.currentTimeMillis();
        System.out.printf( "j = %d, transmitter.start() : %d \n", j, (t2 - t1) );
        startedDataTransmitters.add( transmitter );
      }
      catch( Exception startEx ) {
        logger.error( "Transmitter not started '%s", startEx.getMessage() );
      }
    }

    initialized = true;
  }

  protected abstract IDataSetter createSetter( Gwid aDataGwid, DataObjNameExtension aDataObjName,
      IMap<Gwid, T> aDataSet );

  protected abstract boolean needsToAddCodToDataSet( DataObjNameExtension aDataObjName );

  /**
   * Возвращает тег.
   *
   * @param aContext IDlmContext - контекст модулей, необходимый для получения тега.
   * @param aTransTagsParams IOptionSet - параметры получения тега.
   * @return ITag - тег.
   */
  @SuppressWarnings( "static-method" )
  private ITag getTag( IDlmContext aContext, IOptionSet aTransTagsParams ) {
    // получение спец устройства
    String tagsSpecDev = aTransTagsParams.getStr( TAG_DEVICE_ID );
    ITsOpc tagsDevice = (ITsOpc)aContext.hal().listSpecificDevices().getByKey( tagsSpecDev );

    String tagId = aTransTagsParams.getStr( TAG_ID );
    ITag tag = tagsDevice.tag( tagId );
    return tag;
  }

  @Override
  public IMap<Gwid, IDataSetter> getDataSetters() {
    return dataSetters;
  }

  @Override
  public IList<IDataTransmitter<T>> getDataTransmitters()
      throws TsIllegalStateRtException {
    return startedDataTransmitters;
  }

  /**
   * Метод для переопределения потомакми, Должен создавать дата-сет на запись
   *
   * @param aConnection IS5Connection - соединение с сервером
   * @param aGwids IList - набор кодов COD
   * @return T - датасет на запись.
   */
  protected abstract IMap<Gwid, T> createWriteDataSet( ISkConnection aConnection, IGwidList aGwids );

}

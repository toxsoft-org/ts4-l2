package org.toxsoft.l2.dlm.tags.submodules.data;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;
import static org.toxsoft.l2.dlm.tags.submodules.data.IL2Resources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.dlm.tags.submodules.setters.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Реализация инициализатора передатчиков данных от железа на сервер, призванный по конфиг информации сопоставить каналу
 * НУ данное системы, наладить эту связь (найти канал и найти данное), зарегистрировать всю необходимую информацию
 * (датасеты)
 *
 * @author max
 */
public class DataTranslatorsInitializer
    implements IDataTranslatorsInitializer {

  /**
   * Журнал работы
   */
  private ILogger logger;

  /**
   * Конфигурационная информация данных сервера.
   */
  private IListEdit<IListEdit<GwidTranslatorCfgExtension>> dataDefs = new ElemArrayList<>();

  /**
   * Список идентификаторов тегов.
   */
  private IListEdit<IListEdit<IOptionSet>> tagsIds = new ElemArrayList<>();

  /**
   * Дата-сет.
   */
  private IMap<Gwid, ISkWriteCurrDataChannel> currDataSet;
  private IMap<Gwid, ISkWriteHistDataChannel> histDataSet;

  /**
   * tag ids 2 Gwids map
   */
  // private IMapEdit<String, IGwidList> tag2Gwids = new ElemMap<>();

  /**
   * Список передатчиков.
   */
  private IListEdit<IDataGwidTranslator> dataGwidTranslators = new ElemArrayList<>();

  // private IListEdit<IDataTransmitter<T>> startedDataTransmitters = new ElemArrayList<>();

  /**
   * Признак окончания инициализации.
   */
  private boolean initialized = false;

  private IMapEdit<Gwid, IGwidValueSetter> dataSetters = new ElemMap<>();

  // Заплатка от повторяющихся COD-ов
  private IListEdit<String> codStrs = new ElemArrayList<>();

  private GwidList currGwids;
  private GwidList histGwids;

  private IListEdit<String> tagsDevices = new ElemArrayList<>( false );

  @Override
  public void addDataConfigParamsForTransmitter( IAvTree aTransConfig )
      throws TsIllegalStateRtException {
    TsIllegalStateRtException.checkTrue( initialized, ERR_MSG_ADD_PARAM_METHOD_AFTER_CONFIG_FORMAT,
        getClass().getName() );

    // создание и инициализация всех передатчиков

    // Список данных сервера, участвующих в передатчике - получение из конфигурации единообразно
    // TODO - возможно нужно только gwid
    IListEdit<GwidTranslatorCfgExtension> transDataDefs = getTransDataDefs( aTransConfig );

    // проверка на повторяющиеся COD-ы //TODO - нужно ли?
    for( GwidTranslatorCfgExtension dataObjName : transDataDefs ) {
      String codStr = dataObjName.getGwid().canonicalString();
      if( codStrs.hasElem( codStr ) ) {
        // Заплатка от повторяющихся COD-ов
        // здесь должно быть выброшено исключение а не продолжение работы
        logger.error( "Duplicated COD: %s", codStr ); //$NON-NLS-1$
        return;
      }

      codStrs.add( codStr );
    }

    dataDefs.add( transDataDefs );

    // передатчик - просто получение объектов класса
    IDataGwidTranslator transmitter = createTransmitter( aTransConfig );
    transmitter.config( aTransConfig );
    dataGwidTranslators.add( transmitter );

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
  private IListEdit<GwidTranslatorCfgExtension> getTransDataDefs( IAvTree aTransConfig ) {
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
      IListEdit<GwidTranslatorCfgExtension> result = new ElemArrayList<>();
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

        result.add( new GwidTranslatorCfgExtension( dParams ) );
      }
      return result;
    }

    // в случае одного данного
    IListEdit<GwidTranslatorCfgExtension> oneResult = new ElemArrayList<>();

    IOptionSetEdit dParams = new OptionSet();
    dParams.setValue( CLASS_ID, defaultClassId );
    dParams.setValue( OBJ_NAME, defaultObjName );
    dParams.setValue( DATA_ID, transmitterParams.getValue( DATA_ID ) );
    dParams.setValue( IS_CURR, defaultCurr );
    dParams.setValue( IS_HIST, defaultHist );
    dParams.setValue( SYNCH_PERIOD, defaultSynchPeriod );

    oneResult.add( new GwidTranslatorCfgExtension( dParams ) );

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
  private IDataGwidTranslator createTransmitter( IAvTree aTransConfig ) {
    // тип передатчика - из конфигурации
    String transFactoryClassType = aTransConfig.fields().getStr( JAVA_CLASS );

    try {
      Class<? extends AbstractTranslatorFactory> factoryClass =
          (Class<AbstractTranslatorFactory>)Class.forName( transFactoryClassType );

      AbstractTranslatorFactory factory = factoryClass.newInstance();

      IDataGwidTranslator transmitter = factory.createTransmitter( aTransConfig );
      return transmitter;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_TRANSMITTER_FORMAT,
          aTransConfig.structId() );
    }

  }

  @Override
  public void initialize( IL2SharedContext aContext )
      throws TsIllegalStateRtException {
    TsIllegalStateRtException.checkTrue( initialized, ERR_MSG_CONFIG_IS_CALLED_TWICE_FORMAT, getClass().getName() );

    currGwids = new GwidList();
    histGwids = new GwidList();

    // главное тут - создаём каналы данных на сервере - SK connection должен уже быть

    ISkConnection conn = aContext.net().getSkConnection();

    if( conn.state() != ESkConnState.ACTIVE ) {
      logger.error( "SK connection is not active" );
      return;
    }

    // перебираем все трансляторы - формируем список gwid для создания каналов
    for( int j = 0; j < dataGwidTranslators.size(); j++ ) {
      IListEdit<GwidTranslatorCfgExtension> transDataDefs = dataDefs.get( j );

      for( int i = 0; i < transDataDefs.size(); i++ ) {
        // gwid
        GwidTranslatorCfgExtension dataObjName = transDataDefs.get( i );
        Gwid gwid = dataObjName.getGwid();

        // Добавляем проверку на существование объектов и параметров с распечаткой всех ошибок в лог
        try {
          ISkObject bsObject = aContext.net().getSkConnection().coreApi().objService().find( gwid.skid() );
          if( bsObject == null ) {
            logger.error( ERR_MSG_INVALID_OBJ, gwid.strid(), gwid.classId() );
            // Продолжаем цикл, чтобы получить все ошибки за один проход
            continue;
          }
          ISkClassInfo clsInfo = aContext.net().getSkConnection().coreApi().sysdescr().findClassInfo( gwid.classId() );

          if( clsInfo == null ) {
            logger.error( "Class %s not found", gwid.classId() );
            // Продолжаем цикл, чтобы получить все ошибки за один проход
            continue;
          }
          if( !clsInfo.rtdata().list().hasKey( gwid.propId() ) ) {
            logger.error( ERR_MSG_INVALID_DATA_ID, gwid.classId(), gwid.propId() );
            // Продолжаем цикл, чтобы получить все ошибки за один проход
            continue;
          }
          // описание данного
          IDtoRtdataInfo dataInfo = clsInfo.rtdata().list().getByKey( gwid.propId() );
          // TODO quest - брать ли свойства из системного описания????

        }
        catch( TsIllegalArgumentRtException ex ) {
          logger.error( ex );
          return;
        }

        if( dataObjName.isCurr() ) {
          currGwids.add( gwid );
        }
        if( dataObjName.isHist() ) {
          histGwids.add( gwid );
        }
      }
    }

    // создаём датасет текущих данных на запись
    currDataSet = conn.coreApi().rtdService().createWriteCurrDataChannels( currGwids );
    histDataSet = conn.coreApi().rtdService().createWriteHistDataChannels( histGwids );

    // перечислить гвиды, по которым не были сформированы каналы
    for( Gwid currGwid : currGwids ) {
      if( !currDataSet.hasKey( currGwid ) ) {
        logger.error( "For gwid '%s' curdata channel was not created", currGwid.canonicalString() );
      }
    }
    for( Gwid currGwid : histGwids ) {
      if( !histDataSet.hasKey( currGwid ) ) {
        logger.error( "For gwid '%s' histdata channel was not created", currGwid.canonicalString() );
      }
    }

    // стартуем трансляторы - заполняем тегами и gwid
    for( int j = 0; j < dataGwidTranslators.size(); j++ ) {
      // передатчик
      IDataGwidTranslator transmitter = dataGwidTranslators.get( j );

      GwidList setterGwids = new GwidList();

      IListEdit<GwidTranslatorCfgExtension> transDataDefs = dataDefs.get( j );
      IGwidValueSetter[] tDataSetters = new IGwidValueSetter[transDataDefs.size()];
      for( int i = 0; i < transDataDefs.size(); i++ ) {
        GwidTranslatorCfgExtension dataObjName = transDataDefs.get( i );

        Gwid setterGwid = dataObjName.getGwid();
        IGwidValueSetter setter = createSetter( dataObjName );
        tDataSetters[i] = setter;

        dataSetters.put( setterGwid, setter );
        setterGwids.add( setterGwid );
      }

      IListEdit<IOptionSet> transTagsParams = tagsIds.get( j );

      IListEdit<IL2Tag> tags = new ElemArrayList<>();

      for( int i = 0; i < transTagsParams.size(); i++ ) {
        IOptionSet tagOptSet = transTagsParams.get( i );
        IL2Tag tag = getTag( aContext, tagOptSet );

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
        // TODO log???
        continue;
      }

      // запуск передатчика
      try {
        long t1 = System.currentTimeMillis();
        transmitter.start( tDataSetters, tags );
        long t2 = System.currentTimeMillis();
        logger.debug( "j = %d, transmitter.start() : %d \n", j, (t2 - t1) );

      }
      catch( Exception startEx ) {
        logger.error( "Transmitter not started '%s", startEx.getMessage() );
      }
    }

    initialized = true;
  }

  protected IGwidValueSetter createSetter( GwidTranslatorCfgExtension aDataObjName ) {
    boolean synch = aDataObjName.isSynch();
    long synchPeriod = aDataObjName.getSynchPeriod();

    CombinedDataSetter result = new CombinedDataSetter();

    if( aDataObjName.isCurr() ) {
      result.addDataSetter( new CurrDataSetter( currDataSet, aDataObjName.getGwid(), synch ? synchPeriod : 0 ) );
    }

    if( aDataObjName.isHist() ) {
      IGwidValueSetter setter = synch ? new SynchHistDataSetter( histDataSet, aDataObjName.getGwid(), synchPeriod )
          : new SimpleHistDataSetter( histDataSet, aDataObjName.getGwid() );
      result.addDataSetter( setter );
    }

    return result;
  }

  // protected abstract boolean needsToAddCodToDataSet( DataObjNameExtension aDataObjName );

  /**
   * Возвращает тег.
   *
   * @param aContext IDlmContext - контекст модулей, необходимый для получения тега.
   * @param aTransTagsParams IOptionSet - параметры получения тега.
   * @return ITag - тег.
   */
  private IL2Tag getTag( IL2SharedContext aContext, IOptionSet aTransTagsParams ) {
    // получение спец устройства
    // TODO qwest - как получить теги по устройству
    String tagsSpecDev = aTransTagsParams.getStr( TAG_DEVICE_ID );
    String tagId = aTransTagsParams.getStr( TAG_ID );
    IL2Tag tag = aContext.hal().tags().findByKey( tagId );

    tagsDevices.add( tagsSpecDev );
    return tag;
  }

  @Override
  public IMap<Gwid, IGwidValueSetter> getDataSetters() {
    return dataSetters;
  }

  @Override
  public IList<IDataGwidTranslator> getDataGwidTranslators()
      throws TsIllegalStateRtException {
    return dataGwidTranslators;
  }

  @Override
  public IList<String> getTagsDevices() {
    return tagsDevices;
  }

}

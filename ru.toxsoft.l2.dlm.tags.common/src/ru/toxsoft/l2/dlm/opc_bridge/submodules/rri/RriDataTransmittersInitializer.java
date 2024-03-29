package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.IL2Resources.*;

import java.math.*;
import java.text.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.skf.rri.lib.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация инициализатора передатчиков НСИ данных от железа на сервер, призванный по конфиг информации сопоставить
 * каналу НУ данное системы, наладить эту связь (найти канал и найти данное), зарегистрировать всю необходимую
 * информацию.
 *
 * @author dima
 */
public class RriDataTransmittersInitializer
    implements IRriDataTransmittersInitializer {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Конфигурационная информация данных сервера.
   */
  private IListEdit<IListEdit<RriDataObjNameExtension>> rriDefs = new ElemArrayList<>();

  /**
   * Список идентификаторов тегов.
   */
  private IListEdit<IListEdit<IOptionSet>> tagsIds = new ElemArrayList<>();

  /**
   * Список передатчиков.
   */
  private IListEdit<IRriDataTransmitter> dataTransmitters = new ElemArrayList<>();

  private IListEdit<IRriDataTransmitter> startedDataTransmitters = new ElemArrayList<>();

  /**
   * Признак окончания инициализации.
   */
  private boolean initialized = false;

  private IMapEdit<Gwid, IRriSetter> dataSetters = new ElemMap<>();

  // Заплатка от повторяющихся COD-ов
  private IListEdit<String> codStrs = new ElemArrayList<>();

  private GwidList currGwids;

  private IListEdit<String> tagsDevices = new ElemArrayList<>( false );

  private IComplexTagsContainer complexTagsContainer;
  /**
   * Для округления плавающих
   */
  private static DecimalFormat  decimalFormat = new DecimalFormat( "#.#####" ); //$NON-NLS-1$

  static {
    decimalFormat.setRoundingMode( RoundingMode.CEILING );
  }

  @Override
  public void addDataConfigParamsForTransmitter( IAvTree aTransConfig, IComplexTagsContainer aComplexTagsContainer )
      throws TsIllegalStateRtException {
    TsIllegalStateRtException.checkTrue( initialized, ERR_MSG_ADD_PARAM_METHOD_AFTER_CONFIG_FORMAT,
        getClass().getName() );

    complexTagsContainer = aComplexTagsContainer;
    // создание и инициализация всех передатчиков

    // Список данных сервера, участвующих в передатчике - получение из конфигурации единообразно
    IListEdit<RriDataObjNameExtension> transDataDefs = getTransDataDefs( aTransConfig );

    // проверка на повторяющиеся COD-ы
    for( RriDataObjNameExtension dataObjName : transDataDefs ) {
      String codStr = dataObjName.getClassId() + " - " + dataObjName.getObjName() + " - " + dataObjName.getDataId(); //$NON-NLS-1$ //$NON-NLS-2$

      if( codStrs.hasElem( codStr ) ) {
        // Заплатка от повторяющихся COD-ов
        // здесь должно быть выброшено исключение, а не продолжение работы
        logger.error( "Duplicated COD: %s", codStr ); //$NON-NLS-1$
        return;
      }

      codStrs.add( codStr );
    }

    rriDefs.add( transDataDefs );

    // передатчик
    IRriDataTransmitter transmitter = createTransmitter( aTransConfig );
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
  private static IListEdit<IOptionSet> getTransTagsParams( IAvTree aTransConfig ) {
    IAtomicValue defaultDevId = IAtomicValue.NULL;
    String defaultComlexTag = "";

    IOptionSet transmitterParams = aTransConfig.fields();
    if( transmitterParams.hasValue( TAG_DEVICE_ID ) ) {
      defaultDevId = transmitterParams.getValue( TAG_DEVICE_ID );
    }
    if( transmitterParams.hasValue( COMPLEX_TAG_ID ) ) {
      defaultComlexTag = transmitterParams.getStr( COMPLEX_TAG_ID );
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
        result.add( tagParams );
      }
      return result;
    }

    // в случае одного тега
    IOptionSetEdit result = new OptionSet();
    result.setValue( TAG_DEVICE_ID, defaultDevId );
    result.setStr( TAG_ID, transmitterParams.getStr( TAG_ID ) );
    result.setStr( COMPLEX_TAG_ID, defaultComlexTag );
    return new ElemArrayList<>( result );

  }

  /**
   * Возвращает конфигурационную информацию для данных, участвующих в передатчике
   *
   * @param aTransConfig IAvTree - конфигурационная информация из файла.
   * @return IListEdit - список конфигураций для данных сервера.
   */
  private static IListEdit<RriDataObjNameExtension> getTransDataDefs( IAvTree aTransConfig ) {
    // FIXME продумать как работать через default секцию или как-то еще
    IAtomicValue defaultRriSection = AvUtils.avStr( "rri.section.id" );
    IAtomicValue defaultClassId = IAtomicValue.NULL;
    IAtomicValue defaultObjName = IAtomicValue.NULL;

    IOptionSet transmitterParams = aTransConfig.fields();
    if( transmitterParams.hasValue( RRI_SECTION_ID ) ) {
      defaultRriSection = transmitterParams.getValue( RRI_SECTION_ID );
    }
    if( transmitterParams.hasValue( CLASS_ID ) ) {
      defaultClassId = transmitterParams.getValue( CLASS_ID );
    }
    if( transmitterParams.hasValue( OBJ_NAME ) ) {
      defaultObjName = transmitterParams.getValue( OBJ_NAME );
    }

    // если есть несколько данных TODO MAX зачем массив данных?
    if( aTransConfig.nodes().hasKey( TRANSMITTER_DATA_ARRAY ) ) {
      IListEdit<RriDataObjNameExtension> result = new ElemArrayList<>();
      IAvTree dataTree = aTransConfig.nodes().getByKey( TRANSMITTER_DATA_ARRAY );
      for( int i = 0; i < dataTree.arrayLength(); i++ ) {
        IAvTree dParamsTree = dataTree.arrayElement( i );

        IOptionSetEdit dParams = new OptionSet();

        try {
          copyAsNeed( dParamsTree.fields(), dParams, RRI_SECTION_ID, defaultRriSection );
          copyAsNeed( dParamsTree.fields(), dParams, CLASS_ID, defaultClassId );
          copyAsNeed( dParamsTree.fields(), dParams, OBJ_NAME, defaultObjName );
        }
        catch( TsIllegalArgumentRtException e ) {
          throw new TsIllegalArgumentRtException( e, ERR_MSG_DURING_CONFIG_DATA_TRANSMITTER_FORMAT,
              dParamsTree.structId(), aTransConfig.structId() );
        }

        dParams.setStr( RRI_ATTR_ID, dParamsTree.fields().getStr( RRI_ATTR_ID ) );

        result.add( new RriDataObjNameExtension( dParams ) );
      }
      return result;
    }

    // в случае одного данного
    IListEdit<RriDataObjNameExtension> oneResult = new ElemArrayList<>();

    IOptionSetEdit dParams = new OptionSet();
    dParams.setValue( RRI_SECTION_ID, defaultRriSection );
    dParams.setValue( CLASS_ID, defaultClassId );
    dParams.setValue( OBJ_NAME, defaultObjName );
    dParams.setValue( RRI_ATTR_ID, transmitterParams.getValue( RRI_ATTR_ID ) );

    oneResult.add( new RriDataObjNameExtension( dParams ) );

    return oneResult;
  }

  private static void copyAsNeed( IOptionSet aSource, IOptionSetEdit aTarget, String aId, IAtomicValue aDefault ) {
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
   * @param aTransConfig дерево описания конфигурации
   * @return {@link IRriDataTransmitter} передатчик НСИ параметровdataSetter
   */
  @SuppressWarnings( "unchecked" )
  private static IRriDataTransmitter createTransmitter( IAvTree aTransConfig ) {
    // тип передатчика - из конфигурации
    String transFactoryClassType = aTransConfig.fields().getStr( JAVA_CLASS );

    try {
      Class<? extends AbstractRriTransmitterFactory> factoryClass =
          (Class<AbstractRriTransmitterFactory>)Class.forName( transFactoryClassType );

      AbstractRriTransmitterFactory factory = factoryClass.newInstance();

      IRriDataTransmitter transmitter = factory.createTransmitter( aTransConfig );
      return transmitter;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_TRANSMITTER_FORMAT,
          aTransConfig.structId() );
    }

  }

  // TODO тут нужен глубокий рефакторинг, щас сделано просто чтобы заработало и без понимания как это работает и как
  // должно
  @Override
  public void initialize( IDlmContext aContext )
      throws TsIllegalStateRtException {
    TsIllegalStateRtException.checkTrue( initialized, ERR_MSG_CONFIG_IS_CALLED_TWICE_FORMAT, getClass().getName() );

    currGwids = new GwidList();

    int[][] dataIndexes = new int[rriDefs.size()][];
    int codIndex = 0;
    // перебираем все данные
    for( int j = 0; j < rriDefs.size(); j++ ) {
      IListEdit<RriDataObjNameExtension> transDataDefs = rriDefs.get( j );
      dataIndexes[j] = new int[transDataDefs.size()];
      for( int i = 0; i < transDataDefs.size(); i++ ) {
        // на случай ошибки инициализации - устанавливаем значение по умолчанию
        dataIndexes[j][i] = -1;
        RriDataObjNameExtension dataObjName = transDataDefs.get( i );
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
          // TODO как проверить что НСИ атрибут описан в классе
          // if( !clsInfo.rtdata().list().hasKey( dataObjName.getDataId() ) ) {
          // logger.error( ERR_MSG_INVALID_DATA_ID, dataObjName.getClassId(), dataObjName.getDataId() );
          // // Продолжаем цикл, чтобы получить все ошибки за один проход
          // continue;
          // }
        }
        catch( TsIllegalArgumentRtException ex ) {
          logger.error( ex );
          return;
        }
        Gwid gwid = dataObjName.convertToRriGwid();
        currGwids.add( gwid );
        dataIndexes[j][i] = codIndex;
        codIndex++;
      }
    }

    for( int j = 0; j < dataTransmitters.size(); j++ ) {
      // передатчик
      IRriDataTransmitter transmitter = dataTransmitters.get( j );

      int[] transDataIndexes = dataIndexes[j];
      IListEdit<RriDataObjNameExtension> transDataDefs = rriDefs.get( j );
      IRriSetter[] realDataSetters = new IRriSetter[transDataIndexes.length];
      for( int i = 0; i < transDataIndexes.length; i++ ) {
        RriDataObjNameExtension dataObjName = transDataDefs.get( i );
        int index = transDataIndexes[i];

        IRriSetter setter = IRriSetter.NULL;
        if( index >= 0 ) {
          Gwid setterGwid = currGwids.get( index );
          IMapEdit<Gwid, ISkRriSection> setterMap = new ElemMap<>();
          setterMap.put( setterGwid, getRriSection( aContext, dataObjName ) );
          setter = transDataIndexes[i] < 0 ? IRriSetter.NULL : createSetter( setterGwid, setterMap );
          dataSetters.put( setterGwid, setter );
        }
        realDataSetters[i] = setter;

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
      // тут комплексны тег
      IOptionSet tagOptSet = transTagsParams.get( 0 );
      String complextTagId = tagOptSet.getStr( COMPLEX_TAG_ID );
      IComplexTag complexTag = complexTagsContainer.getComplexTagById( complextTagId );
      // запуск передатчика
      try {
        long t1 = System.currentTimeMillis();
        transmitter.start( realDataSetters, tags, complexTag );
        long t2 = System.currentTimeMillis();
        logger.debug( "j = %d, transmitter.start() : %d \n", j, (t2 - t1) );
        startedDataTransmitters.add( transmitter );
      }
      catch( Exception startEx ) {
        logger.error( "Transmitter not started '%s", startEx.getMessage() );
      }
    }

    initialized = true;
  }

  private static ISkRriSection getRriSection( IDlmContext aContext, RriDataObjNameExtension aDataObjName ) {
    ISkConnection connection = aContext.network().getSkConnection();
    ISkRegRefInfoService rriService = connection.coreApi().getService( ISkRegRefInfoService.SERVICE_ID );
    String sectId = aDataObjName.rriSectionId();
    return rriService.getSection( sectId );
  }

  protected IRriSetter createSetter( Gwid aRriGwid, IMap<Gwid, ISkRriSection> aGwid2SectionMap ) {
    ISkRriSection rriSection = aGwid2SectionMap.getByKey( aRriGwid );
    IAtomicValue serverCurrVal = rriSection.getAttrParamValue( aRriGwid.skid(), aRriGwid.propId() );

    return new RriSetter( aGwid2SectionMap, aRriGwid, serverCurrVal );
  }

  /**
   * Возвращает тег.
   *
   * @param aContext IDlmContext - контекст модулей, необходимый для получения тега.
   * @param aTransTagsParams IOptionSet - параметры получения тега.
   * @return ITag - тег.
   */
  private ITag getTag( IDlmContext aContext, IOptionSet aTransTagsParams ) {
    // получение спец устройства
    String tagsSpecDev = aTransTagsParams.getStr( TAG_DEVICE_ID );
    ITsOpc tagsDevice = (ITsOpc)aContext.hal().listSpecificDevices().getByKey( tagsSpecDev );

    String tagId = aTransTagsParams.getStr( TAG_ID );
    ITag tag = tagsDevice.tag( tagId );

    tagsDevices.add( tagsSpecDev );
    return tag;
  }

  @Override
  public IMap<Gwid, IRriSetter> getDataSetters() {
    return dataSetters;
  }

  @Override
  public IList<IRriDataTransmitter> getDataTransmitters()
      throws TsIllegalStateRtException {
    return startedDataTransmitters;
  }

  @Override
  public IList<String> getTagsDevices() {
    return tagsDevices;
  }

  /**
   * Установщик НСИ данных.
   *
   * @author dima
   */
  class RriSetter
      implements IRriSetter {

    private ISkRriSection channel;

    private IAtomicValue value;

    private Gwid rriGwid;

    private IMap<Gwid, ISkRriSection> gwid2SectMap;

    public RriSetter( IMap<Gwid, ISkRriSection> aDataSet, Gwid aRriGwid, IAtomicValue aCurrServerValue ) {
      super();
      TsIllegalArgumentRtException.checkFalse( aDataSet.hasKey( aRriGwid ) );
      channel = aDataSet.getByKey( aRriGwid );
      rriGwid = aRriGwid;
      gwid2SectMap = aDataSet;
      value = aCurrServerValue;
    }

    @Override
    public boolean setRriValue( IAtomicValue aValue, long aTime ) {
      if( !aValue.isAssigned() ) {
        return false;
      }
      boolean result = !value.equals( aValue );
      if( result ) {
        IAtomicValue uskatVal = channel.getAttrParamValue(rriGwid.skid(), rriGwid.propId());
        if(uskatVal.compareTo(aValue) != 0) {
            // устанавливаем новое значение только если оно не совпадает с тем что уже на сервере
           channel.setAttrParamValue( rriGwid.skid(), rriGwid.propId(), aValue, "automatic update OPC UA -> USkat" );
           value = aValue;

           logger.debug( "rri data: %s - update on USkat server. New value = %s", rriGwid.asString(),
            (aValue.isAssigned() ? aValue.asString() : "Not Assigned") );
        }
      }

      return result;
    }

    @Override
    public String toString() {
      return rriGwid.asString();
    }

    @Override
    public IMap<Gwid, ISkRriSection> gwid2Section() {
      return gwid2SectMap;
    }

  }

}

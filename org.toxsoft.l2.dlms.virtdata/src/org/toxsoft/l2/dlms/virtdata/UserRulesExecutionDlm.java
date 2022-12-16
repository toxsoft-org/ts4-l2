package org.toxsoft.l2.dlms.virtdata;

import static org.toxsoft.l2.dlms.virtdata.IDlmConstants.*;
import static org.toxsoft.l2.dlms.virtdata.IL2Resources.*;

import java.io.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.dlm.impl.*;

/**
 * Модуль выполняющий проверку правил описанных пользователем.
 *
 * @author dima
 */
public class UserRulesExecutionDlm
    extends AbstractDlm {

  /**
   * Журнал работы
   */
  private static final ILogger logger = LoggerUtils.errorLogger();

  /**
   * Список правил
   */
  private IListEdit<RuleInfo> rulesInfoes = new ElemArrayList<>();

  /**
   * Список мониторов правил
   */
  private IListEdit<RuleMonitoring> rulesMonitors = new ElemArrayList<>();

  /**
   * Признак что DLM сконфигурирован.
   */
  private boolean configured = false;

  private ISkConnection connection;

  /**
   * Конструктор.
   *
   * @param aInfo {@link IDlmInfo} - информация о модуле.
   * @param aContext {@link IDlmContext} - контекст нижнего уровня, в котором загружается модуль.
   */
  protected UserRulesExecutionDlm( IDlmInfo aInfo, IDlmContext aContext ) {
    super( aInfo, aContext );
  }

  @Override
  public void configYourself( IUnitConfig aConfig ) {
    IAvTree params = aConfig.params();
    try {
      // Читаем правила
      IAvTree rules = params.nodes().findByKey( RULES );
      for( int i = 0; i < rules.arrayLength(); i++ ) {
        // Описание одного правила
        IAvTree ruleTree = rules.arrayElement( i );
        // Считываем описание правила
        String definition = ruleTree.fields().getStr( RULE_DEF );
        // Считываем текст скрипта
        String script = ruleTree.fields().getStr( RULE_SCRIPT );
        // Считываем таймаут
        int timeout = ruleTree.fields().getInt( RULE_TIMEOUT );
        // Считываем текст сообщения
        String eventText = ruleTree.fields().getStr( RULE_EVENT_TEXT );
        // Считываем входные параметры
        IAvTree inParamsTree = ruleTree.nodes().findByKey( IN_PARAM_SECTION );
        IListEdit<ParamInfo> inputParams = new ElemArrayList<>();
        for( int j = 0; j < inParamsTree.arrayLength(); j++ ) {
          // Данные одного входного параметра правила
          IAvTree inParamTree = inParamsTree.arrayElement( j );
          // название в параметра скрипте
          String scriptName = inParamTree.fields().getStr( IN_PARAM_SCRIPT_NAME );
          // класс параметра
          String classId = inParamTree.fields().getStr( IN_PARAM_CLASS_ID );
          String objName = inParamTree.fields().getStr( IN_PARAM_OBJ_NAME );
          String dataId = inParamTree.fields().getStr( IN_PARAM_DATA_ID );
          // Создаем описание одного параметра и добавляем его в список
          ParamInfo paramInfo = new ParamInfo( scriptName, classId, objName, dataId );
          inputParams.add( paramInfo );
        }
        // Считываем вЫходной параметры
        IAvTree outParamsTree = ruleTree.nodes().findByKey( OUT_PARAM_SECTION );
        // Данные одного входного параметра правила
        IAvTree outParamTree = outParamsTree.arrayElement( 0 );
        // название в параметра скрипте
        String scriptName = outParamTree.fields().getStr( OUT_PARAM_SCRIPT_NAME );
        // класс параметра
        String classId = outParamTree.fields().getStr( OUT_PARAM_CLASS_ID );
        String objName = outParamTree.fields().getStr( OUT_PARAM_OBJ_NAME );
        String dataId = outParamTree.fields().getStr( OUT_PARAM_DATA_ID );
        // Создаем описание одного параметра и добавляем его в список
        ParamInfo paramInfo = new ParamInfo( scriptName, classId, objName, dataId );
        // Создаем правило и добавляем его в список правил
        RuleInfo ruleInfo = new RuleInfo( definition, inputParams, paramInfo, script, timeout, eventText );
        rulesInfoes.add( ruleInfo );
      }
    }
    catch( TsItemNotFoundRtException e ) {
      LoggerUtils.defaultLogger().error( e, ERR_MSG_NECESSARY_PARAM_IS_NOT_SET );
      return;
    }

    configured = true;
  }

  @Override
  protected void doStartComponent() {
    // если модуль не сконфигурирован - выйти
    if( !configured ) {
      return;
    }

    // Получаем связь с сервером системы
    connection = context.network().getSkConnection();
    // На каждое правило создаем блок для его мониторинга
    for( RuleInfo ruleInfo : rulesInfoes ) {
      // создаем монитор для одного правила
      RuleMonitoring ruleMonitoring = new RuleMonitoring( connection, ruleInfo );
      // инициализируем мониторинг значений входных параметров
      initInParamsMonitoring( ruleInfo, ruleMonitoring );
      // инициализируем обновление значений выходных параметров
      ruleMonitoring.setEvSrcSkid( initOutParamsWriteChannels( ruleInfo, ruleMonitoring ) );
      rulesMonitors.add( ruleMonitoring );
    }
  }

  /**
   * Инициализируем мониторинг одного правила
   *
   * @param aRuleInfo описание правила
   * @param aRuleMonitoring монитор
   * @return {@link Skid} id объекта выходного параметра
   */
  private Skid initOutParamsWriteChannels( RuleInfo aRuleInfo, RuleMonitoring aRuleMonitoring ) {

    ParamInfo outParamInfo = aRuleInfo.getOutParam();
    Skid retVal = new Skid( outParamInfo.getClassId(), outParamInfo.getObjName() );
    TsItemNotFoundRtException.checkNull( connection.coreApi().objService().find( retVal ), ERR_MSG_RULE_OBJ_NOT_FOUND,
        aRuleInfo.getDefinition(), retVal.classId(), retVal.strid() );
    GwidList outDataGwidsList = new GwidList();
    outDataGwidsList.add( outParamInfo.gwid() );

    IMap<Gwid, ISkWriteCurrDataChannel> wCurrDataSet =
        connection.coreApi().rtdService().createWriteCurrDataChannels( outDataGwidsList );

    aRuleMonitoring.setOutDataChannels( wCurrDataSet );
    aRuleMonitoring.setEvSrcSkid( retVal );
    return retVal;
  }

  /**
   * Инициализируем мониторинг значений входных параметров
   *
   * @param aRuleInfo описание одного правила
   * @param aRuleMonitoring монитор
   */
  private void initInParamsMonitoring( RuleInfo aRuleInfo, RuleMonitoring aRuleMonitoring ) {
    // IListEdit<Cod> inParamCODs = new ElemArrayList<>();
    // // входные параметры
    // for( ParamInfo paramInfo : aRuleInfo.getInputParams() ) {
    // doName = new DataObjName( paramInfo.getClassId(), paramInfo.getObjName(), paramInfo.getDataId() );
    // TsItemNotFoundRtException.checkNull(
    // connection.serverApi().objectService().find( paramInfo.getClassId(), paramInfo.getObjName() ),
    // ERR_MSG_RULE_OBJ_NOT_FOUND, aRuleInfo.getDefinition(), paramInfo.getClassId(), paramInfo.getObjName() );
    // Cod cod = doName.convertToCod( connection );
    // inParamCODs.add( cod );
    // }
    // // Создаем набор данных для мониторинга значений входных параметров
    // IReadCurrDataSet inParamsCdSet = connection.serverApi().currDataService().createReadCurrDataSet( inParamCODs );
    // // и слушаем его изменения
    // inParamsCdSet.addReadCurrDataSetListener( aRuleMonitoring );

    // входные параметры
    GwidList inDataGwidsList = new GwidList();
    for( ParamInfo inParamInfo : aRuleInfo.getInputParams() ) {
      inDataGwidsList.add( inParamInfo.gwid() );
    }
    connection.coreApi().rtdService().createReadCurrDataChannels( inDataGwidsList );
    connection.coreApi().rtdService().eventer().addListener( aRuleMonitoring );
  }

  @Override
  public void doJob() {
    for( RuleMonitoring ruleMonitoring : rulesMonitors ) {
      try {
        ruleMonitoring.doJob();
      }
      catch( Exception e ) {
        // сбой одного правила не должен затрагивать другие
        LoggerUtils.defaultLogger().error( e, ERR_MSG_JAVACSRIPT_RUN_EXCEPTION, e.getMessage() );
      }
    }
  }

  @Override
  protected boolean doQueryStop() {
    return true;
  }

  @Override
  protected boolean doStopStep() {
    return true;
  }

  /**
   * Пример создания конфигурационного файла описания маршрутов
   *
   * @throws IOException исключение при работе с файловой
   */
  @SuppressWarnings( "nls" )
  public static void testWrite()
      throws IOException {
    // File f = new File( ".\\test1.t" );
    // FileWriter fw = new FileWriter( f );
    // IStrioWriter writer = new StrioWriter( new CharOutputStreamAppendable( fw ) );

    // IDvWriter dr = new DvWriter( writer );

    // массив правил
    AvTree rulesArrayTree = AvTree.createArrayAvTree();

    // первое правило
    IAvTree rule1Tree = createRule1Section();
    rulesArrayTree.addElement( rule1Tree );

    IOptionSetEdit moduleSet = new OptionSet();

    StringMap<IAvTree> ruleNodes = new StringMap<>();
    ruleNodes.put( RULES, rulesArrayTree );

    moduleSet.setStr( "id", "org.toxsoft.l2.dlms.virtdata.UserRulesExecutionDlm" );
    moduleSet.setStr( "description", "Модуль пользовательских правил" );

    IAvTree moduleTree = AvTree.createSingleAvTree( "rules.def", moduleSet, ruleNodes );

    // AvTreeKeeper.INSTANCE.write( dr, moduleTree );

    File f = new File( "test1.t" );
    AvTreeKeeper.KEEPER.write( f, moduleTree );

    // fw.close();

    // PinsConfigFileFormatter.format( "test1.t",
    // "C:\\works\\ws_mm\\org.toxsoft.l2.dlms.virtdata\\dlmcfg\\user_rules.dlmcfg", "DlmConfig = " );
  }

  @SuppressWarnings( "nls" )
  private static IAvTree createRule1Section() {

    IOptionSetEdit ruleOpSet = new OptionSet();
    ruleOpSet.setStr( "rule.def", "" + 1 );
    ruleOpSet.setStr( RULE_SCRIPT, "var isError = !(light0.asBool() || light1.asBool() || light2.asBool())" );

    StringMap<IAvTree> ruleNodes = new StringMap<>();

    // Создаем входные параметры
    AvTree inParamNodes = AvTree.createArrayAvTree();
    IAvTree inParam1Tree = createInParam( "light0", "metro.TrafficLightSign", "bitStl_25RedSign", "on" );
    inParamNodes.addElement( inParam1Tree );
    IAvTree inParam2Tree = createInParam( "light1", "metro.TrafficLightSign", "bitStl_25GreenSign", "on" );
    inParamNodes.addElement( inParam2Tree );
    IAvTree inParam3Tree = createInParam( "light2", "metro.TrafficLightSign", "bitStl_25BlueSign", "on" );
    inParamNodes.addElement( inParam3Tree );
    ruleNodes.put( IN_PARAM_SECTION, inParamNodes );

    // Создаем вЫходные параметры
    AvTree outParamNodes = AvTree.createArrayAvTree();
    IAvTree outParam1Tree = createOutParam( "isError", "mm.BittsevskiyPark", "bitStation", "fuseSpareOn" );
    outParamNodes.addElement( outParam1Tree );
    ruleNodes.put( OUT_PARAM_SECTION, outParamNodes );

    IAvTree sectioTree = AvTree.createSingleAvTree( RULE_SECTIONS, ruleOpSet, ruleNodes );
    return sectioTree;
  }

  @SuppressWarnings( "unchecked" )
  private static IAvTree createInParam( String aScriptName, String aClassId, String aObjName, String aDataId ) {

    IOptionSetEdit inParamOpSet = new OptionSet();
    inParamOpSet.setStr( IN_PARAM_SCRIPT_NAME, aScriptName );
    inParamOpSet.setStr( IN_PARAM_CLASS_ID, aClassId );
    inParamOpSet.setStr( IN_PARAM_OBJ_NAME, aObjName );
    inParamOpSet.setStr( IN_PARAM_DATA_ID, aDataId );

    IAvTree inParamTree = AvTree.createSingleAvTree( IN_PARAM_SECTION, inParamOpSet, IStringMap.EMPTY );
    return inParamTree;
  }

  @SuppressWarnings( "unchecked" )
  private static IAvTree createOutParam( String aScriptName, String aClassId, String aObjName, String aDataId ) {

    IOptionSetEdit outParamOpSet = new OptionSet();
    outParamOpSet.setStr( OUT_PARAM_SCRIPT_NAME, aScriptName );
    outParamOpSet.setStr( OUT_PARAM_CLASS_ID, aClassId );
    outParamOpSet.setStr( OUT_PARAM_OBJ_NAME, aObjName );
    outParamOpSet.setStr( OUT_PARAM_DATA_ID, aDataId );

    IAvTree outParamTree = AvTree.createSingleAvTree( OUT_PARAM_SECTION, outParamOpSet, IStringMap.EMPTY );
    return outParamTree;
  }

  /**
   * @param args аргументы командной строки
   */
  public static void main( String[] args ) {
    try {
      // testRead();
      testWrite();
    }
    catch( Exception e ) {
      logger.error( e );
    }
  }

}

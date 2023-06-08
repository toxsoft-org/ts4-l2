package ru.toxsoft.l2.utils.opc.cfg.exe.gwp;

import static ru.toxsoft.l2.dlms.pins.base.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.utils.opc.cfg.exe.ISkResources.*;
import static ru.toxsoft.l2.utils.opc.cfg.exe.ISysClassRowDef.*;

import java.io.*;
import java.util.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.*;

public class GwpOpcOdsToBaseDlmConvertor {

  private static final ILogger logger = LoggerUtils.errorLogger();

  private static boolean USE_EVENT_SCRIPT_VER = false;

  private static boolean USE_COMMAND_SCRIPT_VER = false;

  private static boolean USE_DATA_SCRIPT_VER = false;

  private static final String VALUE_COMMAND_EXEC_CLASS  =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.ValueCommandExec";
  private static final String SCRIPT_COMMAND_EXEC_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.ScriptCommandExec";

  private static final String CMD_PARAM_VALUE_TEMPLATE          = "value";
  private static final String ONE_TAG_TO_ONE_PARAM_FORMER_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.events.OneTagToOneParamFormer";

  private static final String ONE_TAG_TO_CHANGED_PARAM_FORMER_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.events.OneTagToChangedParamFormer";

  private static final String SCRIPT_PARAM_FORMER_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.events.ScriptParamFormer";

  private static final String ONE_TAG_SWITCH_EVENT_CONDITION_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.events.OneTagSwitchEventCondition";

  private static final String ONE_INT_TAG_CHANGE_EVENT_CONDITION_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.events.OneIntTagChangedEventCondition";

  private static final String ONE_FLOAT_TAG_CHANGE_EVENT_CONDITION_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.events.OneFloatTagChangedEventCondition";

  private static final String SCRIPT_EVENT_CONDITION_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.events.ScriptEventCondition";

  private static final String OPC_TAGS_EVENT_SENDER_CLASS                       =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.events.OpcTagsEventSender";
  private static final String TAG_ID_FOR_CONDITION                              = "tag1";
  private static final String TAG_ID_FOR_PARAM_FORMER                           = "tag2";
  private static final String ONE_TO_ONE_DATA_TRANSMITTER_FACTORY_CLASS         =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.data.OneToOneDataTransmitterFactory";
  private static final String ONE_INT_TO_ONE_BIT_DATA_TRANSMITTER_FACTORY_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.data.SingleIntToSingleBoolDataTransmitterFactory";

  private static final String SCRIPT_DATA_TRANSMITTER_FACTORY_CLASS =
      "ru.toxsoft.l2.dlm.opc_bridge.submodules.data.ScriptDataTransmitterFactory";

  private static final String ERR_BASIS_DATA_STRING_IS_NULL_FOR_TAG            = "Basis data string is NULL for tag %s";
  private static final String ERR_COUNT_OF_TRIGGERS_MUST_BE_MORE_THAN_0        =
      "Count of triggers  must be more than 0 (Tag %s, Event %s)";
  private static final String ERR_COULD_NOT_FIND_EVENT_PARAM                   =
      "Could not find event param tag %s (Tag %s, Event %s)";
  private static final String ERR_CANT_DETERMINATE_TYPE_OF_EVENT_TRIGGER_PARAM =
      "Cant determinate type of event trigger parameter (Tag %s, Event %s)";

  private static final String ERR_CANT_DETERMINATE_TYPE_OF_EVENT_FORMER_PARAM =
      "Cant determinate type of event former parameter (Tag %s, Param %s)";

  private static final String DLM_CFG_NODE_ID_TEMPLATE = "dispatch.dlm.cfg";
  private static final String TMP_DEST_FILE            = "destDlmFile.tmp";
  private static final String DLM_CONFIG_STR           = "DlmConfig = ";
  private static final String DLM_ID_TEMPLATE          = "ru.toxsoft.l2.dlm.tags.common.OpcBridgeDlm";
  private static final String DLM_DESCR_TEMPLATE       = "ru.toxsoft.l2.dlm.tags.common.OpcBridgeDlm";

  private static final String DESCRIPTION_STR          = "description";
  private static final String ID_STR                   = "id";
  private static final String DATA_DEF_FORMAT          = "data.%s.def";
  private static final String TRIGGER_TYPE_OFF         = "off";
  private static final String TRIGGER_TYPE_ON          = "on";
  private static final String EVENT_TRIGGER_DEF_FORMAT = "event.trigger.%s.def";
  private static final String SRC_PARAM_DEF_FORMAT     = "src.param.%s.def";
  private static final String EVENT_PARAMS_FORMER_DEF  = "event.params.former.def";
  private static final String EVENT_DEF_FORMAT         = "event.%s.def";
  private static final String PARAM_DEF_FORMAT         = "param.%s.def";
  private static final String CMD_DEF_FORMAT           = "cmd.%s.def";
  private static final String CLASS_DEF_FORMAT         = "class.%s.def";

  public static List<IOptionSet> stringDatas;

  public static String objectIdPrefix = new String();// "ci_1.";

  /**
   * Запускает программу.
   *
   * @param args - 0 параметр - исходный ODS-файл, 1 параметр - целевой dlmcfg-файл.
   */
  public static void main( String[] args ) {
    generate( args[0], args[1] );
  }

  /**
   * Генерация файла конфигурации из файла описания сигналов
   *
   * @param aSourceOdsFile - исходный Ods файл описания
   * @param aTargetThdFile - целевой файл *.dlmcfg
   * @return результат выполнения операции { @link ValidationResult }
   */
  public static ValidationResult generate( String aSourceOdsFile, String aTargetThdFile ) {

    String classSheetName = "Классы";
    GwpTagsOdsReader reader = new GwpTagsOdsReader( new File( aSourceOdsFile ), classSheetName );

    try {
      reader.read();

      stringDatas = reader.getAllRows();
    }
    catch( IOException e ) {
      logger.error( e.getMessage() );
      return ValidationResult.error( FMT_ERR_READ_SHEET, aSourceOdsFile );
    }

    try {
      formDlmFile( aTargetThdFile );
    }
    catch( IOException e ) {
      logger.error( e.getMessage() );
      return ValidationResult.error( FMT_ERR_FORM_DLM_FILE, aTargetThdFile );
    }
    return ValidationResult.SUCCESS;
  }

  public static void formDlmFile( String aDstFile )
      throws IOException {
    StringMap<IAvTree> nodes = new StringMap<>();

    // данные
    IAvTree datasMassivTree = createDatas();
    nodes.put( DATA_DEFS, datasMassivTree );

    // перечисление возможных команд по классам
    IAvTree commandInfoesMassivTree = createCommandInfoes();
    nodes.put( CMD_CLASS_DEFS, commandInfoesMassivTree );

    // команды
    IAvTree commandsMassivTree = createCommands();
    nodes.put( CMD_DEFS, commandsMassivTree );

    // события
    IAvTree eventsMassivTree = createEvents();
    nodes.put( EVENT_DEFS, eventsMassivTree );

    IOptionSetEdit opSet = new OptionSet();

    opSet.setStr( ID_STR, DLM_ID_TEMPLATE );
    opSet.setStr( DESCRIPTION_STR, DLM_DESCR_TEMPLATE );

    IAvTree dstParams = AvTree.createSingleAvTree( DLM_CFG_NODE_ID_TEMPLATE, opSet, nodes );

    String tmpDlmDestFile = TMP_DEST_FILE;

    GwpOpcUaOdsToOpcThdConvertor.saveDstTree( tmpDlmDestFile, dstParams );

    PinsConfigFileFormatter.format( tmpDlmDestFile, aDstFile, DLM_CONFIG_STR );
    // Dima, 06/03/16
    try {
      File file = new File( tmpDlmDestFile );
      if( file.delete() ) {
        System.out.println( file.getName() + " is deleted!" );
      }
      else {
        System.out.println( "Delete operation is failed." );
      }
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
    System.out.println( "Operation completed." );
  }

  /**
   * Создаёт и возвращает перечисление возможных команд по классам.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  private static IAvTree createCommandInfoes() {
    AvTree commandInfoesMassivTree = AvTree.createArrayAvTree();

    return commandInfoesMassivTree;
  }

  /**
   * Создаёт и возвращает конфигурацию всех событий.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  private static IAvTree createCommands() {
    AvTree commandsMassivTree = AvTree.createArrayAvTree();

    return commandsMassivTree;
  }

  /**
   * Создаёт и возвращает конфигурацию всех событий.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  private static IAvTree createEvents() {
    AvTree eventsMassivTree = AvTree.createArrayAvTree();

    return eventsMassivTree;
  }

  /**
   * Создаёт конфигурацию всех данных для подмодуля данных базового DLM.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  private static IAvTree createDatas() {
    AvTree pinsMassivTree = AvTree.createArrayAvTree();

    for( int i = 0; i < stringDatas.size(); i++ ) {
      IOptionSet tagData = stringDatas.get( i );

      pinsMassivTree.addElement( createDataPin( tagData ) );
    }

    return pinsMassivTree;
  }

  /**
   * Создаёт конфигурацию одного данного (пина-тега) для подмодуля данных базового DLM.
   *
   * @param aTagData StringData - описание данного (пина-тега). Предполагается что все параметры заполнены.
   * @return IAvTree - конфигурация в стандартном виде.
   */
  @SuppressWarnings( "unchecked" )
  private static IAvTree createDataPin( IOptionSet aTagData ) {
    String pinId = GwpOpcUaOdsToOpcThdConvertor.getPinId( TAG_PATH_PARAM.getValue( aTagData ).asString() );

    // синхронный
    boolean isSynch = true;// aTagData.getTagSynchType() == ETagSynchType.SYNCH;

    String tagPath = TAG_PATH_PARAM.getValue( aTagData ).asString();

    // класс
    String classId = CLASS_ID_PARAM.getValue( aTagData ).asString();

    String objName = objectIdPrefix + OBJ_NAME_PARAM.getValue( aTagData ).asString();
    String dataId = DATA_ID_PARAM.getValue( aTagData ).asString();

    IOptionSetEdit pinOpSet1 = new OptionSet();
    pinOpSet1.setStr( PIN_ID, pinId );

    pinOpSet1.setStr( JAVA_CLASS, ONE_TO_ONE_DATA_TRANSMITTER_FACTORY_CLASS );

    // класс-объект-данное - остаётся как есть
    pinOpSet1.setStr( CLASS_ID, classId );
    pinOpSet1.setStr( OBJ_NAME, objName );
    pinOpSet1.setStr( DATA_ID, dataId );

    // вместо пина - данные о теге
    // идентификатор OPC-устройства (драйвера)
    pinOpSet1.setStr( TAG_DEVICE_ID, OpcOdsToOpcThdConvertor.OPC_TAG_DEVICE );

    // сам идентфикатор тега
    pinOpSet1.setStr( TAG_ID, tagPath );

    // признак текущности, историчности
    pinOpSet1.setBool( IS_HIST, true );
    pinOpSet1.setBool( IS_CURR, true );

    // признак синхронности заменён на конкретное значение периода синхронизации
    if( isSynch ) {
      pinOpSet1.setLong( SYNCH_PERIOD, 1000L );
    }

    try {
      IAvTree pinTree1 =
          AvTree.createSingleAvTree( String.format( DATA_DEF_FORMAT, pinId ), pinOpSet1, IStringMap.EMPTY );
      return pinTree1;
    }
    catch( TsValidationFailedRtException e ) {
      System.out.println( "Validation Exception for id:" + TAG_NAME_PARAM.getValue( aTagData ).asString() ); //$NON-NLS-1$
      throw e;
    }

  }
}

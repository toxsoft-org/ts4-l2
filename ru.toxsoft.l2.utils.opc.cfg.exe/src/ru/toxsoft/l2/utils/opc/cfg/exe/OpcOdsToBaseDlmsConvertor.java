package ru.toxsoft.l2.utils.opc.cfg.exe;

import static ru.toxsoft.l2.dlms.pins.base.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.utils.opc.cfg.exe.ISkResources.*;

import java.io.*;
import java.util.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.OdsFileReader.*;

/**
 * Конвертор ODS-файла с описанием OPC в файл конфигурации базовых (бибилиотечных) модулей.
 *
 * @author max
 */
public class OpcOdsToBaseDlmsConvertor {

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

  private static IListEdit<StringData> stringDatas;

  /**
   * Запускает программу.
   *
   * @param args - 0 параметр - исходный ODS-файл, 1 параметр - целевой dlmcfg-файл.
   */
  public static void main( String[] args ) {
    String srcFileName = args[0];

    try {
      // stringDatas = OdsFileReader.readSheet( srcFileName );
      stringDatas = TwoTabsOdsFileReader.readSheet( srcFileName );
      System.out.println( "data size = " + stringDatas.size() );
    }
    catch( IOException e ) {
      e.printStackTrace();
      return;
    }

    try {
      formDlmFile( args[1] );
    }
    catch( IOException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Генерация файла конфигурации из файла описания сигналов
   *
   * @param aSourceOdsFile - исходный Ods файл описания
   * @param aTargetThdFile - целевой файл *.dlmcfg
   * @return результат выполнения операции { @link ValidationResult }
   */
  public static ValidationResult generate( String aSourceOdsFile, String aTargetThdFile ) {

    try {
      stringDatas = TwoTabsOdsFileReader.readSheet( aSourceOdsFile );
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

  private static void formDlmFile( String aDstFile )
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

    OpcOdsToOpcThdConvertor.saveDstTree( tmpDlmDestFile, dstParams );

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

    IMapEdit<String, IListEdit<String>> objsByClass = new ElemMap<>();
    IMapEdit<String, IListEdit<String>> cmdsByClass = new ElemMap<>();

    // текущая строка, содержащая полную информацию (определяется через наличие tagFullName)
    StringData currBasisData = null;

    for( StringData tagData : stringDatas ) {

      if( tagData.getTagFullName() != null && tagData.getTagFullName().trim().length() > 0 ) {
        currBasisData = tagData;
      }

      if( tagData.getCmdId() != null && tagData.getCmdId().length() > 0 ) {

        StringData basisData = null;
        // проверяем опорную строку
        if( tagData == currBasisData ) {
          basisData = tagData;
        }
        else
          if( currBasisData != null ) {// && currBasisData.getTagName().equals( tagData.getTagName() ) ) {
            basisData = currBasisData;
          }

        if( basisData == null ) {
          throw new TsIllegalArgumentRtException( ERR_BASIS_DATA_STRING_IS_NULL_FOR_TAG, tagData.getTagName() );
        }

        IListEdit<String> objs;
        IListEdit<String> cmds;

        if( objsByClass.hasKey( basisData.getClassId() ) ) {
          objs = objsByClass.getByKey( basisData.getClassId() );
          cmds = cmdsByClass.getByKey( basisData.getClassId() );
        }
        else {
          objs = new ElemArrayList<>( false );
          cmds = new ElemArrayList<>( false );

          objsByClass.put( basisData.getClassId(), objs );
          cmdsByClass.put( basisData.getClassId(), cmds );
        }

        objs.add( basisData.getObjName() );
        cmds.add( tagData.getCmdId() );

      }

    }

    Iterator<String> classIterator = objsByClass.keys().iterator();

    while( classIterator.hasNext() ) {
      String classId = classIterator.next();

      IAvTree classDef =
          createClassCommandsInfo( classId, objsByClass.getByKey( classId ), cmdsByClass.getByKey( classId ) );
      commandInfoesMassivTree.addElement( classDef );
    }

    return commandInfoesMassivTree;
  }

  /**
   * Создаёт и возвращает перечисление возможных команд одного класса.
   *
   * @param aClassId - идентификатор класса
   * @param aObjNames - имена объектов
   * @param aCmdIds - идентификаторы команд класса.
   * @return IAvTree - конфигурация в стандартном виде.
   */
  @SuppressWarnings( "unchecked" )
  private static IAvTree createClassCommandsInfo( String aClassId, IList<String> aObjNames, IList<String> aCmdIds ) {
    IOptionSetEdit pinOpSet1 = new OptionSet();
    pinOpSet1.setStr( CLASS_ID, aClassId );

    StringBuilder objList = new StringBuilder();
    String add = new String();
    for( String str : aObjNames ) {
      objList.append( add );
      objList.append( str );
      add = LIST_DELIM;
    }
    pinOpSet1.setStr( OBJ_NAMES_LIST, objList.toString() );

    StringBuilder cmdList = new StringBuilder();
    add = new String();
    for( String str : aCmdIds ) {
      cmdList.append( add );
      cmdList.append( str );
      add = LIST_DELIM;
    }
    pinOpSet1.setStr( CMD_IDS_LIST, cmdList.toString() );

    IAvTree pinTree1 =
        AvTree.createSingleAvTree( String.format( CLASS_DEF_FORMAT, aClassId ), pinOpSet1, IStringMap.EMPTY );
    return pinTree1;
  }

  /**
   * Создаёт и возвращает конфигурацию всех команд.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  @Deprecated
  private static IAvTree createCommandsOld() {
    AvTree commandsMassivTree = AvTree.createArrayAvTree();

    for( StringData tagData : stringDatas ) {
      if( tagData.getCmdId() != null && tagData.getCmdId().length() > 0 ) {
        commandsMassivTree.addElement( createCommand( tagData, null ) );
      }
    }

    return commandsMassivTree;
  }

  /**
   * Создаёт и возвращает конфигурацию всех событий.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  private static IAvTree createCommands() {
    AvTree commandsMassivTree = AvTree.createArrayAvTree();

    // текущая строка, содержащая полную информацию (определяется через наличие tagFullName)
    StringData currBasisData = null;

    for( StringData tagData : stringDatas ) {
      if( tagData.getTagFullName() != null && tagData.getTagFullName().trim().length() > 0 ) {
        currBasisData = tagData;
      }

      if( tagData.getCmdId() != null && tagData.getCmdId().length() > 0 ) {
        StringData basisData = null;
        // проверяем опорную строку
        if( tagData == currBasisData ) {
          basisData = tagData;
        }
        else
          if( currBasisData != null ) {// && currBasisData.getTagName().equals( tagData.getTagName() ) ) {
            basisData = currBasisData;
          }

        if( basisData == null ) {
          throw new TsIllegalArgumentRtException( ERR_BASIS_DATA_STRING_IS_NULL_FOR_TAG, tagData.getTagName() );
        }

        // System.out.println( "Class tag = " + tagData.getClassTagId()
        // + (tagData.getCmdWordBitIndex() >= 0 ? (" | maskCmd= " + tagData.getCmdWordBitIndex()) : "") );
        commandsMassivTree.addElement( createCommand( tagData, basisData ) );
      }
    }

    return commandsMassivTree;
  }

  /**
   * Создаёт и возвращает конфигурацию команды.
   *
   * @param aTagData StringData - данные для конфигурации команды
   * @return IAvTree - конфигурация в стандартном виде.
   */
  @SuppressWarnings( "unchecked" )
  private static IAvTree createCommand( StringData aTagData, StringData aBasisData ) {
    IOptionSetEdit cmdOpSet = new OptionSet();
    // for debug
    String tagFullName = aBasisData.getTagFullName().trim();
    System.out.println( tagFullName );

    cmdOpSet.setStr( CLASS_ID, aBasisData.getClassId() );
    cmdOpSet.setStr( OBJ_NAME, aBasisData.getObjName() );
    cmdOpSet.setStr( CMD_ID, aTagData.getCmdId().trim() );

    if( aTagData.getCmdWordBitIndex() >= 0 ) {
      cmdOpSet.setInt( BIT_INDEX, aTagData.getCmdWordBitIndex() );
    }

    if( !USE_COMMAND_SCRIPT_VER ) {
      // обычный класс обработки команд
      cmdOpSet.setStr( COMMAND_EXEC_JAVA_CLASS, VALUE_COMMAND_EXEC_CLASS );
      cmdOpSet.setStr( EVENT_VALUE_PARAM_ID_PARAM_IN_CFG, CMD_PARAM_VALUE_TEMPLATE );
    }
    else {
      // класс скриптовой обработки команд
      // Max 2016.03.17
      // для скрипта - изменить класс и добавить текст самого скрипта
      cmdOpSet.setStr( COMMAND_EXEC_JAVA_CLASS, SCRIPT_COMMAND_EXEC_CLASS );
      cmdOpSet.setStr( CMD_EXEC_SCRIPT, String.format( "%s.set(%s);", DEFAULT_TAG_ID, CMD_PARAM_VALUE_TEMPLATE ) );
    }

    // вместо пина - данные о теге
    // идентификатор OPC-устройства (драйвера)
    cmdOpSet.setStr( TAG_DEVICE_ID, OpcOdsToOpcThdConvertor.OPC_TAG_DEVICE );

    // сам идентфикатор тега

    cmdOpSet.setStr( TAG_ID, tagFullName );

    // cmdOpSet.setStr( PIN_ID, aTagData.getPinId().trim() );

    IAvTree eventTree = AvTree.createSingleAvTree( String.format( CMD_DEF_FORMAT, aTagData.getCmdId().trim() ),
        cmdOpSet, IStringMap.EMPTY );

    return eventTree;
  }

  /**
   * Создаёт и возвращает конфигурацию всех событий.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  private static IAvTree createEvents() {
    AvTree eventsMassivTree = AvTree.createArrayAvTree();

    // текущая строка, содержащая полную информацию (определяется через наличие tagFullName)
    StringData currBasisData = null;

    for( StringData tagData : stringDatas ) {
      if( tagData.getTagFullName() != null && tagData.getTagFullName().trim().length() > 0 ) {
        currBasisData = tagData;
      }

      if( tagData.getEventData().getEventId() != null && tagData.getEventData().getEventId().length() > 0 ) {
        StringData basisData = null;
        // проверяем опорную строку
        if( tagData == currBasisData ) {
          basisData = tagData;
        }
        else
          if( currBasisData != null ) {// && currBasisData.getTagName().equals( tagData.getTagName() ) ) {
            basisData = currBasisData;
          }

        if( basisData == null ) {
          throw new TsIllegalArgumentRtException( ERR_BASIS_DATA_STRING_IS_NULL_FOR_TAG, tagData.getTagName() );
        }

        eventsMassivTree.addElement( createEvent( tagData, basisData ) );
      }
    }

    return eventsMassivTree;
  }

  /**
   * Создаёт конфигурацию события и возвращает её.
   *
   * @param aTagData StringData - данные, определяющие событие
   * @param aBasisData StringData - опорные данные, определяющие все параметры, необходимые для конфигурации события,
   *          кроме непосредственно данных о самом событии
   * @return IAvTree - конфигурация события.
   */
  @SuppressWarnings( "unchecked" )
  private static IAvTree createEvent( StringData aTagData, StringData aBasisData ) {
    EventStringData eventData = aTagData.getEventData();

    // проверка корректности входных данных: тег либо булевый, либо интовый, но тогда должен быть указан номер бита
    // TsIllegalArgumentRtException.checkFalse( aBasisData.getTagValueType() == ETagValueType.FLOAT
    // || aBasisData.getTagValueType() == ETagValueType.BOOLEAN
    // || (aBasisData.getTagValueType() == ETagValueType.INTEGER && eventData.getTriggerWordBitIndex() >= 0)
    // , ERR_CANT_DETERMINATE_TYPE_OF_EVENT_TRIGGER_PARAM, aTagData.objName+" - "+ aTagData.getTagName(),
    // eventData.getEventId() );

    IOptionSetEdit eventOpSet = new OptionSet();

    eventOpSet.setStr( CLASS_ID, aBasisData.getClassId() );
    eventOpSet.setStr( OBJ_NAME, aBasisData.getObjName() );
    eventOpSet.setStr( EVENT_ID, eventData.getEventId() );

    eventOpSet.setStr( EVENT_SENDER_JAVA_CLASS, OPC_TAGS_EVENT_SENDER_CLASS );

    // определитель события для разных типов тегов
    ETagValueType tagValType = aBasisData.getTagValueType();
    String eventConditionClass = null;
    String eventParamFormClass = null;
    String eventParamsList = null;

    if( tagValType == ETagValueType.BOOLEAN ) {
      eventConditionClass = ONE_TAG_SWITCH_EVENT_CONDITION_CLASS;
      eventParamFormClass = ONE_TAG_TO_ONE_PARAM_FORMER_CLASS;
      eventParamsList = "on";
    }
    else
      if( tagValType == ETagValueType.INTEGER ) {
        if( eventData.getTriggerWordBitIndex() >= 0 ) {
          eventConditionClass = ONE_TAG_SWITCH_EVENT_CONDITION_CLASS;
          eventParamFormClass = ONE_TAG_TO_ONE_PARAM_FORMER_CLASS;
          eventParamsList = "on";
        }
        else {
          if( eventData.isOnTrigger() || eventData.isOffTrigger() ) {
            eventConditionClass = ONE_TAG_SWITCH_EVENT_CONDITION_CLASS;
            // не понятен параметр, который ставить в событии - посмотреть в системно описании TODO
            // eventParamFormClass = ONE_TAG_TO_ONE_PARAM_FORMER_CLASS;
            // eventParamsList = "on";
          }
          else {
            eventConditionClass = ONE_INT_TAG_CHANGE_EVENT_CONDITION_CLASS;
            eventParamFormClass = ONE_TAG_TO_CHANGED_PARAM_FORMER_CLASS;
            eventParamsList = "oldVal;newVal";
          }
        }
      }
      else
        if( tagValType == ETagValueType.FLOAT ) {
          eventConditionClass = ONE_FLOAT_TAG_CHANGE_EVENT_CONDITION_CLASS;
          eventParamFormClass = ONE_TAG_TO_CHANGED_PARAM_FORMER_CLASS;
          eventParamsList = "oldVal;newVal";
        }

    if( !USE_EVENT_SCRIPT_VER ) {

      // обычный триггер (определитель) события и его параметры
      eventOpSet.setStr( CONDITION_JAVA_CLASS, eventConditionClass );

      // параметры обычного триггера: - "включение", "выключение", номер бита
      eventOpSet.setBool( CONDITION_SWITCH_ON, eventData.isOnTrigger() );
      eventOpSet.setBool( CONDITION_SWITCH_OFF, eventData.isOffTrigger() );
      if( aBasisData.getTagValueType() == ETagValueType.INTEGER && eventData.getTriggerWordBitIndex() >= 0 ) {
        eventOpSet.setInt( BIT_INDEX, eventData.getTriggerWordBitIndex() );
      }
    }

    // идентификатор тега триггера в конфиг файле
    String conditionTagId = DEFAULT_TAG_ID;

    // вместо пина - данные о теге
    // идентификатор OPC-устройства (драйвера)
    eventOpSet.setStr( TAG_DEVICE_ID, OpcOdsToOpcThdConvertor.OPC_TAG_DEVICE );

    // сам идентфикатор тега
    eventOpSet.setStr( TAG_ID, aBasisData.getTagFullName().trim() );

    // max 2016.02.28
    // всегда есть один параметр "on", значение которого равно тегу (или биту тега), который определяет событие
    if( !USE_EVENT_SCRIPT_VER ) {
      eventOpSet.setStr( PARAM_FORMER_JAVA_CLASS, eventParamFormClass );
      eventOpSet.setStr( FORMER_EVENT_PARAMS, eventParamsList );
    }
    else {
      // для скрипта
      eventOpSet.setStr( PARAM_FORMER_JAVA_CLASS, SCRIPT_PARAM_FORMER_CLASS );
      formParamScript( eventOpSet, eventData.getTriggerWordBitIndex(), DEFAULT_TAG_ID, "on" );
      eventOpSet.setStr( FORMER_EVENT_PARAMS, eventParamsList );
    }

    StringMap<IAvTree> eventNodes = new StringMap<>();

    // max 2016.02.28
    // если есть второй параметр события (определяется указанием тега значения параметра), то
    // добавить второй тег (сформировать массив из двух тегов) и добавить формирователь параметров (сформировать массив)
    if( eventData.getParamTagName() != null && eventData.getParamTagName().length() > 0 ) {
      StringData paramsStringData = searchStringDataByFullTagName( eventData.getParamTagName() );
      if( paramsStringData == null ) {
        throw new TsIllegalArgumentRtException( ERR_COULD_NOT_FIND_EVENT_PARAM, eventData.getParamTagName(),
            aTagData.getTagName(), eventData.getEventId() );
      }

      // идентификатор тега параметра события в конфиг файле
      String valueTag = DEFAULT_TAG_ID;

      // если тег параметра события и тег определяющий событие один - то можно ничего не делать
      // если это разные теги - то нужно их поместить отдельно и в каждом указать к какому он условию
      // и к какому он формирователю параметров

      if( !paramsStringData.getTagFullName().equals( aBasisData.getTagFullName() ) ) {
        AvTree tagsMassivTree = AvTree.createArrayAvTree();

        // уже настроенный тег для триггера (он же для формирователя параметра "on")
        IOptionSetEdit tag1OpSet = new OptionSet();
        tag1OpSet.setStr( EVENT_TAG_CONDITIONS, DEFAULT_ID );
        // max 2016.02.28
        tag1OpSet.setStr( EVENT_TAG_PARAM_FORMERS, "on.param.former" );

        IAvTree tag1 = AvTree.createSingleAvTree( TAG_ID_FOR_CONDITION, tag1OpSet, IStringMap.EMPTY );
        conditionTagId = TAG_ID_FOR_CONDITION;

        tagsMassivTree.addElement( tag1 );

        // второй тег для формирователя параметров

        IOptionSetEdit tag2OpSet = new OptionSet();
        tag2OpSet.setStr( TAG_ID, paramsStringData.getTagFullName().trim() );
        // max 2016.02.28
        tag2OpSet.setStr( EVENT_TAG_PARAM_FORMERS, "value.param.former" );

        IAvTree tag2 = AvTree.createSingleAvTree( TAG_ID_FOR_PARAM_FORMER, tag2OpSet, IStringMap.EMPTY );
        valueTag = TAG_ID_FOR_PARAM_FORMER;

        tagsMassivTree.addElement( tag2 );

        eventNodes.put( EVENT_TAGS_ARRAY, tagsMassivTree );
      }

      // max 2016.02.28
      // формируем два формирователя
      // первый - для параметра "on" - полностью повторяет то, что по умолчанию (механизм по умолчанию пока не работает
      // для условий и формирователей)
      // обычный
      IOptionSetEdit onParamsFormerOpSet = new OptionSet();
      if( !USE_EVENT_SCRIPT_VER ) {
        onParamsFormerOpSet.setInt( BIT_INDEX, eventData.getTriggerWordBitIndex() );
      }
      else {
        // скриптовый
        // onParamsFormerOpSet.setStr( PARAM_FORMER_JAVA_CLASS, SCRIPT_PARAM_FORMER_CLASS );
        formParamScript( onParamsFormerOpSet, eventData.getTriggerWordBitIndex(), conditionTagId, "on" );
        // удалить чтоб глаза не мозолил
        eventOpSet.removeByKey( EVENT_PARAM_FORMER_SCRIPT );
      }
      onParamsFormerOpSet.setStr( FORMER_EVENT_PARAMS, "on" );

      IAvTree onParanFormer = AvTree.createSingleAvTree( "on.param.former", onParamsFormerOpSet, IStringMap.EMPTY );
      eventNodes.put( "params.former1.params", onParanFormer );

      // второй для параметра "value"
      IOptionSetEdit valueParamsFormerOpSet = new OptionSet();

      if( !USE_EVENT_SCRIPT_VER ) {
        // чтобы случайно не брался один бит, а брался параметр целиком
        valueParamsFormerOpSet.setInt( BIT_INDEX, -1 );
      }
      else {
        // скриптовый
        // valueParamsFormerOpSet.setStr( PARAM_FORMER_JAVA_CLASS, SCRIPT_PARAM_FORMER_CLASS );
        formParamScript( valueParamsFormerOpSet, -1, valueTag, "value" );
      }
      valueParamsFormerOpSet.setStr( FORMER_EVENT_PARAMS, "value" );

      IAvTree valueParanFormer =
          AvTree.createSingleAvTree( "value.param.former", valueParamsFormerOpSet, IStringMap.EMPTY );
      eventNodes.put( "params.former2.params", valueParanFormer );

    }

    if( USE_EVENT_SCRIPT_VER ) {
      // класс скриптового триггера
      // Max 2016.03.17
      // для скрипта - изменить класс и добавить текст самого скрипта
      eventOpSet.setStr( CONDITION_JAVA_CLASS, SCRIPT_EVENT_CONDITION_CLASS );
      // далее формирование текста скрипта
      formConditionScript( eventOpSet, eventData, conditionTagId );
    }

    IAvTree eventTree =
        AvTree.createSingleAvTree( String.format( EVENT_DEF_FORMAT, eventData.getEventId() ), eventOpSet, eventNodes );

    return eventTree;
  }

  private static void formConditionScript( IOptionSetEdit aEventOpSet, EventStringData aEventData,
      String aConditionTagId ) {
    String tagId = aConditionTagId;
    int bitIndex = aEventData.getTriggerWordBitIndex();

    boolean isOn = aEventData.isOnTrigger();
    boolean isOff = aEventData.isOffTrigger();

    String intValueFormat = "(((%s.get().asInt() >> %d) %% 2) == 1)"; //$NON-NLS-1$
    String boolValueFormat = "%s.get().asBool()"; //$NON-NLS-1$
    String isOnEventStr = "(tValue==true && (prev==null || prev == false))"; //$NON-NLS-1$
    String isOffEventStr = "(tValue==false && (prev==null || prev == true))"; //$NON-NLS-1$
    String orStr = "||"; //$NON-NLS-1$
    String emptyStr = ""; //$NON-NLS-1$

    String valueStr = bitIndex >= 0 ? String.format( intValueFormat, tagId, Integer.valueOf( bitIndex ) )
        : String.format( boolValueFormat, tagId );

    String initScript = "var prev = null;"; //$NON-NLS-1$
    String conditionScript = String.format(
        "if(%s.get()==null || !%s.get().isAssigned()) {var is_event = false;}else{var tValue=%s; var is_event =%s%s%s;prev = tValue;}", //$NON-NLS-1$
        tagId, tagId, valueStr, isOn ? isOnEventStr : emptyStr, (isOn && isOff) ? orStr : emptyStr,
        isOff ? isOffEventStr : emptyStr );

    aEventOpSet.setStr( EVENT_INIT_COND_SCRIPT, initScript );
    aEventOpSet.setStr( EVENT_COND_SCRIPT, conditionScript );
  }

  private static void formParamScript( IOptionSetEdit aParamOpSet, int aBitIndex, String aParamTagId,
      String aParamId ) {
    String tagId = aParamTagId;
    int bitIndex = aBitIndex;
    String paramId = aParamId;

    String intValueFormat = "(((%s.get().asInt() >> %d) %% 2) == 1)"; //$NON-NLS-1$
    String boolValueFormat = "%s.get().asBool()"; //$NON-NLS-1$

    String valueStr = bitIndex >= 0 ? String.format( intValueFormat, tagId, Integer.valueOf( bitIndex ) )
        : String.format( boolValueFormat, tagId );
    String ppp = paramId.replaceAll( DOT, DASH );
    String paramFormerScript = String.format(
        "if(%s.get()!=null && %s.get().isAssigned()){var %s=Packages.ru.toxsoft.tslib.datavalue.impl.DvUtils.avBool( %s );}", //$NON-NLS-1$
        tagId, tagId, ppp, valueStr );

    aParamOpSet.setStr( EVENT_PARAM_FORMER_SCRIPT, paramFormerScript );
  }

  /**
   * Ищет и возвращает конфигурационные данные одной строки по полному имени тега.
   *
   * @param aTagFullName String - полное имя тега.
   * @return StringData - онфигурационные данные одной строки, null - если не удалось найти.
   */
  private static StringData searchStringDataByFullTagName( String aTagFullName ) {
    for( StringData data : stringDatas ) {
      if( data.getTagFullName().equals( aTagFullName ) ) {
        return data;
      }
    }
    return null;
  }

  /**
   * Создаёт конфигурацию триггера события и возвращает её.
   *
   * @param aTagData StringData - данные, определяющие событие
   * @param aBasisData StringData - опорные данные, определяющие все параметры, необходимые для конфигурации события,
   *          кроме непосредственно данных о самом событии
   * @param aOnTrigger boolean - true - триггера перехода 0->1, false - триггер перехода 1->0
   * @return IAvTree - конфигурация триггера события.
   */
  @SuppressWarnings( { "unchecked", "unused" } )
  @Deprecated
  private static IAvTree createTrigger( StringData aTagData, StringData aBasisData, boolean aOnTrigger ) {
    EventStringData eventData = aTagData.getEventData();
    String triggerParamType = null;

    IOptionSetEdit opSet = new OptionSet();
    if( aBasisData.getTagValueType() == ETagValueType.BOOLEAN ) {
      triggerParamType = SRC_PIN_DI;
      opSet.setStr( TYPE_PARAM, triggerParamType );
    }
    if( aBasisData.getTagValueType() == ETagValueType.INTEGER && eventData.getTriggerWordBitIndex() >= 0 ) {
      triggerParamType = SRC_PIN_AI;
      opSet.setStr( TYPE_PARAM, triggerParamType );
      opSet.setInt( BIT_INDEX, eventData.getTriggerWordBitIndex() );
    }

    if( triggerParamType == null ) {
      throw new TsIllegalArgumentRtException( ERR_CANT_DETERMINATE_TYPE_OF_EVENT_TRIGGER_PARAM, aTagData.getTagName(),
          eventData.getEventId() );
    }

    opSet.setStr( PIN_ID, aBasisData.getPinId() );

    opSet.setStr( BOOL_EVENT_VALUE, String.valueOf( aOnTrigger ) );

    IAvTree paramTree = null;
    try {
      paramTree = AvTree.createSingleAvTree( String.format( SRC_PARAM_DEF_FORMAT, aBasisData.getPinId() ), opSet,
          IStringMap.EMPTY );
    }
    catch( TsValidationFailedRtException e ) {
      System.out.println( String.format( "Validation Exception for id: %s , tag = %s", aBasisData.getPinId(), //$NON-NLS-1$
          aBasisData.getTagName() ) );
      throw e;
    }
    AvTree paramsMassivTree = AvTree.createArrayAvTree();
    paramsMassivTree.addElement( paramTree );

    IOptionSetEdit triggerOpSet = new OptionSet();
    triggerOpSet.setStr( TYPE_PARAM, FRM_VALUES );

    StringMap<IAvTree> paramNodes = new StringMap<>();

    paramNodes.put( EVENTS_PARAMS, paramsMassivTree );

    IAvTree triggerTree = AvTree.createSingleAvTree(
        String.format( EVENT_TRIGGER_DEF_FORMAT, (aOnTrigger ? TRIGGER_TYPE_ON : TRIGGER_TYPE_OFF) ), triggerOpSet,
        paramNodes );

    return triggerTree;

  }

  /**
   * Создаёт конфигурацию всех данных для подмодуля данных базового DLM.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  private static IAvTree createDatas() {
    AvTree pinsMassivTree = AvTree.createArrayAvTree();

    // Set<String> alreadyAddedTags = new HashSet<>();

    StringData prevTagData = null;

    boolean bitDataOn = false;

    for( int i = 0; i < stringDatas.size(); i++ ) {
      StringData tagData = stringDatas.get( i );

      if( // alreadyAddedTags.contains( tagData.getTagFullName() ) ||
      tagData.getDataId() == null || tagData.getDataId().length() == 0 ) {
        continue;
      }

      if( bitDataOn ) {
        if( tagData.getCmdWordBitIndex() < 0 ) {
          bitDataOn = false;
          prevTagData = tagData;
        }
      }

      if( i + 1 < stringDatas.size() ) {
        StringData nextTagData = stringDatas.get( i + 1 );

        if( nextTagData.getCmdWordBitIndex() >= 0 ) {
          if( !bitDataOn ) {
            prevTagData = tagData;
            bitDataOn = true;
            continue;
          }
        }

      }

      System.out.println( "Class tag = " + tagData.getClassTagId()
          + (tagData.getCmdWordBitIndex() >= 0 ? ("     |    mask= " + tagData.getCmdWordBitIndex()) : "") );

      pinsMassivTree.addElement( createDataPin( tagData, bitDataOn ? prevTagData : tagData ) );

      // alreadyAddedTags.add( tagData.getTagFullName() );
    }

    return pinsMassivTree;
  }

  /**
   * Создаёт конфигурацию всех данных для подмодуля данных базового DLM.
   *
   * @return IAvTree - конфигурация в стандартном виде.
   */
  private static IAvTree createDatasOld() {
    AvTree pinsMassivTree = AvTree.createArrayAvTree();

    Set<String> alreadyAddedTags = new HashSet<>();

    for( StringData tagData : stringDatas ) {

      if( alreadyAddedTags.contains( tagData.getTagFullName() ) || tagData.getDataId() == null
          || tagData.getDataId().length() == 0 ) {
        continue;
      }

      System.out.println( "Class tag = " + tagData.getClassTagId()
          + (tagData.getCmdWordBitIndex() >= 0 ? ("     |    mask= " + tagData.getCmdWordBitIndex()) : "") );

      alreadyAddedTags.add( tagData.getTagFullName() );

      pinsMassivTree.addElement( createDataPin( tagData, tagData ) );
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
  private static IAvTree createDataPin( StringData aTagData, StringData aBasisTagData ) {
    String pinId = aBasisTagData.getPinId();

    if( pinId == null ) {
      throw new TsIllegalArgumentRtException( "pin id == null for tag: %s", aTagData.getTagFullName() ); //$NON-NLS-1$
    }

    // синхронный
    boolean isSynch = aTagData.getTagSynchType() == ETagSynchType.SYNCH;

    String tagFullName = aBasisTagData.getTagFullName().trim();// .replaceAll( " ", "_" );

    // класс
    String classId = aTagData.getClassId();
    String objName = aTagData.getObjName();
    String dataId = aTagData.getDataId();

    IOptionSetEdit pinOpSet1 = new OptionSet();
    pinOpSet1.setStr( PIN_ID, pinId );
    // pinOpSet1.setStr( CLASS_ID, classId );
    // pinOpSet1.setStr( OBJ_NAME, objName );
    // pinOpSet1.setStr( DATA_ID, dataId );
    // pinOpSet1.setBool( IS_HIST, true );
    // pinOpSet1.setBool( IS_CURR, true );
    // pinOpSet1.setBool( IS_SYNCH, isSynch );

    if( !USE_DATA_SCRIPT_VER ) {
      // Max 2016.01.31
      // корректировка настроек передатчика из тага в данное сервера
      // для начала - тип передатчика - через класс передатчика
      if( aTagData.getCmdWordBitIndex() < 0 ) {
        pinOpSet1.setStr( JAVA_CLASS, ONE_TO_ONE_DATA_TRANSMITTER_FACTORY_CLASS );
      }
      else {
        pinOpSet1.setStr( JAVA_CLASS, ONE_INT_TO_ONE_BIT_DATA_TRANSMITTER_FACTORY_CLASS );
      }
    }
    else {
      // Max 2016.03.04
      // для скрипта - изменить класс и добавить текст самого скрипта
      pinOpSet1.setStr( JAVA_CLASS, SCRIPT_DATA_TRANSMITTER_FACTORY_CLASS );
      pinOpSet1.setStr( DATA_TRANSMIT_SCRIPT, "var is_transmitted = data0.setDataValue(tag0.get(),curr_time);" );
    }

    // класс-объект-данное - остаётся как есть
    pinOpSet1.setStr( CLASS_ID, classId );
    pinOpSet1.setStr( OBJ_NAME, objName );
    pinOpSet1.setStr( DATA_ID, dataId );

    // дичайшая заплатка для МСС - добавление поля при наличии поля//TODO
    // data.id="rtdEnblAlarmTreat",
    // inv.data.id="rtdBlockAlarm",
    if( dataId.equals( "rtdEnblAlarmTreat" ) ) {
      pinOpSet1.setStr( "inv.data.id", "rtdBlockAlarm" );
    }

    // вместо пина - данные о теге
    // идентификатор OPC-устройства (драйвера)
    pinOpSet1.setStr( TAG_DEVICE_ID, OpcOdsToOpcThdConvertor.OPC_TAG_DEVICE );

    // сам идентфикатор тега
    pinOpSet1.setStr( TAG_ID, tagFullName );

    if( aTagData.getCmdWordBitIndex() >= 0 ) {
      pinOpSet1.setInt( BIT_INDEX, aTagData.getCmdWordBitIndex() );
    }

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
      System.out.println( "Validation Exception for id:" + tagFullName ); //$NON-NLS-1$
      throw e;
    }

  }

}

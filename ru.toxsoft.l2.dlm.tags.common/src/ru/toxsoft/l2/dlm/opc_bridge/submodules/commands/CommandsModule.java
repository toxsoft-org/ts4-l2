package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.IL2Resources.*;

import java.util.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.coll.derivative.Queue;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.util.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Класс работы с командами системы диспетчеризации, является исполнителем команд, регистрируемым в сервисе.
 *
 * @author max
 */
public class CommandsModule
    extends ConfigurableWorkerModuleBase
    implements ISkCommandExecutor {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( CommandsModule.class );

  /**
   * Контекст подгружаемых модулей.
   */
  IDlmContext context;

  /**
   * Информация о модуле DLM
   */
  private IDlmInfo dlmInfo;

  /**
   * Соединение с сервером.
   */
  ISkConnection connection;

  /**
   * Редактор состояния команд.
   */
  // ICommandStateEditor cStateEditor;

  /**
   * Очередь команд, пришедших на обработку.
   */
  private IQueue<IDtoCommand> commandsQueue;

  /**
   * Описание команд в терминах сервиса команд, обрабатываемых данным исполнителем.
   */
  private IGwidList commandsDef;

  /**
   * Определение команд из конфигурации
   */
  private IList<ProcessedCommandsDefByObjNames> commandsDefByObjNames;

  /**
   * Исполнители команд.
   */
  private IStringMapEdit<ICommandExec> cmdExecs;

  /**
   * Сконфигурированные исполнители команд.
   */
  private IListEdit<ICommandExec> cmdExecsConfigured = new ElemArrayList<>();

  private IListEdit<DataObjName> dataObjNemas = new ElemArrayList<>();

  private IListEdit<IStringMap<TagInfo>> tagInfoes = new ElemArrayList<>();

  /**
   * Конструктор по контексту.
   *
   * @param aContext {@link IDlmContext} - контекст подгружаемых модулей.
   * @param aDlmInfo IDlmInfo - информация о DLM
   */
  public CommandsModule( IDlmContext aContext, IDlmInfo aDlmInfo ) {
    context = aContext;
    dlmInfo = aDlmInfo;
  }

  @Override
  protected void doConfigYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {

    // создание по конфигурации описаний для регистрации в сервисе
    IAvTree cmdClassDefs = aConfig.params().nodes().findByKey( CMD_CLASS_DEFS );
    commandsDefByObjNames = createCmdDefs( cmdClassDefs );

    // создание локальных исполнителей команд, непосредственной выполняющих установку значений в устройство
    IAvTree cmdDefs = aConfig.params().nodes().findByKey( CMD_DEFS );
    configCommandExecs( cmdDefs );

  }

  /**
   * Создаёт по конфигурации информацию, необходимую для регистрации объекта в сервисе команд в качестве исполнителя.
   *
   * @param cmdDefs - {@link IAvTree} - конфигурационные данные.
   * @return {@link IList} - список определяющий команды, классы, объекты исполнителя.
   */
  public static IList<ProcessedCommandsDefByObjNames> createCmdDefs( IAvTree cmdDefs ) {

    IListEdit<ProcessedCommandsDefByObjNames> result = new ElemArrayList<>();

    if( cmdDefs != null && cmdDefs.isArray() ) {
      for( int i = 0; i < cmdDefs.arrayLength(); i++ ) {
        // описание команд одного класса
        IAvTree classCmdDefs = cmdDefs.arrayElement( i );

        // try {
        ProcessedCommandsDefByObjNames classCmds = creatClassCmdDefs( classCmdDefs );
        result.add( classCmds );
        // }
        // catch( TsItemNotFoundRtException | TsUnsupportedFeatureRtException | DvTypeCastRtException e ) {
        // LoggerUtils.defaultLogger().error( e );
        // }
      }
    }

    return result;
  }

  /**
   * Создаёт по конфигурации опеределение команд-объектов для регистрации в качестве исполнителя.
   *
   * @param aClassCmdDefs - {@link IAvTree} - конфигурационные данные для одного класса.
   * @return ProcessedCommandsDefByObjNames - промежуточный объект определения команд-объектов для класса.
   * @throws TsItemNotFoundRtException
   * @throws TsUnsupportedFeatureRtException
   * @throws DvTypeCastRtException
   */
  public static ProcessedCommandsDefByObjNames creatClassCmdDefs( IAvTree aClassCmdDefs )
      throws TsItemNotFoundRtException,
      TsUnsupportedFeatureRtException {
    String classId = aClassCmdDefs.fields().getStr( CLASS_ID );
    String objNamesListStr = aClassCmdDefs.fields().getStr( OBJ_NAMES_LIST );
    String cmdIdsListStr = aClassCmdDefs.fields().getStr( CMD_IDS_LIST );

    IStringListEdit objList = new StringArrayList();

    StringTokenizer st = new StringTokenizer( objNamesListStr, LIST_DELIM );
    while( st.hasMoreTokens() ) {
      String currToken = st.nextToken();
      objList.add( currToken.trim() );
    }

    IStringListEdit cmdList = new StringArrayList();

    st = new StringTokenizer( cmdIdsListStr, LIST_DELIM );
    while( st.hasMoreTokens() ) {
      String currToken = st.nextToken();
      cmdList.add( currToken.trim() );
    }

    ProcessedCommandsDefByObjNames result = new ProcessedCommandsDefByObjNames( classId, objList, cmdList );

    return result;
  }

  @Override
  protected void doStartComponent() {
    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_COMMAND_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    connection = context.network().getSkConnection();
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    // Опеределения для регистрации исполнителя
    GwidList convertedCommandsDef = new GwidList();

    // обращение к серверу с целью конвертации имён в коды
    for( ProcessedCommandsDefByObjNames cmdDefByObjName : commandsDefByObjNames ) {
      IList<Gwid> cmdDef = cmdDefByObjName.convert();
      convertedCommandsDef.addAll( cmdDef );
    }

    commandsDef = convertedCommandsDef;

    for( Gwid gd : commandsDef ) {
      logger.info( "*** Handler registered on command: %s", gd );
    }

    // создание синхронизованной очереди получаемых команд команд
    commandsQueue = new SynchronizedQueueWrapper<>( new Queue<>() );

    // регистраци модуля в качестве исполнителя команд
    connection.coreApi().cmdService().registerExecutor( this, commandsDef() );

    // соответствие локального исполнителя команде-объекту
    cmdExecs = new StringMap<>();

    // запуск локальных исполнителей
    for( int i = 0; i < cmdExecsConfigured.size(); i++ ) {
      ICommandExec cExec = cmdExecsConfigured.get( i );
      IStringMap<TagInfo> execTagsInfoes = tagInfoes.get( i );

      IStringMapEdit<ITag> execTags = new StringMap<>();

      for( String tcId : execTagsInfoes.keys() ) {
        TagInfo tc = execTagsInfoes.getByKey( tcId );

        ITsOpc tagsDevice = (ITsOpc)context.hal().listSpecificDevices().getByKey( tc.getDeviceId() );
        ITag tag = tagsDevice.tag( tc.getTagId() );

        if( tag == null ) {
          logger.error( "Tag '%s' not found", tc.getDeviceId() + " | " + tc.getTagId() );
        }
        else {
          execTags.put( tcId, tag );
        }
      }

      if( execTagsInfoes.size() != execTags.size() ) {
        continue;
      }

      DataObjName dataObjName = dataObjNemas.get( i );
      String clsId = dataObjName.getClassId();
      String objId = dataObjName.getObjName();
      String cmdId = dataObjName.getDataId();
      ISkClassInfo classInfo = connection.coreApi().sysdescr().findClassInfo( clsId );

      if( classInfo == null ) {
        logger.error( "Class '%s' not found during command '%s' exec registration", clsId, cmdId );
        continue;
      }

      if( !classInfo.cmds().list().hasKey( cmdId ) ) {
        logger.error( "Command '%s' of class '%s' not found during command exec registration", cmdId, clsId );
        continue;
      }

      if( connection.coreApi().objService().find( new Skid( clsId, objId ) ) == null ) {
        logger.error( "Object '%s' of class '%s' not found during cmd '%s' exec registration", objId, clsId, cmdId );
        continue;
      }

      cExec.start( execTags, connection.coreApi().cmdService() );

      cmdExecs.put( getTotalCommandId( cmdId, objId ), cExec );
    }

    // очистка конфигурационного набора
    cmdExecsConfigured.clear();

    // вывод на печать конфиг информации
    // for( IProcessedCommandsDef def : commandsDef ) {
    // System.out.println( "DEFFFF : " + def.classId() );
    // System.out.println( "CMDs : " );
    // for( String cmdId : def.cmdIds() ) {
    // System.out.print( cmdId + ", " );
    // }
    // System.out.println();
    //
    // System.out.println( "ObjIDs : " );
    // for( Long objId : def.objIds() ) {
    // System.out.print( objId + ", " );
    // }
    // System.out.println();
    // }

    // long testComandSentTime = System.currentTimeMillis();
  }

  // private boolean testComandVal = true;
  // private long testComandSentPeriod = 30000L;

  @Override
  protected void doDoJob() {
    // test
    // if( System.currentTimeMillis() > testComandSentTime + testComandSentPeriod ) {
    // testComandSentTime = System.currentTimeMillis();
    // Gwid cmdGwid = Gwid.createCmd( "ci.AnalogInput", "ci_1.AI_BHB", "cmdImitation" );
    // OptionSet cmdArgs = new OptionSet();
    // cmdArgs.setValue( "value", AvUtils.avBool( testComandVal ) );
    // testComandVal = !testComandVal;
    // logger.debug( "Test Command come to sending" );
    // connection.coreApi().cmdService().sendCommand( cmdGwid, new Skid( ISkUser.CLASS_ID, "root" ), cmdArgs );
    // logger.debug( "Test Command just has sent" );
    // }

    long time = System.currentTimeMillis();
    // Получение команды
    IDtoCommand cmd = commandsQueue.peekHeadOrNull();

    if( cmd != null ) {
      String cmdId = cmd.cmdGwid().propId();
      String objId = cmd.cmdGwid().strid();

      String totalCmdId = getTotalCommandId( cmdId, objId );

      ICommandExec exec = cmdExecs.findByKey( totalCmdId );
      if( exec != null ) {
        if( !exec.isBusy() ) {
          commandsQueue.getHead();
          // изменение состояния команды - принята к исполнению
          setCmdStateForApplication( cmd );

          // выполнение команды соответствующим локальным исполнителем
          exec.execCommand( cmd, time );
        }
      }

      // commandsQueue.getHeadOrNull();
    }

    // совершение работы всеми локальными исполнителями (если выполнение команды происходит не за один такт - например
    // подача импульса)
    for( ICommandExec cExec : cmdExecs.values() ) {
      cExec.doJob( time );
    }

  }

  private void setCmdStateForApplication( IDtoCommand aCmd ) {
    // Изменяем состояние команды
    ValidationResult vr = ValidationResult.info( MSG_COMMAND_COME_FOR_APPLICATION, aCmd.instanceId() );
    ValResList result = new ValResList();
    result.add( vr );

    SkCommandState state = new SkCommandState( System.currentTimeMillis(), ESkCommandState.EXECUTING );

    // ,formMessage( result ), Gwid.createObj( author() ) );
    changeCommandState( aCmd.instanceId(), state );
  }

  private void changeCommandState( String aExecCmdId, SkCommandState aCmdState ) {

    DtoCommandStateChangeInfo cmdStateChangeInfo = new DtoCommandStateChangeInfo( aExecCmdId, aCmdState );

    try {
      connection.coreApi().cmdService().changeCommandState( cmdStateChangeInfo );
      logger.debug( "State of command ( %s ) changed  on: %s", aExecCmdId, aCmdState.state().id() );
    }
    catch( Exception e ) {
      logger.error( "Cant change command ( %s ) state: %s", aExecCmdId, e.getMessage() );
      logger.error( e );
    }
  }

  /**
   * @return текущий пользователь
   */
  // private Skid author() {
  // ISkSession session = connection.coreApi().;
  // ISkUser user = session.getUser();
  // return user.skid();
  // }

  private static String formMessage( IValResList aResult ) {
    String message = aResult == null ? TsLibUtils.EMPTY_STRING
        : (!aResult.isEmpty() ? aResult.results().get( 0 ).message() : "Empty state message");
    return message;
  }

  /**
   * Возвращает полный идентфикатор команды (включающий идентификатор объекта).
   *
   * @param aCmdId String - идентфикатор команды.
   * @param aObjId long - идентификатор объекта
   * @return String - полный идентфикатор команды.
   */
  @SuppressWarnings( "nls" )
  private static String getTotalCommandId( String aCmdId, String aObjId ) {
    return "CMD#" + aCmdId + ",OBJ#" + aObjId;
  }

  @Override
  protected boolean doQueryStop() {
    connection.coreApi().cmdService().unregisterExecutor( this );
    return true;
  }

  private IGwidList commandsDef() {
    return commandsDef;
  }

  @Override
  public void executeCommand( IDtoCommand aCmd ) {
    // System.out.println( "Второй новый" );
    // System.out.println( "уникальный в системе идентификатор (ИД-путь) команды: " + aCmd.id() );
    // System.out.println( "идентификатор объекта автора команды: " + aCmd.authorObjId() );
    // System.out.println( "идентификатор объекта, которому направляется команда: " + aCmd.objId() );
    // System.out.println( "Идентификатор команды: " + aCmd.cmdId() );
    // System.out.println( "текущее состояние процесса (этапов) выполнения команды: " + aCmd.state().id() );
    // System.out.println();

    // поместить команду в очередь с синхронизированным доступом - так как команда может прийти в момент, когда основной
    // поток выполнения занят на другом участке - а выполнять команду следует в тот момнет, когда основной поток
    // выполнения заходит в этот модуль
    // cStateEditor.changeCommandState( aCmd.id(), ECommandState.EXCUTING, ValidationResult.SUCCESS );
    // TODO
    // Dima, for debug
    commandsQueue.putTail( aCmd );
    logger.debug( "Get command %s, and put into queue ", aCmd.instanceId() ); //$NON-NLS-1$
  }

  /**
   * Создаёт по конфигурации локальные исполнители - по одному исполнителю на команду.
   *
   * @param cmdDefs - {@link IAvTree} - конфигурационные данные.
   */
  private void configCommandExecs( IAvTree cmdDefs ) {

    if( cmdDefs != null && cmdDefs.isArray() ) {
      for( int i = 0; i < cmdDefs.arrayLength(); i++ ) {
        IAvTree cmdDef = cmdDefs.arrayElement( i );

        ICommandExec commandExec = createCommandExec( cmdDef );
        commandExec.config( cmdDef );
        cmdExecsConfigured.add( commandExec );

        DataObjName dataObjName = new DataObjName( cmdDef.fields().getStr( CLASS_ID ),
            cmdDef.fields().getStr( OBJ_NAME ), cmdDef.fields().getStr( CMD_ID ) );

        dataObjNemas.add( dataObjName );

        IStringMapEdit<TagInfo> tagsConfig = new StringMap<>();
        // если есть несколько тегов
        if( cmdDef.nodes().hasKey( COMMAND_TAGS_ARRAY ) ) {
          IAvTree tagsTree = cmdDef.nodes().getByKey( COMMAND_TAGS_ARRAY );

          for( int j = 0; j < tagsTree.arrayLength(); j++ ) {
            IAvTree tagParamsTree = tagsTree.arrayElement( j );
            try {
              TagInfo tagConf = createTagConfig( tagParamsTree, cmdDef );

              tagsConfig.put( tagParamsTree.structId(), tagConf );
            }
            catch( TsIllegalArgumentRtException e ) {
              throw new TsIllegalArgumentRtException( e, ERR_MSG_DURING_CONFIG_COMMAND_EXECUTER_FORMAT,
                  cmdDef.structId() );
            }

          }
        }
        // если один тег - использовать корневой
        else {
          TagInfo tagConf = createTagConfig( cmdDef, cmdDef );

          tagsConfig.put( DEFAULT_TAG_ID, tagConf );
        }

        tagInfoes.add( tagsConfig );

      }
    }

  }

  private TagInfo createTagConfig( IAvTree aTagParams, IAvTree aDefaultTagParams ) {
    TagInfo result = new TagInfo( getConfigParamField( TAG_DEVICE_ID, aTagParams, aDefaultTagParams, null ),
        getConfigParamField( TAG_ID, aTagParams, aDefaultTagParams, null ) );

    return result;
  }

  String getConfigParamField( String aFieldName, IAvTree aParams, IAvTree aDefaultParams, String aDefault ) {
    if( aParams.fields().hasValue( aFieldName ) ) {
      return aParams.fields().getStr( aFieldName );
    }

    if( aDefaultParams.fields().hasValue( aFieldName ) ) {
      return aDefaultParams.fields().getStr( aFieldName );
    }

    if( aDefault != null ) {
      return aDefault;
    }

    throw new TsIllegalArgumentRtException( ERR_MSG_FIELD_IS_NOT_PRESENTED_IN_CFG_FILE_FORMAT, aFieldName );

  }

  /**
   * Создаёт объект - исполнитель команды по конфигурационной информации
   *
   * @param aConfig IAvTree - конфигурационная информация
   * @return ICommandExec -
   */
  @SuppressWarnings( "unchecked" )
  private static ICommandExec createCommandExec( IAvTree aConfig ) {
    // тип передатчика - из конфигурации
    String commandExecClassStr = aConfig.fields().getStr( COMMAND_EXEC_JAVA_CLASS );

    try {
      Class<ICommandExec> commandExecClass = (Class<ICommandExec>)Class.forName( commandExecClassStr );

      ICommandExec exec = commandExecClass.getDeclaredConstructor().newInstance();

      return exec;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_COMMAND_EXEC_FORMAT,
          aConfig.structId() );
    }

  }

  static class TagInfo {

    private String deviceId;

    private String tagId;

    public TagInfo( String aDeviceId, String aTagId ) {
      super();
      deviceId = aDeviceId;
      tagId = aTagId;
    }

    public String getDeviceId() {
      return deviceId;
    }

    public void setDeviceId( String aDeviceId ) {
      deviceId = aDeviceId;
    }

    public String getTagId() {
      return tagId;
    }

    public void setTagId( String aTagId ) {
      tagId = aTagId;
    }

  }

  /**
   * Класс, определяющий обрабатываемые команды класса, в котором объекты заданы именами.
   *
   * @author max
   */
  public static class ProcessedCommandsDefByObjNames {

    private String classId;

    private IStringList objNames;

    private IStringList cmdIds;

    /**
     * Контейнер для описания команд одного класса обрабатываемых данным модулем
     *
     * @param aClassId - id класса
     * @param aObjNames - strid объектов этого класса
     * @param aCmdIds - id команд
     */
    public ProcessedCommandsDefByObjNames( String aClassId, IStringList aObjNames, IStringList aCmdIds ) {
      super();
      classId = aClassId;
      objNames = aObjNames;
      cmdIds = aCmdIds;
    }

    /**
     * @return создает {@link Gwid} id команды
     */
    public IList<Gwid> convert() {
      IListEdit<Gwid> result = new ElemArrayList<>();
      // ILongListEdit objIdsList = new LongArrayList();
      for( String objName : objNames ) {
        for( String cmdId : cmdIds ) {
          result.add( Gwid.createCmd( classId, objName, cmdId ) );
        }
      }

      return result;
    }
  }

}

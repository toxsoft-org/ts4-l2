package org.toxsoft.l2.dlm.tags.submodules.commands2;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;
import static org.toxsoft.l2.dlm.tags.submodules.commands.IL2Resources.*;

import java.util.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.coll.derivative.Queue;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.dlms.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Класс работы с командами системы диспетчеризации, является исполнителем команд, регистрируемым в сервисе.
 *
 * @author max
 */
public class CommandsModule2
    extends AbstractTsCoopCompMultiUse
    implements ISkCommandExecutor {

  private static final String OPC_MODULE_ID = "commandsModule"; //$NON-NLS-1$

  /**
   * Журнал работы
   */
  private ILogger logger;

  /**
   * Контекст подгружаемых модулей.
   */
  private IL2SharedContext context;

  /**
   * Информация о модуле DLM
   */
  private DlmInfo dlmInfo;

  /**
   * Соединение с сервером.
   */
  ISkConnection connection;

  /**
   * Очередь команд, пришедших на обработку.
   */
  private IStringMapEdit<IQueue<IDtoCommand>> commandsQueues;

  /**
   * Исполнители команд.
   */
  private IListEdit<IDataGwidTranslator> commandsTranslators;

  //
  // ----------------------------------------
  // Конфигурационная информация

  /**
   * Определение команд из конфигурации
   */
  private IListEdit<ProcessedCommandsDefByObjNames> commandsDefByObjNames;

  /**
   * Сконфигурированные исполнители команд.
   */
  private IListEdit<CmdExecConfiguredInfo> cmdExecConfiguredInfoes;

  // private IComplexTagsContainer complexTagsContainer;

  /**
   * Конструктор по контексту.
   *
   * @param aContext {@link IDlmContext} - контекст подгружаемых модулей.
   * @param aDlmInfo IDlmInfo - информация о DLM
   * @param aInstanceId
   * @param aComplexTagsContainer IComplexTagsContainer - контейнер сложных тегов.
   */
  public CommandsModule2( IL2SharedContext aContext, DlmInfo aDlmInfo, String aInstanceId ) {// , IComplexTagsContainer
                                                                                             // aComplexTagsContainer
    // ) {
    context = aContext;
    dlmInfo = aDlmInfo;
    // complexTagsContainer = aComplexTagsContainer;

    logger = LoggerUtils.getLogger( this.getClass(), aDlmInfo.moduleId(), aInstanceId, OPC_MODULE_ID );
  }

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    // создание по конфигурации описаний для регистрации в сервисе
    IAvTree cmdClassDefs = (IAvTree)aArgs.get( CMD_CLASS_DEFS );
    commandsDefByObjNames = createCmdDefs( cmdClassDefs );

    // создание локальных исполнителей команд, непосредственной выполняющих установку значений в устройство
    IAvTree cmdDefs = (IAvTree)aArgs.get( CMD_DEFS );
    cmdExecConfiguredInfoes = configCommandExecs( cmdDefs );

    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doStart() {
    boolean isConfigured = true;

    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured, ERR_MSG_COMMAND_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    connection = context.net().getSkConnection();
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    // Опеределения для регистрации исполнителя
    GwidList convertedCommandsDef = new GwidList();

    // обращение к серверу с целью конвертации имён в коды
    for( ProcessedCommandsDefByObjNames cmdDefByObjName : commandsDefByObjNames ) {
      IList<Gwid> cmdDef = cmdDefByObjName.convert();
      convertedCommandsDef.addAll( cmdDef );
    }

    for( Gwid gd : convertedCommandsDef ) {
      logger.info( "*** Handler registered on command: %s", gd );
    }

    // регистраци модуля в качестве исполнителя команд
    connection.coreApi().cmdService().registerExecutor( this, convertedCommandsDef );
    commandsDefByObjNames.clear();

    // запуск локальных исполнителей
    for( CmdExecConfiguredInfo cmdExecConfiguredInfo : cmdExecConfiguredInfoes ) {
      IDataGwidTranslator cExec = cmdExecConfiguredInfo.getCmdExecCfg();
      IList<TagInfo> execTagsInfoes = cmdExecConfiguredInfo.getTagsCfg();

      IStringMapEdit<IL2Tag> execTags = new StringMap<>();

      for( TagInfo tc : execTagsInfoes ) {
        IL2Tag tag = null;
        if( tc.isComplex() ) {
          // tag = complexTagsContainer.getComplexTagById( tc.tagId );
        }
        else {
          // ITsOpc tagsDevice = (ITsOpc)context.hal().listSpecificDevices().getByKey( tc.getDeviceId() );
          // tag = tagsDevice.tag( tc.getTagId() );
          tag = context.hal().tags().getByKey( tc.getTagId() );
        }

        if( tag == null ) {
          logger.error( "Tag '%s' not found", tc.getTagId() );
        }
        else {
          execTags.put( tc.getTagId(), tag );
        }
      }

      if( execTagsInfoes.size() != execTags.size() ) {
        continue;
      }

      GwidTranslatorCfgExtension dataObjName = cmdExecConfiguredInfo.getCmdGwidCfg();
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

      IQueue<IDtoCommand> queue = new Queue<>(); // new SynchronizedQueueWrapper<>( new Queue<>() );
      commandsQueues.put( getTotalCommandId( cmdId, objId ), queue );

      List<IGwidValueGetter> getters = new ArrayList<>();
      // создание getters и setters для s5
      CmdGwidValueGetter cmdGwidValueGetter = new CmdGwidValueGetter( queue, dataObjName.getGwid() );
      getters.add( cmdGwidValueGetter );
      IStringList argIds = cmdExecConfiguredInfo.getCmdArgsCfg();
      for( String argId : argIds ) {
        CmdArgGwidValueGetter cmdArgGwidValueGetter =
            new CmdArgGwidValueGetter( queue, Gwid.createCmdArg( clsId, cmdId, argId ) );
        getters.add( cmdArgGwidValueGetter );
      }

      CmdStateGwidValueSetter cmdStateGwidValueSetter =
          new CmdStateGwidValueSetter( queue, connection.coreApi().cmdService(), dataObjName.getGwid() );

      cExec.start( new IGwidValueSetter[] { cmdStateGwidValueSetter }, getters.toArray( new IGwidValueGetter[0] ),
          execTags.values() );

      commandsTranslators.add( cExec );
    }

    // очистка конфигурационного набора
    cmdExecConfiguredInfoes.clear();

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
    long time = System.currentTimeMillis();
    // совершение работы всеми локальными исполнителями
    for( IDataGwidTranslator translator : commandsTranslators ) {
      translator.translate( time );
    }
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

  @Override
  protected boolean doQueryStop() {
    connection.coreApi().cmdService().unregisterExecutor( this );
    return true;
  }

  @Override
  public void executeCommand( IDtoCommand aCmd ) {
    String cmdId = aCmd.cmdGwid().propId();
    String objId = aCmd.cmdGwid().strid();

    String totalCmdId = getTotalCommandId( cmdId, objId );

    // поместить команду в очередь для специфического (объект-тип команды) обработчика
    if( commandsQueues.hasKey( totalCmdId ) ) {
      commandsQueues.getByKey( totalCmdId ).putTail( aCmd );
      changeCommandState( aCmd.instanceId(),
          new SkCommandState( System.currentTimeMillis(), ESkCommandState.EXECUTING ) );
    }

    logger.debug( "Get command %s, cmdGwid:%s, and put into queue ", aCmd.instanceId(), //$NON-NLS-1$
        aCmd.cmdGwid().canonicalString() );
  }

  /**
   * Создаёт по конфигурации локальные исполнители - по одному исполнителю на команду.
   *
   * @param cmdDefs - {@link IAvTree} - конфигурационные данные.
   */
  private static IListEdit<CmdExecConfiguredInfo> configCommandExecs( IAvTree cmdDefs ) {
    IListEdit<CmdExecConfiguredInfo> result = new ElemArrayList<>();
    if( cmdDefs != null && cmdDefs.isArray() ) {
      for( int i = 0; i < cmdDefs.arrayLength(); i++ ) {
        IAvTree cmdDef = cmdDefs.arrayElement( i );

        IDataGwidTranslator commandExec = createCommandExec( cmdDef );
        commandExec.config( cmdDef );

        GwidTranslatorCfgExtension dataObjName = new GwidTranslatorCfgExtension( cmdDef.fields() );

        IListEdit<TagInfo> tagsConfig = new ElemArrayList<>();
        // если есть несколько тегов
        if( cmdDef.nodes().hasKey( COMMAND_TAGS_ARRAY ) ) {
          IAvTree tagsTree = cmdDef.nodes().getByKey( COMMAND_TAGS_ARRAY );

          for( int j = 0; j < tagsTree.arrayLength(); j++ ) {
            IAvTree tagParamsTree = tagsTree.arrayElement( j );
            try {
              TagInfo tagConf = createTagConfig( tagParamsTree, cmdDef );

              tagsConfig.add( tagConf );
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

          tagsConfig.add( tagConf );
        }

        IStringList args = new StringArrayList();
        CmdExecConfiguredInfo cmdExecConfiguredInfo =
            new CmdExecConfiguredInfo( commandExec, dataObjName, args, tagsConfig );
        result.add( cmdExecConfiguredInfo );
      }
    }
    return result;
  }

  private static TagInfo createTagConfig( IAvTree aTagParams, IAvTree aDefaultTagParams ) {
    if( aTagParams.fields().hasValue( COMPLEX_TAG_ID ) ) {
      return new TagInfo( aTagParams.fields().getStr( COMPLEX_TAG_ID ) );
    }
    TagInfo result = new TagInfo( getConfigParamField( TAG_DEVICE_ID, aTagParams, aDefaultTagParams, null ),
        getConfigParamField( TAG_ID, aTagParams, aDefaultTagParams, null ) );

    return result;
  }

  private static String getConfigParamField( String aFieldName, IAvTree aParams, IAvTree aDefaultParams,
      String aDefault ) {
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
  private static IDataGwidTranslator createCommandExec( IAvTree aConfig ) {
    // тип передатчика - из конфигурации
    String commandExecClassStr = aConfig.fields().getStr( COMMAND_EXEC_JAVA_CLASS );

    try {
      Class<IDataGwidTranslator> commandExecClass = (Class<IDataGwidTranslator>)Class.forName( commandExecClassStr );

      IDataGwidTranslator exec = commandExecClass.getDeclaredConstructor().newInstance();

      return exec;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_COMMAND_EXEC_FORMAT,
          aConfig.structId() );
    }

  }

  /**
   * Создаёт по конфигурации информацию, необходимую для регистрации объекта в сервисе команд в качестве исполнителя.
   *
   * @param cmdDefs - {@link IAvTree} - конфигурационные данные.
   * @return {@link IList} - список определяющий команды, классы, объекты исполнителя.
   */
  private static IListEdit<ProcessedCommandsDefByObjNames> createCmdDefs( IAvTree cmdDefs ) {

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

  /**
   * Создаёт по конфигурации опеределение команд-объектов для регистрации в качестве исполнителя.
   *
   * @param aClassCmdDefs - {@link IAvTree} - конфигурационные данные для одного класса.
   * @return ProcessedCommandsDefByObjNames - промежуточный объект определения команд-объектов для класса.
   * @throws TsItemNotFoundRtException
   * @throws TsUnsupportedFeatureRtException
   * @throws DvTypeCastRtException
   */
  private static ProcessedCommandsDefByObjNames creatClassCmdDefs( IAvTree aClassCmdDefs )
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

  static class CmdExecConfiguredInfo {

    private IDataGwidTranslator cmdExecCfg;

    private GwidTranslatorCfgExtension cmdGwidCfg;

    private IStringList cmdArgsCfg;

    private IList<TagInfo> tagsCfg;

    public CmdExecConfiguredInfo( IDataGwidTranslator aCmdExecCfg, GwidTranslatorCfgExtension aCmdGwidCfg,
        IStringList aCmdArgsCfg, IList<TagInfo> aTagsCfg ) {
      cmdExecCfg = aCmdExecCfg;
      cmdGwidCfg = aCmdGwidCfg;
      cmdArgsCfg = aCmdArgsCfg;
      tagsCfg = aTagsCfg;
    }

    public IDataGwidTranslator getCmdExecCfg() {
      return cmdExecCfg;
    }

    public GwidTranslatorCfgExtension getCmdGwidCfg() {
      return cmdGwidCfg;
    }

    public IStringList getCmdArgsCfg() {
      return cmdArgsCfg;
    }

    public IList<TagInfo> getTagsCfg() {
      return tagsCfg;
    }

  }

  static class TagInfo {

    private String deviceId;

    private String tagId;

    private boolean isComplex;

    /**
     * Конструктор описания комплексного тега
     *
     * @param aComplexTagId String - ид комплексного тега
     */
    public TagInfo( String aComplexTagId ) {
      tagId = aComplexTagId;
      isComplex = true;
    }

    public TagInfo( String aDeviceId, String aTagId ) {
      deviceId = aDeviceId;
      tagId = aTagId;
      isComplex = false;
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

    public boolean isComplex() {
      return isComplex;
    }

    public void setComplex( boolean aIsComplex ) {
      isComplex = aIsComplex;
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

package ru.toxsoft.l2.utils.opc.ods2s5scripts;

import java.io.*;
import java.util.*;

import org.jopendocument.dom.spreadsheet.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.ETagValueType;

/**
 * Читатель описаний классов из файла *.ods
 *
 * @author Dima
 */
@SuppressWarnings( "nls" )
public class OdsS5ClassReader {

  private static final int CLASS_COLUMN = 1;
  private static final int TYPE_COLUMN = 9;
  private static final int DESCR_COLUMN = 11;
  private static final int SYNC_COLUMN = 12;
  private static final int DATA_ID_COLUMN = 13;
  private static final int EVENT_ID_COLUMN = 14;
  private static final int CMD_ID_COLUMN = 17;

  private static final String POINT_PREFIX = "$"; //$NON-NLS-1$
  private static final String CREATE_CLASS = "createClass -classId "; //$NON-NLS-1$
  private static final String parent_id_etc = " -parentId S5Object -name \"описание класса\""; //$NON-NLS-1$
  private static final String EQU = " = "; //$NON-NLS-1$
  private static final String COMMA = ", "; //$NON-NLS-1$
  private static final String addData = "addData -id "; //$NON-NLS-1$
  private static final String _type = " -type "; //$NON-NLS-1$
  private static final String _currdata = " -currdata "; //$NON-NLS-1$
  private static final String _histdata = " -histdata "; //$NON-NLS-1$
  private static final String _syncdata = " -syncdata true -syncdataDelta 1000 ";//$NON-NLS-1$
  private static final String _name = " -n "; //$NON-NLS-1$
  private static final String _description = " -d "; //$NON-NLS-1$
  private static final String addEvent = "addEvent       -id "; //$NON-NLS-1$
  private static final String _h = " -h "; //$NON-NLS-1$
  private static final String addEventParam = "addEventParam  -ev "; //$NON-NLS-1$
  private static final String _ev_id_on = " -id on"; //$NON-NLS-1$
  private static final String _ev_id_oldVal = " -id oldVal"; //$NON-NLS-1$
  private static final String _ev_id_newVal = " -id newVal"; //$NON-NLS-1$
  private static final String addCmd = "addCmd     -id "; //$NON-NLS-1$
  private static final String addCmdArg = "addCmdArg  -c "; //$NON-NLS-1$
  private static final String _cmd_id = " -id value"; //$NON-NLS-1$

  private static final String COMMENT = "# "; //$NON-NLS-1$
  private static final String CD_DEV_CLS = "cd dev.classes"; //$NON-NLS-1$
  private static final String QUOTE = "\""; //$NON-NLS-1$

  private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
  private static final Object DOT_CLI = ".cli"; //$NON-NLS-1$
  private static final String saveChanges = "saveChanges"; //$NON-NLS-1$
  private static final String CREATE = "create"; //$NON-NLS-1$

  // имя закладки с описанием классов
  private static final String CLASS_TAB_NAME = "Классы";

  // Создатель файла скрипта
  private static PrintWriter scriptWriter;

  // страница с описанием классов
  private static Sheet classSheet;

  private Map<String, ClassDescription> classesMap = new HashMap<>();

  // Базовая структура для описания одной сущности (data, command, event)
  protected static class BaseItem {

    String className;
    String name;
    ETagValueType type;
    String description;

    BaseItem( String aClassName, String aName, ETagValueType aType, String aDescription ) {
      className = aClassName;
      name = aName;
      type = aType;
      description = aDescription;
    }
  }

  // описание одного данного
  private static class DataItem
      extends BaseItem {

    Boolean isSync;

    public DataItem( String aClassName, String aName, ETagValueType aType, Boolean aSync, String aDescription ) {
      super( aClassName, aName, aType, aDescription );
      isSync = aSync;
    }
  }

  // описание одного события
  private static class EventItem
      extends BaseItem {

    public EventItem( String aClassName, String aName, ETagValueType aType, String aDescription ) {
      super( aClassName, aName, aType, aDescription );
    }
  }

  // описание одной команды
  private static class CmdItem
      extends BaseItem {

    public CmdItem( String aClassName, String aName, ETagValueType aType, String aDescription ) {
      super( aClassName, aName, aType, aDescription );
    }
  }

  // Вспомогательный класс, хранит описания данных, событий и команды
  private static class ClassDescription {

    String className;
    // описание данных
    List<DataItem> dataItems = new ArrayList<>();
    // описание событий
    List<EventItem> eventItems = new ArrayList<>();
    // описание команд
    List<CmdItem> cmdItems = new ArrayList<>();

    public ClassDescription( String aClassName ) {
      className = aClassName;
    }
  }

  /**
   * Считывает описание классов
   */
  public void scanClassTab() {
    classesMap.clear();
    try {
      // Сканируем таблицу
      for( int row = 3; row < classSheet.getRowCount(); row++ ) {
        parseDataItem( row );
        parseEventItem( row );
        parseCmdItem( row );
      }
      // Таблицу отсканировали, теперь создаем файлы классов
      for( String className : classesMap.keySet() ) {
        // Получаем имя для файла скрипта
        String classScriptName = getScriptName( className );
        // Создаем файл скрипта
        scriptWriter = new PrintWriter( classScriptName, UTF_8 );
        scriptWriter.println();
        // сразу настроим на нужную директорию
        scriptWriter.println( CD_DEV_CLS );
        scriptWriter.println();
        // генерим строку создания класса
        scriptWriter.println( getClassScript( className ) );
        scriptWriter.println();
        // генерим описание данных
        for( DataItem di : classesMap.get( className ).dataItems ) {
          // генерим строку создания данного
          scriptWriter.println( getDataScript( di ) );
        }
        scriptWriter.println();
        // генерим описание событий
        for( EventItem evi : classesMap.get( className ).eventItems ) {
          // генерим строку создания данного
          scriptWriter.println( getEventScript( evi ) );
        }
        scriptWriter.println();
        // генерим описание команд
        for( CmdItem cmdi : classesMap.get( className ).cmdItems ) {
          // генерим строку создания данного
          scriptWriter.println( getCommandScript( cmdi ) );
        }
        scriptWriter.println();
        scriptWriter.println( saveChanges );

        scriptWriter.close();
      }
      System.out.println( "Done!" );
    }
    catch( IOException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Считывает и разбирает описание одной команды
   *
   * @param aRow номер ряда
   */
  private void parseCmdItem( int aRow ) {
    // Читаем
    CmdItem cmdItem = readCmdItem( aRow );
    if( cmdItem != null ) {
      // Заносим в контейнер описания класса новое описание команды
      if( classesMap.containsKey( cmdItem.className ) ) {
        ClassDescription cd = classesMap.get( cmdItem.className );
        cd.cmdItems.add( cmdItem );
      }
      else {
        ClassDescription cd = new ClassDescription( cmdItem.className );
        cd.cmdItems.add( cmdItem );
        classesMap.put( cmdItem.className, cd );
      }
    }
  }

  /**
   * Считывает и разбирает описание одного данного
   *
   * @param aRow номер ряда
   */
  private void parseDataItem( int aRow ) {
    // Читаем данное
    DataItem dataItem = readDataItem( aRow );
    if( dataItem != null ) {
      // Заносим в контейнер описания класса новое описание данного
      if( classesMap.containsKey( dataItem.className ) ) {
        ClassDescription cd = classesMap.get( dataItem.className );
        cd.dataItems.add( dataItem );
      }
      else {
        ClassDescription cd = new ClassDescription( dataItem.className );
        cd.dataItems.add( dataItem );
        classesMap.put( dataItem.className, cd );
      }
    }
  }

  /**
   * Считывает и разбирает описание одного события
   *
   * @param aRow номер ряда
   */
  private void parseEventItem( int aRow ) {
    // Читаем событие
    EventItem eventItem = readEventItem( aRow );
    if( eventItem != null ) {
      // Заносим в контейнер описания класса новое описание данного
      if( classesMap.containsKey( eventItem.className ) ) {
        ClassDescription cd = classesMap.get( eventItem.className );
        cd.eventItems.add( eventItem );
      }
      else {
        ClassDescription cd = new ClassDescription( eventItem.className );
        cd.eventItems.add( eventItem );
        classesMap.put( eventItem.className, cd );
      }
    }
  }

  /**
   * Строка создания события
   *
   * @param aEventItem описание события
   * @return строка скрипт создания события
   */
  private static String getEventScript( EventItem aEventItem ) {
    if( aEventItem.type == ETagValueType.BOOLEAN ) {
      return addEvent + aEventItem.name + _h + _name + QUOTE + aEventItem.description + QUOTE + "\n" + addEventParam
          + aEventItem.name + _ev_id_on + _type + aEventItem.type.getName();
    }
    // Для Integer & Float
    return addEvent + aEventItem.name + _h + _name + QUOTE + aEventItem.description + QUOTE + "\n" + addEventParam
        + aEventItem.name + _ev_id_oldVal + _type + aEventItem.type.getName() + "\n" + addEventParam + aEventItem.name
        + _ev_id_newVal + _type + aEventItem.type.getName();
  }

  /**
   * Строка создания команды
   *
   * @param aCmdItem описание команды
   * @return строка скрипт создания команды
   */
  private static String getCommandScript( CmdItem aCmdItem ) {
    return addCmd + aCmdItem.name + _name + QUOTE + aCmdItem.description + QUOTE + _description + QUOTE
        + aCmdItem.description + QUOTE + "\n" + addCmdArg + aCmdItem.name + _cmd_id + _type + aCmdItem.type.getName();
  }

  /**
   * Строка создания данного
   *
   * @param aDataItem описание данного
   * @return строка скрипт создания класса
   */
  private static String getDataScript( DataItem aDataItem ) {
    if( aDataItem.isSync.booleanValue() ) {
      return addData + aDataItem.name + _type + aDataItem.type.getName() + _currdata + _histdata + _syncdata + _name
          + QUOTE + aDataItem.description + QUOTE + _description + QUOTE + aDataItem.description + QUOTE;
    }
    return addData + aDataItem.name + _type + aDataItem.type.getName() + _currdata + _histdata + _name + QUOTE
        + aDataItem.description + QUOTE + _description + QUOTE + aDataItem.description + QUOTE;
  }

  /**
   * Строка создания класса
   *
   * @param aClassName название класса
   * @return скрипт создания класса
   */
  private static String getClassScript( String aClassName ) {
    String retVal = CREATE_CLASS + aClassName + parent_id_etc;
    return retVal;
  }

  private static String getScriptName( String aClassName ) {
    // Собираем имя по частям
    String filePrefix = CREATE;
    // находим точку и вырезаем имя после нее
    String className = aClassName.substring( aClassName.indexOf( "." ) + 1 );
    StringBuffer retVal = new StringBuffer();
    retVal.append( filePrefix );
    retVal.append( className );
    retVal.append( DOT_CLI );
    return retVal.toString();
  }

  /**
   * Считывает описание команды
   *
   * @param aRow - ряд считывания
   * @return описание команды
   */
  private static CmdItem readCmdItem( int aRow ) {
    MutableCell<?> classCell, typeCell, descrCell, cmdIdCell;
    // ситываем колонку cmdId
    cmdIdCell = classSheet.getCellAt( CMD_ID_COLUMN, aRow );
    if( cmdIdCell.isEmpty() ) {
      // нет описания
      return null;
    }
    // читаем класс
    classCell = classSheet.getCellAt( CLASS_COLUMN, aRow );
    String className = classCell.getValue().toString();
    // читаем тип
    typeCell = classSheet.getCellAt( TYPE_COLUMN, aRow );
    String typeStr = typeCell.getValue().toString();
    ETagValueType type = ETagValueType.BOOLEAN;
    // Ищем тип среди допустимых
    if( typeStr.indexOf( "Int" ) >= 0 ) {
      type = ETagValueType.INTEGER;
    }
    else {
      if( typeStr.indexOf( "Float" ) >= 0 ) {
        type = ETagValueType.FLOAT;
      }
    }
    // читаем описание
    descrCell = classSheet.getCellAt( DESCR_COLUMN, aRow );
    String descr = descrCell.getValue().toString();
    // читаем cmdId
    cmdIdCell = classSheet.getCellAt( CMD_ID_COLUMN, aRow );
    String cmdId = cmdIdCell.getValue().toString();
    // Создаем DataItem
    CmdItem retVal = new CmdItem( className, cmdId, type, descr );
    return retVal;
  }

  /**
   * Считывает описание данного
   *
   * @param aRow - ряд считывания
   * @return описание данного
   */
  private static DataItem readDataItem( int aRow ) {
    MutableCell<?> classCell, typeCell, descrCell, synchCell, dataIdCell;
    // ситываем колонку dataId
    dataIdCell = classSheet.getCellAt( DATA_ID_COLUMN, aRow );
    if( dataIdCell.isEmpty() ) {
      // нет описания данного
      return null;
    }
    // читаем класс
    classCell = classSheet.getCellAt( CLASS_COLUMN, aRow );
    String className = classCell.getValue().toString();
    // читаем тип
    typeCell = classSheet.getCellAt( TYPE_COLUMN, aRow );
    String typeStr = typeCell.getValue().toString();
    ETagValueType type = ETagValueType.BOOLEAN;
    // Ищем тип среди допустимых
    if( typeStr.indexOf( "Int" ) >= 0 ) {
      type = ETagValueType.INTEGER;
    }
    else {
      if( typeStr.indexOf( "Float" ) >= 0 ) {
        type = ETagValueType.FLOAT;
      }
    }
    // читаем описание
    descrCell = classSheet.getCellAt( DESCR_COLUMN, aRow );
    String descr = descrCell.getValue().toString();
    // читаем sync/async
    synchCell = classSheet.getCellAt( SYNC_COLUMN, aRow );
    String sync = synchCell.getValue().toString();
    boolean syncFlag = false;
    if( sync.compareTo( "sync" ) == 0 ) {
      syncFlag = true;
    }
    // читаем dataId
    dataIdCell = classSheet.getCellAt( DATA_ID_COLUMN, aRow );
    String dataId = dataIdCell.getValue().toString();
    // Создаем DataItem
    DataItem retVal = new DataItem( className, dataId, type, Boolean.valueOf( syncFlag ), descr );
    return retVal;
  }

  /**
   * Считывает описание события
   *
   * @param aRow - ряд считывания
   * @return описание события
   */
  private static EventItem readEventItem( int aRow ) {
    MutableCell<?> classCell, typeCell, descrCell, eventIdCell;
    // ситываем колонку dataId
    eventIdCell = classSheet.getCellAt( EVENT_ID_COLUMN, aRow );
    if( eventIdCell.isEmpty() ) {
      // нет описания
      return null;
    }
    // читаем класс
    classCell = classSheet.getCellAt( CLASS_COLUMN, aRow );
    String className = classCell.getValue().toString();
    // читаем тип
    typeCell = classSheet.getCellAt( TYPE_COLUMN, aRow );
    String typeStr = typeCell.getValue().toString();
    ETagValueType type = ETagValueType.BOOLEAN;
    // Ищем тип среди допустимых
    if( typeStr.indexOf( "Int" ) >= 0 ) {
      type = ETagValueType.INTEGER;
    }
    else {
      if( typeStr.indexOf( "Float" ) >= 0 ) {
        type = ETagValueType.FLOAT;
      }
    }
    // читаем описание
    descrCell = classSheet.getCellAt( DESCR_COLUMN, aRow );
    String descr = descrCell.getValue().toString();
    // читаем eveId
    eventIdCell = classSheet.getCellAt( EVENT_ID_COLUMN, aRow );
    String eventId = eventIdCell.getValue().toString();
    // Создаем DataItem
    EventItem retVal = new EventItem( className, eventId, type, descr );
    return retVal;
  }

  /**
   * @param args -sum - летнее расписание, -sub - расписание для линии Сабутарло
   */
  public static void main( String[] args ) {

    // Открываем файл с описанием классов
    File odsFile = new File( args[0] );
    try {
      classSheet = SpreadSheet.createFromFile( odsFile ).getSheet( CLASS_TAB_NAME );
      OdsS5ClassReader classODSReader = new OdsS5ClassReader();
      // Считываем описания классов
      classODSReader.scanClassTab();
    }
    catch( IOException e ) {
      e.printStackTrace();
    }
  }
}

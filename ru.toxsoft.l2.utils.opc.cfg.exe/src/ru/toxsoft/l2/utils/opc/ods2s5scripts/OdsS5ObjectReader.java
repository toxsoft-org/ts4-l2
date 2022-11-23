package ru.toxsoft.l2.utils.opc.ods2s5scripts;

import java.io.*;
import java.util.*;

import org.jopendocument.dom.spreadsheet.*;

/**
 * Читатель описаний объектов из файла *.ods
 *
 * @author Dima
 */
@SuppressWarnings( "nls" )
public class OdsS5ObjectReader {

  private static final int CLASS_COLUMN = 13;
  private static final int OBJ_NAME_COLUMN = 14;

  private static final String POINT_PREFIX = "$"; //$NON-NLS-1$
  private static final String createObject = "createObject "; //$NON-NLS-1$
  private static final String EQU = " = "; //$NON-NLS-1$
  private static final String COMMA = ", "; //$NON-NLS-1$

  private static final String COMMENT = "# "; //$NON-NLS-1$
  private static final String CD_DEV_OBJS = "cd dev.objects"; //$NON-NLS-1$
  private static final String QUOTE = "\""; //$NON-NLS-1$

  private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
  private static final Object DOT_CLI = ".cli"; //$NON-NLS-1$
  private static final String saveChanges = "saveChanges"; //$NON-NLS-1$
  private static final String CREATE_OBJ = "createObjects"; //$NON-NLS-1$

  // имя закладки с описанием объектов
  private static final String OBJ_TAB_NAME = "Объекты";

  // Создатель файла скрипта
  private static PrintWriter scriptWriter;

  // страница с описанием объектов
  private static Sheet objSheet;

  private Map<String, List<String>> classesMap = new HashMap<>();

  // Структура для описания одного объекта
  protected static class ObjectItem {

    String className;
    String objName;

    ObjectItem( String aClassName, String aObjName ) {
      className = aClassName;
      objName = aObjName;
    }
  }

  /**
   * Считывает описание Объектов
   */
  public void scanObjectTab() {
    classesMap.clear();
    try {
      // Сканируем таблицу
      for( int row = 3; row < objSheet.getRowCount(); row++ ) {
        parseRow( row );
      }
      // Таблицу отсканировали, теперь создаем файлы классов
      // Получаем имя для файла скрипта
      String objScriptName = getScriptName();
      // Создаем файл скрипта
      scriptWriter = new PrintWriter( objScriptName, UTF_8 );
      scriptWriter.println();
      // сразу настроим на нужную директорию
      scriptWriter.println( CD_DEV_OBJS );
      scriptWriter.println();
      for( String className : classesMap.keySet() ) {
        // генерим описание данных
        List<String> objNames = classesMap.get( className );
        for( String objName : objNames ) {
          // генерим строку создания данного
          scriptWriter.println( getObjectScript( className, objName ) );
          scriptWriter.println();
        }
      }
      scriptWriter.println();
      scriptWriter.println( saveChanges );

      scriptWriter.close();
      System.out.println( "Done!" );
    }
    catch(

    IOException e ) {
      e.printStackTrace();
    }
  }

  /**
   * Считывает и разбирает описание одного ряда
   *
   * @param aRow номер ряда
   */
  private void parseRow( int aRow ) {
    // Читаем
    ObjectItem objItem = readObject( aRow );
    if( objItem != null ) {
      // Заносим в контейнер новый объект
      if( classesMap.containsKey( objItem.className ) ) {
        List<String> objList = classesMap.get( objItem.className );
        if( objList.indexOf( objItem.objName ) < 0 ) {
          objList.add( objItem.objName );
        }
      }
      else {
        List<String> objList = new ArrayList<>();
        objList.add( objItem.objName );
        classesMap.put( objItem.className, objList );
      }
    }
  }

  /**
   * Строка создания объекта
   *
   * @param aClassName класс
   * @param aObjectName объект
   * @return скрипт создания объекта
   */
  private static String getObjectScript( String aClassName, String aObjectName ) {
    String retVal = POINT_PREFIX + aObjectName + EQU + createObject + aClassName + "  " + aObjectName;
    return retVal;
  }

  private static String getScriptName() {
    // Собираем имя по частям
    String fileName = CREATE_OBJ;
    StringBuffer retVal = new StringBuffer();
    retVal.append( fileName );
    retVal.append( DOT_CLI );
    return retVal.toString();
  }

  /**
   * Считывает описание команды
   *
   * @param aRow - ряд считывания
   * @return описание команды
   */
  private static ObjectItem readObject( int aRow ) {
    MutableCell<?> classCell, objCell;
    // ситываем колонку cmdId
    objCell = objSheet.getCellAt( OBJ_NAME_COLUMN, aRow );
    if( objCell.isEmpty() ) {
      // нет описания
      return null;
    }
    if( objCell.getValue().toString().indexOf( "-" ) >= 0 ) {
      // нет описания
      return null;
    }
    // читаем класс
    classCell = objSheet.getCellAt( CLASS_COLUMN, aRow );
    String className = classCell.getValue().toString();
    // читаем название объекта
    objCell = objSheet.getCellAt( OBJ_NAME_COLUMN, aRow );
    String objName = objCell.getValue().toString();
    // Создаем ObjectItem
    ObjectItem retVal = new ObjectItem( className, objName );
    return retVal;
  }

  /**
   * @param args -sum - летнее расписание, -sub - расписание для линии Сабутарло
   */
  public static void main( String[] args ) {

    // Открываем файл с описанием классов
    File odsFile = new File( args[0] );
    try {
      objSheet = SpreadSheet.createFromFile( odsFile ).getSheet( OBJ_TAB_NAME );
      OdsS5ObjectReader classODSReader = new OdsS5ObjectReader();
      // Считываем описания классов
      classODSReader.scanObjectTab();
    }
    catch( IOException e ) {
      e.printStackTrace();
    }
  }
}

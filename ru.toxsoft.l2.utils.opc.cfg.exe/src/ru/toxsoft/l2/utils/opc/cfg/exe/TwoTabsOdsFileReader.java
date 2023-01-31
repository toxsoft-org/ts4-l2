package ru.toxsoft.l2.utils.opc.cfg.exe;

import static ru.toxsoft.l2.utils.opc.cfg.exe.ISysClassRowDef.*;

import java.io.*;
import java.util.*;

import org.jopendocument.dom.spreadsheet.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.OdsFileReader.*;
import ru.toxsoft.l2.utils.opc.cfg.exe.ods.*;

/**
 * Утилитный класс чтения ODS описания пинов, событий, команд для формирования конфигурационных файлов НУ.
 *
 * @author max
 */
public class TwoTabsOdsFileReader
    extends CommonOdsFileReader {

  private final static String SHEET_NAME = "Объекты";

  private final static StringFieldValueGetter TAG_NAME_COLUMN = new StringFieldValueGetter( 2 );

  static StringFieldValueGetter TAG_FULL_NAME_COLUMN = new StringFieldValueGetter( 6 );

  private final static StringFieldValueGetter CLASS_ID_COLUMN = new StringFieldValueGetter( 13 );

  private final static StringFieldValueGetter OBJ_NAME_COLUMN = new StringFieldValueGetter( 14 );

  private final static IntegerFieldValueGetter IS_PROCCESS_COLUMN = new IntegerFieldValueGetter( 17 );

  private ClassTabsOdsFileReader classTabsOdsFileReader;

  private List<StringData> rows = new ArrayList<>();

  public TwoTabsOdsFileReader( File odsFile ) {
    super( odsFile, SHEET_NAME );

    classTabsOdsFileReader = new ClassTabsOdsFileReader( odsFile );
    try {
      classTabsOdsFileReader.read();
    }
    catch( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  protected void readRow( Sheet aSheet, int aRowNumber ) {

    String tagName = TAG_NAME_COLUMN.getValue( aSheet, aRowNumber );
    String tagFullName = TAG_FULL_NAME_COLUMN.getValue( aSheet, aRowNumber );
    String classId = CLASS_ID_COLUMN.getValue( aSheet, aRowNumber );
    String objName = OBJ_NAME_COLUMN.getValue( aSheet, aRowNumber );

    Integer isProccess = Integer.valueOf( 0 );
    try {
      isProccess = IS_PROCCESS_COLUMN.getValue( aSheet, aRowNumber );
    }
    catch( Exception e ) {
      System.out.println( "Row number = " + aRowNumber );
      e.printStackTrace();
    }

    if( isProccess.intValue() != 1 || tagName.length() == 0 || tagFullName.length() == 0 || classId.length() == 0
        || objName.length() == 0 || objName.startsWith( "-" ) ) {
      return;
    }

    List<IOptionSet> classRows = classTabsOdsFileReader.getRows( classId, tagName );

    if( classRows.size() == 0 ) {
      return;
    }

    System.out.println( "Classes presents = " + aRowNumber + " , " + classId + " , " + tagName );

    for( IOptionSet classRow : classRows ) {

      StringData newStringData = new StringData();

      newStringData.tagName = tagName;

      newStringData.tagFullName = tagFullName;

      newStringData.tagValueType = ETagValueType.searchTypeByName( VAL_TYPE_PARAM.getValue( classRow ).asString() );

      newStringData.classId = classId;

      newStringData.classTagId = CLASS_TAG_ID_PARAM.getValue( classRow ).asString();

      newStringData.objName = objName;

      newStringData.pinId = getPinId( newStringData.tagFullName );

      IAtomicValue synchType = SYNCH_TYPE_PARAM.getValue( classRow );
      newStringData.tagType =
          (synchType == IAtomicValue.NULL) ? null : ETagSynchType.searchTypeByName( synchType.asString() );

      newStringData.isOutput = IS_WRITE_PARAM.getValue( classRow ).asBool();

      newStringData.dataId = DATA_ID_PARAM.getValue( classRow ).asString();

      newStringData.cmdId = COMMAND_ID_PARAM.getValue( classRow ).asString();

      newStringData.eventData = new EventStringData();

      newStringData.eventData.eventId = EVENT_ID_PARAM.getValue( classRow ).asString();

      newStringData.eventData.onTrigger = EVENT_ON_PARAM.getValue( classRow ).asBool();

      newStringData.eventData.offTrigger = EVENT_OFF_PARAM.getValue( classRow ).asBool();

      newStringData.eventData.triggerWordBitIndex = BIT_INDEX_PARAM.getValue( classRow ).asInt();

      newStringData.cmdWordBitIndex = BIT_INDEX_PARAM.getValue( classRow ).asInt();

      rows.add( newStringData );
    }
  }

  private String getPinId( String aTagName ) {
    String result = aTagName;
    result = result.replace( " ", "_" );
    result = result.replace( "-", "_" );
    result = result.replace( ".", "_" );
    result = result.replace( "(", "_" );
    result = result.replace( ")", "_" );
    result = result.replace( "=", "_" );
    result = result.replace( ";", "_" );
    result = result.replace( "\"", "" );
    return result;
  }

  /**
   * Читает ODS файл и для каждой его строки формирует объект с описанием тега (пина), событий, команд
   *
   * @param aFileName String - имя ODS-файла описания.
   * @return IListEdit - список объектов описаний тегов.
   * @throws IOException - ошибка чтения файла.
   */
  public static IListEdit<StringData> readSheet( String aFileName )
      throws IOException {
    TwoTabsOdsFileReader reader = new TwoTabsOdsFileReader( new File( aFileName ) );
    reader.read();

    return new ElemArrayList<>( reader.rows );
  }

  public static void main( String[] a ) {
    try {
      TwoTabsOdsFileReader reader = new TwoTabsOdsFileReader( new File( "17023 Сигналы Москокс 2018-04-11.ods" ) );
      reader.read();
      int i = 1;
      for( StringData cr : reader.rows ) {
        System.out.print( i + " | " );
        System.out.print( cr.classId + " | " );
        System.out.print( cr.tagName + " | " );
        System.out.print( cr.tagFullName + " | " );
        System.out.print( cr.pinId + " | " );
        System.out.print( cr.dataId + " | " );
        System.out.print( cr.cmdId + " | " );
        System.out.print( cr.eventData.eventId + " | " );
        System.out.print( cr.eventData.triggerWordBitIndex + " | " );
        // System.out.print( cr.eventOn + " | " );
        // System.out.print( cr.eventOff + " | " );

        System.out.println();

        i++;
      }

    }
    catch( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}

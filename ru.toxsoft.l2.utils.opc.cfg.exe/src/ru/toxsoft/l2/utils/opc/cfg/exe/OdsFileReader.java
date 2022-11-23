package ru.toxsoft.l2.utils.opc.cfg.exe;

import static ru.toxsoft.l2.utils.opc.cfg.exe.IOpcConvertorConstants.*;

import java.io.*;
import java.math.*;

import org.jopendocument.dom.spreadsheet.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;

/**
 * Утилитный класс чтения ODS описания пинов, событий, команд для формирования конфигурационных файлов НУ.
 *
 * @author max
 */
public class OdsFileReader {

  private static final String ODS_SHEET_NAME = "TC1";

  /**
   * Читает ODS файл и для каждой его строки формирует объект с описанием тега (пина), событий, команд
   *
   * @param aFileName String - имя ODS-файла описания.
   * @return IListEdit - список объектов описаний тегов.
   * @throws IOException - ошибка чтения файла.
   */
  public static IListEdit<StringData> readSheet( String aFileName )
      throws IOException {
    File file = new File( aFileName );
    Sheet sheet;

    // закладка с описанием сигналов ТС
    sheet = SpreadSheet.createFromFile( file ).getSheet( ODS_SHEET_NAME );

    IListEdit<StringData> result = new ElemArrayList<>();

    int nRowCount = Math.min( 100000, sheet.getRowCount() );

    if( nRowCount <= 1 ) {
      return result;
    }

    int emptyCount = 0;

    for( int nRowIndex = 1; nRowIndex < nRowCount; nRowIndex++ ) {

      // далее код для корректной остановки - количество строк иногда оказывается некорректным

      MutableCell<?> cell = sheet.getCellAt( TAG_FULL_NAME_COLUMN, nRowIndex );

      if( cell.isEmpty() || ((String)cell.getValue()).length() == 0 ) {
        emptyCount++;
      }
      else {
        emptyCount = 0;
      }

      result.add( new StringData( sheet, nRowIndex ) );

      if( emptyCount > 100 ) {
        break;
      }
    }

    return result;
  }

  /**
   * Считанные данные одной строки (тег, пин, событие, команда).
   *
   * @author max
   */
  public static class StringData {

    private static final String CMD_MASK_START_STR = "2^";

    String connection;

    String tagName;

    String tagFullName;

    ETagValueType tagValueType;

    String pinId;

    ETagSynchType tagType;

    boolean isOutput;

    String classId;

    String classTagId;

    String objName;

    String dataId;

    EventStringData eventData;

    String cmdId;

    int cmdWordBitIndex = -1;

    public StringData() {

    }

    public StringData( Sheet aSheet, int aRowNumber ) {

      MutableCell<?> cell = aSheet.getCellAt( CONNECTION_COLUMN, aRowNumber );
      connection = ((String)cell.getValue()).trim();

      cell = aSheet.getCellAt( TAG_NAME_COLUMN, aRowNumber );
      tagName = ((String)cell.getValue()).trim();

      cell = aSheet.getCellAt( TAG_FULL_NAME_COLUMN, aRowNumber );
      tagFullName = ((String)cell.getValue()).trim();

      cell = aSheet.getCellAt( TAG_VALUE_TYPE_COLUMN, aRowNumber );
      if( ((String)cell.getValue()).trim().length() == 0 ) {
        tagValueType = ETagValueType.BOOLEAN;
      }
      else {
        tagValueType = ETagValueType.searchTypeByName( ((String)cell.getValue()).trim() );
      }

      cell = aSheet.getCellAt( PIN_ID_COLUMN, aRowNumber );
      pinId = ((String)cell.getValue()).trim();

      cell = aSheet.getCellAt( TAG_SYNCH_TYPE_COLUMN, aRowNumber );
      if( ((String)cell.getValue()).trim().length() == 0 ) {
        // Dima, 10.02.16
        // По умолчанию асинхронный тег
        tagType = ETagSynchType.ASYNCH;
      }
      else {
        tagType = ETagSynchType.searchTypeByName( ((String)cell.getValue()).trim() );
      }

      cell = aSheet.getCellAt( CMD_ID_COLUMN, aRowNumber );
      cmdId = ((String)cell.getValue()).trim();

      isOutput = cmdId != null && cmdId.length() > 0;

      cell = aSheet.getCellAt( CLASS_ID_COLUMN, aRowNumber );
      classId = ((String)cell.getValue()).trim();

      cell = aSheet.getCellAt( OBJECT_NAME_COLUMN, aRowNumber );
      objName = ((String)cell.getValue()).trim();

      cell = aSheet.getCellAt( DATA_ID_COLUMN, aRowNumber );
      dataId = ((String)cell.getValue()).trim();

      eventData = new EventStringData( aSheet, aRowNumber );

      cell = aSheet.getCellAt( CMD_WORD_MASK_COLUMN, aRowNumber );
      if( cell.getValue() instanceof BigDecimal ) {
        cmdWordBitIndex = ((BigDecimal)cell.getValue()).intValue();
      }
      else
        if( cell.getValue() instanceof String && ((String)cell.getValue()).trim().length() > 0 ) {
          String strValue = ((String)cell.getValue()).trim();
          if( strValue.startsWith( CMD_MASK_START_STR ) ) {
            cmdWordBitIndex = Integer.parseInt( strValue.substring( 2 ).trim() );
          }
          else {
            cmdWordBitIndex = Integer.parseInt( strValue );
          }
        }
    }

    public String getConnection() {
      return connection;
    }

    public String getTagName() {
      return tagName;
    }

    public String getTagFullName() {
      return tagFullName;
    }

    public ETagValueType getTagValueType() {
      return tagValueType;
    }

    public String getPinId() {
      return pinId;
    }

    public ETagSynchType getTagSynchType() {
      return tagType;
    }

    public boolean isOutput() {
      return isOutput;
    }

    public String getClassTagId() {
      return classTagId;
    }

    public String getClassId() {
      return classId;
    }

    public String getObjName() {
      return objName;
    }

    public String getDataId() {
      return dataId;
    }

    public EventStringData getEventData() {
      return eventData;
    }

    public String getCmdId() {
      return cmdId;
    }

    public int getCmdWordBitIndex() {
      return cmdWordBitIndex;
    }

  }

  /**
   * Класс, содержжащий данные о событии.
   *
   * @author max
   */
  public static class EventStringData {

    private static final String EVENT_MASK_START_STR = "2^";

    private static final String ON_TRIGGER_STR = "yes";

    String eventId;

    boolean onTrigger;

    boolean offTrigger;

    int triggerWordBitIndex = -1;

    String paramTagName;

    String paramId;

    public synchronized void setEventId( String eventId ) {
      this.eventId = eventId;
    }

    public synchronized void setOnTrigger( boolean onTrigger ) {
      this.onTrigger = onTrigger;
    }

    public synchronized void setOffTrigger( boolean offTrigger ) {
      this.offTrigger = offTrigger;
    }

    public synchronized void setTriggerWordBitIndex( int triggerWordBitIndex ) {
      this.triggerWordBitIndex = triggerWordBitIndex;
    }

    public synchronized void setParamTagName( String paramTagName ) {
      this.paramTagName = paramTagName;
    }

    public synchronized void setParamId( String paramId ) {
      this.paramId = paramId;
    }

    public EventStringData() {

    }

    public EventStringData( Sheet aSheet, int aRowNumber ) {

      MutableCell<?> cell = aSheet.getCellAt( EVENT_ID_COLUMN, aRowNumber );
      eventId = ((String)cell.getValue()).trim();

      cell = aSheet.getCellAt( ON_TRIGGER_COLUMN, aRowNumber );
      String onTriggerSign = ((String)cell.getValue()).trim();
      onTrigger = onTriggerSign != null && onTriggerSign.trim().equals( ON_TRIGGER_STR );

      cell = aSheet.getCellAt( OFF_TRIGGER_COLUMN, aRowNumber );
      String offTriggerSign = ((String)cell.getValue()).trim();
      offTrigger = offTriggerSign != null && offTriggerSign.trim().equals( ON_TRIGGER_STR );

      cell = aSheet.getCellAt( TRIGGER_MASK_COLUMN, aRowNumber );
      if( cell.getValue() instanceof BigDecimal ) {
        triggerWordBitIndex = ((BigDecimal)cell.getValue()).intValue();
      }
      else
        if( cell.getValue() instanceof String && ((String)cell.getValue()).trim().length() > 0 ) {
          String strValue = ((String)cell.getValue()).trim();
          if( strValue.startsWith( EVENT_MASK_START_STR ) ) {
            triggerWordBitIndex = Integer.parseInt( strValue.substring( 2 ).trim() );
          }
          else {
            triggerWordBitIndex = Integer.parseInt( strValue );
          }
        }

      // if( triggerWordBitIndex >= 0 ) {
      // triggerWordBitIndex =
      // Integer.toBinaryString( triggerWordBitIndex ).length() - 1
      // - Integer.toBinaryString( triggerWordBitIndex ).indexOf( "1" );
      // }

      cell = aSheet.getCellAt( EVENT_PARAM_SRC_COLUMN, aRowNumber );
      paramTagName = ((String)cell.getValue()).trim();

      cell = aSheet.getCellAt( EVENT_PARAM_ID_COLUMN, aRowNumber );
      paramId = ((String)cell.getValue()).trim();
    }

    public String getEventId() {
      return eventId;
    }

    public boolean isOnTrigger() {
      return onTrigger;
    }

    public boolean isOffTrigger() {
      return offTrigger;
    }

    public int getTriggerWordBitIndex() {
      return triggerWordBitIndex;
    }

    public String getParamTagName() {
      return paramTagName;
    }

    public String getParamId() {
      return paramId;
    }

  }
}

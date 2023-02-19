package ru.toxsoft.l2.utils.opc.cfg.exe;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.utils.opc.cfg.exe.ISysClassRowDef.*;

import java.io.*;
import java.util.*;

import org.jopendocument.dom.spreadsheet.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.ods.*;

public class ClassTabsOdsFileReader
    extends CommonOdsFileReader {

  protected StringFieldValueGetter CLASS_TAG_ID_COLUMN = new StringFieldValueGetter( 0 );

  protected StringFieldValueGetter CLASS_ID_COLUMN = new StringFieldValueGetter( 1 );

  protected StringFieldValueGetter TAG_NAME_COLUMN = new StringFieldValueGetter( 4 );

  protected IntegerFieldValueGetter BIT_INDEX_COLUMN = new IntegerFieldValueGetter( 10, -1 );

  protected BooleanStringContaineFieldValueGetter IS_READ_COLUMN = new BooleanStringContaineFieldValueGetter( 12, "R" );

  protected BooleanStringContaineFieldValueGetter IS_WRITE_COLUMN =
      new BooleanStringContaineFieldValueGetter( 12, "W" );

  protected StringFieldValueGetter SYNCH_COLUMN = new StringFieldValueGetter( 16 );

  protected StringFieldValueGetter DATA_ID_COLUMN = new StringFieldValueGetter( 13 );

  protected StringFieldValueGetter EVENT_ID_COLUMN = new StringFieldValueGetter( 17 );

  protected StringFieldValueGetter EVENT_ON_MESSAGE_COLUMN = new StringFieldValueGetter( 19 );

  protected StringFieldValueGetter EVENT_OFF_MESSAGE_COLUMN = new StringFieldValueGetter( 20 );

  protected IntegerFieldValueGetter EVENT_ON_COLUMN = new IntegerFieldValueGetter( 21 );

  protected IntegerFieldValueGetter EVENT_OFF_COLUMN = new IntegerFieldValueGetter( 22 );

  protected StringFieldValueGetter CMD_ID_COLUMN = new StringFieldValueGetter( 23 );

  protected TagTypeFieldValueGetter TAG_TYPE_COLUMN = new TagTypeFieldValueGetter( 11 ); // 7 );

  // private List<ClassRow> classRows = new ArrayList<>();

  private List<IOptionSet> optSetRows = new ArrayList<>();

  public ClassTabsOdsFileReader( File odsFile, String aSheetName ) {
    super( odsFile, aSheetName );
  }

  @Override
  protected void readRow( Sheet aSheet, int aRowNumber ) {

    // ClassRow prevClassRow = classRows.size() == 0 ? null : classRows.get( classRows.size() - 1 );

    IOptionSet prevOptSet = optSetRows.size() == 0 ? null : optSetRows.get( optSetRows.size() - 1 );

    ClassRow newClassRow = new ClassRow();
    IOptionSetEdit newOpt = new OptionSet();

    String classTagId = CLASS_TAG_ID_COLUMN.getValue( aSheet, aRowNumber ); // для отладки

    String classId = CLASS_ID_COLUMN.getValue( aSheet, aRowNumber );

    String tagName = TAG_NAME_COLUMN.getValue( aSheet, aRowNumber );

    int bitIndex = BIT_INDEX_COLUMN.getValue( aSheet, aRowNumber ).intValue();

    if( classId.length() == 0 && tagName.length() == 0 && bitIndex < 0 ) {
      return;
    }

    if( classId.length() == 0 ) {
      if( prevOptSet == null ) {
        return;
      }
      classId = CLASS_ID_PARAM.getValue( prevOptSet ).asString();// prevClassRow.classId;
    }

    if( SYNCH_COLUMN.getValue( aSheet, aRowNumber ).equals( "не обрабатывать" ) ) {
      return;
    }

    String dataId = DATA_ID_COLUMN.getValue( aSheet, aRowNumber );

    String eventId = EVENT_ID_COLUMN.getValue( aSheet, aRowNumber );

    String eventOnMessage = EVENT_ON_MESSAGE_COLUMN.getValue( aSheet, aRowNumber );

    String eventOffMessage = EVENT_OFF_MESSAGE_COLUMN.getValue( aSheet, aRowNumber );

    boolean eventOn = EVENT_ON_COLUMN.getValue( aSheet, aRowNumber ) == 1;

    boolean eventOff = EVENT_OFF_COLUMN.getValue( aSheet, aRowNumber ) == 1;

    String cmdId = CMD_ID_COLUMN.getValue( aSheet, aRowNumber );

    Boolean isWrite = IS_WRITE_COLUMN.getValue( aSheet, aRowNumber );

    Boolean isRead = IS_READ_COLUMN.getValue( aSheet, aRowNumber );

    if( bitIndex >= 0 && dataId.length() == 0 && eventId.length() == 0 && cmdId.length() == 0 ) {
      return;
    }

    if( bitIndex >= 0 && prevOptSet != null ) {
      isRead = IS_READ_PARAM.getValue( prevOptSet ).asBool();// prevClassRow.isRead;
      isWrite = IS_WRITE_PARAM.getValue( prevOptSet ).asBool();// prevClassRow.isWrite;
    }

    if( !isRead ) {
      dataId = "";
    }

    ETagSynchType synchType =
        SYNCH_COLUMN.getValue( aSheet, aRowNumber ).equals( "sync" ) ? ETagSynchType.SYNCH : ETagSynchType.ASYNCH;
    synchType = (dataId.length() > 0 || eventId.length() > 0) ? synchType : null;

    if( !isWrite && (cmdId.length() > 0) ) {
      throw new TsIllegalArgumentRtException( "%s : write indicator does not coinside with cmdId content",
          classId + ". " + tagName );
    }

    if( !isRead && (synchType != null) ) {
      throw new TsIllegalArgumentRtException( "%s : read indicator does not coinside with synch content",
          classId + ". " + tagName );
    }

    ETagValueType valType = TAG_TYPE_COLUMN.getValue( aSheet, aRowNumber );

    newClassRow.classTagId = classTagId;
    CLASS_TAG_ID_PARAM.setValue( newOpt, avStr( classTagId ) );

    newClassRow.classId = classId;
    CLASS_ID_PARAM.setValue( newOpt, avStr( classId ) );

    newClassRow.tagName = tagName;
    TAG_NAME_PARAM.setValue( newOpt, avStr( tagName ) );

    newClassRow.bitIndex = bitIndex;
    BIT_INDEX_PARAM.setValue( newOpt, avInt( bitIndex ) );

    newClassRow.dataId = dataId;
    DATA_ID_PARAM.setValue( newOpt, avStr( dataId ) );

    newClassRow.eventId = eventId;
    EVENT_ID_PARAM.setValue( newOpt, avStr( eventId ) );

    newClassRow.eventOnMessge = eventOnMessage;
    EVENT_ON_MESSAGE_PARAM.setValue( newOpt, avStr( eventOnMessage ) );

    newClassRow.eventOffMessge = eventOffMessage;
    EVENT_OFF_MESSAGE_PARAM.setValue( newOpt, avStr( eventOffMessage ) );

    newClassRow.eventOn = eventOn;
    EVENT_ON_PARAM.setValue( newOpt, avBool( eventOn ) );

    newClassRow.eventOff = eventOff;
    EVENT_OFF_PARAM.setValue( newOpt, avBool( eventOff ) );

    newClassRow.cmdId = cmdId;
    COMMAND_ID_PARAM.setValue( newOpt, avStr( cmdId ) );

    newClassRow.isWrite = isWrite;
    IS_WRITE_PARAM.setValue( newOpt, avBool( isWrite ) );

    newClassRow.isRead = isRead;
    IS_READ_PARAM.setValue( newOpt, avBool( isRead ) );

    newClassRow.synchType = synchType;
    SYNCH_TYPE_PARAM.setValue( newOpt, synchType != null ? avStr( synchType.getName() ) : IAtomicValue.NULL );

    newClassRow.valType = valType;
    VAL_TYPE_PARAM.setValue( newOpt, avStr( valType.getName() ) );

    // classRows.add( newClassRow );
    optSetRows.add( newOpt );
  }

  public static class ClassRow {

    String classTagId;

    String classId;

    String tagName;

    int bitIndex;

    ETagSynchType synchType;

    String dataId;

    String eventId;

    String eventOnMessge;

    String eventOffMessge;

    boolean eventOn;

    boolean eventOff;

    String cmdId;

    boolean isWrite = false;

    boolean isRead = false;

    ETagValueType valType;

    public synchronized String classTagId() {
      return classTagId;
    }

    public synchronized String classId() {
      return classId;
    }

    public synchronized String tagName() {
      return tagName;
    }

    public synchronized int bitIndex() {
      return bitIndex;
    }

    public synchronized ETagSynchType synchType() {
      return synchType;
    }

    public synchronized String dataId() {
      return dataId;
    }

    public synchronized String eventId() {
      return eventId;
    }

    public synchronized boolean eventOn() {
      return eventOn;
    }

    public synchronized boolean eventOff() {
      return eventOff;
    }

    public synchronized String cmdId() {
      return cmdId;
    }

    public synchronized boolean write() {
      return isWrite;
    }

    public synchronized boolean read() {
      return isRead;
    }

    public synchronized ETagValueType valType() {
      return valType;
    }

  }

  public List<IOptionSet> getAllRows() {
    return optSetRows;
  }

  public List<IOptionSet> getRows( String aClassId, String aTagName ) {
    List<IOptionSet> result = new ArrayList<>();
    for( IOptionSet cr : optSetRows ) {
      String classId = CLASS_ID_PARAM.getValue( cr ).asString();
      String tagName = TAG_NAME_PARAM.getValue( cr ).asString();
      if( classId.equals( aClassId ) && tagName.equals( aTagName ) ) {
        result.add( cr );
        continue;
      }
      int bitIndex = BIT_INDEX_PARAM.getValue( cr ).asInt();
      if( result.size() > 0 ) {
        if( bitIndex >= 0 ) {
          result.add( cr );
        }
        else {
          return result;
        }
      }
    }
    return result;
  }

  // public List<ClassRow> getAllRows() {
  // return classRows;
  // }
  //
  // public List<ClassRow> getRows( String aClassId, String aTagName ) {
  // List<ClassRow> result = new ArrayList<>();
  // for( ClassRow cr : classRows ) {
  // if( cr.classId.equals( aClassId ) && cr.tagName.equals( aTagName ) ) {
  // result.add( cr );
  // continue;
  // }
  // if( result.size() > 0 ) {
  // if( cr.bitIndex >= 0 ) {
  // result.add( cr );
  // }
  // else {
  // return result;
  // }
  // }
  // }
  // return result;
  // }

  // public static void main( String[] a ) {
  // try {
  // ClassTabsOdsFileReader reader = new ClassTabsOdsFileReader( new File( "17023 Сигналы Москокс 2018-04-02.ods" ) );
  // reader.read();
  // int i = 1;
  // for( ClassRow cr : reader.getAllRows( /* "mcc.AnalogInput", "WS" */ ) ) {
  // System.out.print( i + " | " );
  // System.out.print( cr.classId + " | " );
  // System.out.print( cr.tagName + " | " );
  // System.out.print( cr.bitIndex + " | " );
  // System.out.print( cr.dataId + " | " );
  // System.out.print( cr.eventId + " | " );
  // System.out.print( cr.eventOn + " | " );
  // System.out.print( cr.eventOff + " | " );
  // System.out.print( cr.cmdId + " | " );
  // System.out.println();
  // }
  // i++;
  // }
  // catch( IOException e ) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }

}

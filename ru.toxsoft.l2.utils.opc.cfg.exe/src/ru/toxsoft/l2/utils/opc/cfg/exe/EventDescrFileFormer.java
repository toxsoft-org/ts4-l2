package ru.toxsoft.l2.utils.opc.cfg.exe;

import static ru.toxsoft.l2.utils.opc.cfg.exe.ISysClassRowDef.*;

import java.io.*;
import java.util.*;

import org.toxsoft.core.tslib.av.opset.*;

public class EventDescrFileFormer {

  private ClassTabsOdsFileReader classTabsOdsFileReader;

  public EventDescrFileFormer( File odsFile ) {

    classTabsOdsFileReader = new ClassTabsOdsFileReader( odsFile );
    try {
      classTabsOdsFileReader.read();
    }
    catch( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  void formDstTree( String aDstFile )
      throws IOException {

    File f = new File( aDstFile );
    FileWriter fw = new FileWriter( f );

    List<IOptionSet> rows = classTabsOdsFileReader.getAllRows();

    for( IOptionSet row : rows ) {
      String eventStr = formEventStrint( row );
      if( eventStr != null ) {
        fw.write( eventStr );
      }
    }

    fw.flush();

    fw.close();
  }

  private String formEventStrint( IOptionSet aRow ) {
    String eventId = EVENT_ID_PARAM.getValue( aRow ).asString();

    if( eventId == null || eventId.trim().length() == 0 ) {
      return null;
    }

    String classId = CLASS_ID_PARAM.getValue( aRow ).asString();

    if( classId == null || classId.trim().length() == 0 ) {
      return null;
    }

    StringBuilder result = new StringBuilder( "evGwid = Gwid.createEvent( \"" ).append( classId ).append( "\" , \"" )
        .append( eventId ).append( "\" );\n" );
    // eventFormattersRegistry.registerFomatter( evGwid, sessionChangeJournalEventFormatter );

    boolean eventOn = EVENT_ON_PARAM.getValue( aRow ).asBool();
    boolean eventOff = EVENT_OFF_PARAM.getValue( aRow ).asBool();

    String eventOnMessage = EVENT_ON_MESSAGE_PARAM.getValue( aRow ).asString();
    String eventOffMessage = EVENT_OFF_MESSAGE_PARAM.getValue( aRow ).asString();

    eventOnMessage = "\"" + eventOnMessage + "\"";

    eventOffMessage = "\"" + eventOffMessage + "\"";

    String className = "valueChangeJournalEventFormatter";

    if( eventOn && eventOff ) {
      className = "new TriggeredJournalEventFormatter( " + eventOnMessage + " , " + eventOffMessage + " )";
    }
    else
      if( eventOn || eventOff ) {
        className = "new TriggeredJournalEventFormatter( "
            + (eventOn ? (eventOnMessage + " , true") : (eventOffMessage + " , false")) + " )";
      }

    result.append( "eventFormattersRegistry.registerFomatter( evGwid, " ).append( className ).append( " );\n" );

    return result.toString();
  }

  public static void main( String[] args ) {
    String srcFileName = args[0];

    String dstFileName = args[1];

    try {
      new EventDescrFileFormer( new File( srcFileName ) ).formDstTree( dstFileName );
    }
    catch( IOException ex ) {
      ex.printStackTrace();
    }
  }

}

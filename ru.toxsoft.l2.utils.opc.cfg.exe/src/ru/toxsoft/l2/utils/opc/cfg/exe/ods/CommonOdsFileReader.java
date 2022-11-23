package ru.toxsoft.l2.utils.opc.cfg.exe.ods;

import java.io.File;
import java.io.IOException;

import org.jopendocument.dom.spreadsheet.*;

public class CommonOdsFileReader {

  private File odsFile;

  private String sheetName;

  private int checkColumn = 0;

  private int startRowIndex = 1;

  public CommonOdsFileReader( File odsFile, String sheetName ) {
    super();
    this.odsFile = odsFile;
    this.sheetName = sheetName;
  }

  public CommonOdsFileReader( File odsFile, String sheetName, int checkColumn, int startRowIndex ) {
    super();
    this.odsFile = odsFile;
    this.sheetName = sheetName;
    this.checkColumn = checkColumn;
    this.startRowIndex = startRowIndex;
  }

  public void read()
      throws IOException {

    Sheet sheet;

    // закладка с описанием сигналов ТС
    sheet = SpreadSheet.createFromFile( odsFile ).getSheet( sheetName );

    int nRowCount = Math.min( 100000, sheet.getRowCount() );

    int emptyCount = 0;

    for( int nRowIndex = startRowIndex; nRowIndex < nRowCount; nRowIndex++ ) {

      // далее код для корректной остановки - количество строк иногда оказывается некорректным

      MutableCell<?> cell = sheet.getCellAt( checkColumn, nRowIndex );

      if( cell.isEmpty() ) {
        emptyCount++;
      }
      else {
        emptyCount = 0;
      }

      try {
        readRow( sheet, nRowIndex );
      }
      catch( Exception e ) {
        e.printStackTrace();
      }

      if( emptyCount > 100 ) {
        break;
      }
    }

  }

  protected void readRow( Sheet aSheet, int aRow ) {
    System.out.println( aRow );
  }

  public static void main( String[] a ) {
    try {
      new CommonOdsFileReader( new File( "17023 Сигналы Москокс 2018-07-09v2.ods" ), "Объекты" ).read();
    }
    catch( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}

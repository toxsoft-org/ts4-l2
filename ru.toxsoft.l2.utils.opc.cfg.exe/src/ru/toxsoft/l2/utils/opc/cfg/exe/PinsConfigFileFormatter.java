package ru.toxsoft.l2.utils.opc.cfg.exe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Класс форматирования файла конфигурации пинов - а именно - вставка переноса на новую строку после фигурных скобок {}
 *
 * @author max
 */
public class PinsConfigFileFormatter {

  private static final String QOUTE               = "\"";
  private static final String QOUTED_ALIAS_START  = "$qoute";
  private static final String QOUTED_ALIAS_FORMAT = QOUTED_ALIAS_START + "%d$";
  private static final String currSearchStr1      = "{";                       //$NON-NLS-1$
  private static final String currSearchStr2      = "}";                       //$NON-NLS-1$
  private static final String currSearchStr3      = "[";                       //$NON-NLS-1$
  private static final String currSearchStr4      = "]";                       //$NON-NLS-1$
  private static final String comma               = ",";                       //$NON-NLS-1$

  public static void main( String[] args )
      throws IOException {
    String initFile = args[0];
    String destFile = args[1];

    format( initFile, destFile, args.length >= 3 ? args[2] : null );

  }

  public static void format( String aInitFile, String aDestFile, String aAdditionStart )
      throws FileNotFoundException,
      IOException {
    BufferedReader br = new BufferedReader( new FileReader( new File( aInitFile ) ) );

    String line = br.readLine();

    if( line != null && aAdditionStart != null ) {
      line = aAdditionStart + line;
    }

    StringBuilder outStrBuilder = new StringBuilder();

    while( line != null ) {

      formatAndAddLine( outStrBuilder, line );

      line = br.readLine();
    }

    br.close();

    FileWriter fw = new FileWriter( new File( aDestFile ) );
    fw.write( outStrBuilder.toString() );
    fw.close();
  }

  private static void formatAndAddLine( StringBuilder aOutStrBuilder, String aLine ) {

    // подготовка - вытащить из строки все куски находящиеся в "" и заменить их номерами

    Map<String, String> quotedStrings = new HashMap<>();

    String init = aLine;
    String result = "";
    do {
      String[] split = getCuttedLine( init, quotedStrings );
      result += split[0];
      init = split[1];
    } while( init != null );

    boolean open = true;
    int count = 1;

    int currStartIndex = 0;
    int currIndex = Math.min( result.indexOf( currSearchStr1 ), result.indexOf( currSearchStr3 ) );

    while( currIndex >= 0 ) {
      String str = result.substring( currStartIndex, currIndex );
      if( str.length() > 0 ) {

        StringTokenizer st = new StringTokenizer( str, comma, true );
        List<String> tokens = new ArrayList<>();
        String prev = "";
        while( st.hasMoreTokens() ) {
          String stStr = st.nextToken();
          if( stStr.equals( comma ) ) {
            tokens.add( prev + stStr );
            prev = "";
          }
          else {
            prev = stStr;
          }
        }
        if( prev.length() > 0 ) {
          tokens.add( prev );
        }

        for( String stStr : tokens ) {
          for( int i = 0; i < (open ? count - 1 : count + 1); i++ ) {
            aOutStrBuilder.append( "  " );
          }

          aOutStrBuilder.append( checkQuotedStrings( stStr, quotedStrings ) );
          aOutStrBuilder.append( "\n" );
        }

      }
      for( int i = 0; i < (open ? (count - 1) : (count)); i++ ) {
        aOutStrBuilder.append( "  " );
      }
      aOutStrBuilder.append( checkQuotedStrings( result.substring( currIndex, currIndex + 1 ), quotedStrings ) );
      aOutStrBuilder.append( "\n" );
      currStartIndex = currIndex + 1;

      int currIndex1 = result.indexOf( currSearchStr1, currStartIndex );
      int currIndex11 = result.indexOf( currSearchStr3, currStartIndex );

      if( currIndex1 < 0 || currIndex11 < 0 ) {
        if( currIndex1 < 0 ) {
          currIndex1 = currIndex11;
        }
      }
      else {
        currIndex1 = Math.min( currIndex1, currIndex11 );
      }

      int currIndex2 = result.indexOf( currSearchStr2, currStartIndex );
      int currIndex21 = result.indexOf( currSearchStr4, currStartIndex );

      if( currIndex2 < 0 || currIndex21 < 0 ) {
        if( currIndex2 < 0 ) {
          currIndex2 = currIndex21;
        }
      }
      else {
        currIndex2 = Math.min( currIndex2, currIndex21 );
      }

      if( currIndex1 < 0 || currIndex2 < 0 ) {
        if( currIndex1 < 0 && currIndex2 >= 0 ) {
          count--;
          open = false;
          currIndex1 = currIndex2;
        }

        if( currIndex1 >= 0 && currIndex2 < 0 ) {
          count++;
          open = true;
        }

        if( currIndex1 < 0 && currIndex2 < 0 ) {
          currIndex1 = -1;
        }
      }
      else {

        if( currIndex1 < currIndex2 ) {
          count++;
          open = true;
        }
        else {
          count--;
          open = false;
        }

        currIndex1 = Math.min( currIndex1, currIndex2 );
      }

      currIndex = currIndex1;
    }

  }

  static String checkQuotedStrings( String aCheckingStr, Map<String, String> aQuotedStrings ) {
    if( !aCheckingStr.contains( QOUTED_ALIAS_START ) ) {
      return aCheckingStr;
    }
    String result = aCheckingStr;
    Iterator<String> strIter = aQuotedStrings.keySet().iterator();

    Set<String> keysToDel = new HashSet<>();
    while( strIter.hasNext() ) {
      String key = strIter.next();

      if( result.contains( key ) ) {
        result = result.replace( key, aQuotedStrings.get( key ) );
        keysToDel.add( key );
      }
    }

    for( String key : keysToDel ) {
      aQuotedStrings.remove( key );
    }
    return result;
  }

  static String[] getCuttedLine( String aInit, Map<String, String> aQuotedStrings ) {
    String qoutedStr = null;
    int second = 0;
    int first = 0;
    while( qoutedStr == null ) {
      first = aInit.indexOf( QOUTE, second );

      if( first < 0 ) {
        break;
      }

      second = aInit.indexOf( QOUTE, first + 1 );
      if( second < 0 ) {
        throw new IllegalArgumentException( "The end sign \" not found" );
      }
      second++;
      String qoutedStrCandidate = aInit.substring( first, second );
      if( qoutedStrCandidate.contains( currSearchStr1 ) || qoutedStrCandidate.contains( currSearchStr2 )
          || qoutedStrCandidate.contains( currSearchStr3 ) || qoutedStrCandidate.contains( currSearchStr4 )
          || qoutedStrCandidate.contains( comma ) ) {
        qoutedStr = qoutedStrCandidate;
      }
    }
    if( qoutedStr == null ) {
      return new String[] { aInit, null };
    }

    String key = String.format( QOUTED_ALIAS_FORMAT, aQuotedStrings.size() );

    aQuotedStrings.put( key, qoutedStr );

    String start = aInit.substring( 0, first ) + key;
    String end = aInit.substring( second );
    return new String[] { start, end };
  }

}

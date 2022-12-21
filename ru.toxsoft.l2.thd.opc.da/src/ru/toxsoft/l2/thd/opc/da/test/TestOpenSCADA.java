package ru.toxsoft.l2.thd.opc.da.test;

import static ru.toxsoft.l2.thd.opc.da.IL2Resources.*;

import java.io.*;
import java.util.concurrent.*;

import org.jinterop.dcom.common.*;
import org.jinterop.dcom.core.*;
import org.openscada.opc.lib.da.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.hal.devices.*;

/**
 * Тест библиотеки OpenSCADA
 *
 * @author Max
 */
public class TestOpenSCADA
    implements DataCallback {

  private static final String OUTPUT_TAGS_FILE = "output.txt";

  private static final String ASYNC_TAGS_FILE = "async.txt";

  private static final String SYNC_TAGS_FILE = "sync.txt";

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( TestOpenSCADA.class.getName() );

  public static void main( String[] args ) {
    logger.info( "Started" );
    if( args.length != 4 ) {
      logger.error( "Wrong params. Proper params: host, user, pass, cls" );
      return;
    }

    new TestOpenSCADA( args[0], args[1], args[2], args[3] );

  }

  private static final int updatePeriod = 500;

  @SuppressWarnings( "nls" )
  private static final String SYNC_GROUP   = "ops2s5_sync_data_group";
  @SuppressWarnings( "nls" )
  private static final String ASYNC_GROUP  = "ops2s5_async_data_group";
  @SuppressWarnings( "nls" )
  private static final String OUTPUT_GROUP = "ops2s5_output_tags_group";
  /**
   * OPC server
   */
  private final Server        server;

  /**
   * Группа синхронных данных
   */
  private Group syncGroup;

  /**
   * Группы асинхронных данных
   */
  // переходим полностью на синхронное чтение
  private AccessBase asyncGroup;
  // private Group asyncGroup;
  /**
   * Группа выходных каналов
   */
  private Group outputGroup;

  private AutoReconnectController autoReconnectController;

  private IStringList syncList;

  private IStringList asyncList;

  private IStringMap<String> outputMap;

  private Item outputItem;

  private String outputValType = "Boolean";

  public TestOpenSCADA( String aHost, String aUser, String aPass, String aCls ) {

    // create connection information
    final org.openscada.opc.lib.common.ConnectionInformation ci =
        new org.openscada.opc.lib.common.ConnectionInformation();
    ci.setHost( aHost );
    ci.setUser( aUser );
    ci.setPassword( aPass );
    ci.setClsid( aCls );

    syncList = IStringList.EMPTY;
    try {
      syncList = loadStringListFromFile( SYNC_TAGS_FILE );
      logger.debug( "Sync tags loaded size = %d", Integer.valueOf( syncList.size() ) );
    }
    catch( IOException ex ) {
    }

    asyncList = IStringList.EMPTY;
    try {
      asyncList = loadStringListFromFile( ASYNC_TAGS_FILE );
      logger.debug( "Async tags loaded size = %d", Integer.valueOf( asyncList.size() ) );
    }
    catch( IOException ex ) {
    }

    outputMap = IStringMap.EMPTY;
    try {
      outputMap = loadStringMapFromFile( OUTPUT_TAGS_FILE );
      logger.debug( "Output tags loaded size = %d", Integer.valueOf( outputMap.size() ) );
    }
    catch( IOException ex ) {
    }

    // Создаем соединение с сервером
    server = new Server( ci, Executors.newSingleThreadScheduledExecutor() );
    // server = new Server( ci, Executors.newScheduledThreadPool( 3 ) );
    server.setDefaultUpdateRate( 500 );
    // disable GC for COM objects to prevent the socket from being closed
    // JISystem.setJavaCoClassAutoCollection( false );
    // Turn off logging
    java.util.logging.Logger.getLogger( "org.jinterop" ).setLevel( java.util.logging.Level.OFF );
    autoReconnectController = new AutoReconnectController( server );
    autoReconnectController.addListener( new AutoReconnectListener() {

      @SuppressWarnings( "synthetic-access" )
      @Override
      public void stateChanged( AutoReconnectState state ) {
        logger.info( AUTO_RECONNECT_STATE, state.name() );
        if( state == AutoReconnectState.CONNECTED ) {

          checkOPCTags( syncList, asyncList, outputMap );

        }

      }
    } );
    // do some actions before connect

    autoReconnectController.connect();

    // ScheduledExecutorService writeThread = Executors.newSingleThreadScheduledExecutor();
    // writeThread.scheduleWithFixedDelay( this::writeValuesOnLL, 10, 3, TimeUnit.SECONDS ); // The code is executed for
    // the first time 5 seconds
    // after startup, and the code
    // is
    // executed every 3 seconds thereafter

    // wait a little bit, delay 3 seconds
    // try {
    // Thread.sleep( 3 * 1000 );
    // }
    // catch( InterruptedException ex1 ) {
    //
    // }
    // writeThread.shutdownNow();

    while( true ) {
      try {
        Thread.sleep( 3000L );
      }
      catch( InterruptedException ex ) {
      }

      Thread t = new Thread( this::writeValuesOnLL );
      t.setPriority( Thread.MAX_PRIORITY );
      t.start();
    }
  }

  private void writeValuesOnLL()
      throws TsMultipleApparatRtException {

    try {
      JIVariant outputVal = null;
      String outputValStr = null;
      outputValStr = switch( outputValType ) {
        case "Float" -> {
          float fValue = (float)Math.random();
          outputVal = new JIVariant( fValue );
          yield String.valueOf( fValue );
        }
        case "Integer" -> {
          int iValue = (int)(1000 * Math.random());
          outputVal = new JIVariant( iValue );
          yield String.valueOf( iValue );
        }
        case "Boolean" -> {
          boolean bValue = Math.random() > 0.5;
          outputVal = new JIVariant( bValue );
          yield String.valueOf( bValue );
        }
        case "String" -> {
          String sValue = "empty";
          outputVal = new JIVariant( sValue );
          yield sValue;
        }
        default -> throw new TsNotAllEnumsUsedRtException();
      };

      long startWriteTime = System.currentTimeMillis();
      // outputItem.write( outputVal );
      WriteRequest request = new WriteRequest( outputItem, outputVal );
      outputGroup.write( request );
      long writeTime = System.currentTimeMillis() - startWriteTime;
      logger.debug( "Wrote tag %s, value=%s", outputItem.getId(), outputValStr );
      logger.debug( "Write Time = %d", Long.valueOf( writeTime ) );
    }
    catch( Exception e ) {
      logger.error( e );
    }
  }

  /**
   * Проверка тегов ОРС сервера
   *
   * @param aSyncTags описание синхронных тегов
   * @param aAsyncTags описание асинхронных тегов
   * @param aOutputTags описание выходных тегов
   */
  @SuppressWarnings( "nls" )
  private void checkOPCTags( IStringList aSyncTags, IStringList aAsyncTags, IStringMap<String> aOutputTags ) {

    try {
      // Группа синхронных данных
      syncGroup = server.addGroup( SYNC_GROUP );

      // Группа асинхронных данных
      asyncGroup = new Async20Access( server, updatePeriod, true );
      // asyncGroup = server.addGroup( ASYNC_GROUP );

      // Группа выходных пинов
      outputGroup = server.addGroup( OUTPUT_GROUP );
    }
    catch( Exception e ) {
      logger.error( CANT_CONNECT_OPC, e.getMessage() );
      return;
    }
    try {
      logger.debug( "Synch tags adding to the group: %s", String.valueOf( aSyncTags.size() ) );
      for( String tagDef : aSyncTags ) {
        try {
          Item syncItem = syncGroup.addItem( tagDef );
          // syncItems.add( syncItem );
        }
        catch( final JIException e ) {
          logger.error( ERR_ADD_SYNC_GROUP );
        }
        catch( AddFailedException ex ) {
          logger.error( CANT_ADD_SYNC_TAG, tagDef );
          // syncGroup.removeItem( tagDef.tagId() );
        }
      }
      logger.debug( "Asynch tags adding to the group: %s", String.valueOf( aAsyncTags.size() ) );
      for( String tagDef : aAsyncTags ) {
        try {
          // Группа асинхронных данных
          asyncGroup.addItem( tagDef, this );
          logger.debug( "Tag %s added to group: %s", tagDef, "Async20Access" );
          // Item asyncItem = asyncGroup.addItem( tagDef.tagId() );
          // asyncItems.add( asyncItem );
        }
        catch( final JIException e ) {
          logger.error( ERR_ADD_ASYNC_GROUP );
        }
        catch( AddFailedException ex ) {
          logger.error( CANT_ADD_ASYNC_TAG, tagDef );
          // asyncGroup.removeItem( tagDef.tagId() );
        }
      }
      logger.debug( "Output tags adding to the group: %s", String.valueOf( aOutputTags.size() ) );
      for( String tagDef : aOutputTags.keys() ) {
        try {
          outputItem = outputGroup.addItem( tagDef );
          // outputItem.setActive( true );
          outputValType = aOutputTags.getByKey( tagDef );
        }
        catch( final JIException e ) {
          logger.error( ERR_ADD_OUTPUT_GROUP );
        }
        catch( AddFailedException ex ) {
          logger.error( CANT_ADD_OUTPUT_TAG, tagDef );
          // outputGroup.removeItem( tagDef.tagId() );
        }
      }
      // Начинаем работу c того что запрашиваем значения всех тегов
      // asyncGroup.bind(); // binding removed for test
      logger.debug( "init OPC items OK" );

    }
    catch( final Exception e ) {
      logger.error( ERROR_OPENSCADA_INIT, e.getMessage() );
    }

  }

  @Override
  public void changed( Item aItem, ItemState aItemState ) {
    try {
      logger.debug( "Item=%s, value=%s", aItem.getId(), String.valueOf( aItemState.getValue().getObjectAsFloat() ) );
    }
    catch( JIException ex ) {
      logger.debug( "Item=%s, parse error", aItem.getId() );
    }

  }

  private static IStringList loadStringListFromFile( String aFile )
      throws IOException {
    IStringListEdit result = new StringArrayList();
    FileReader fr = new FileReader( aFile );
    BufferedReader br = new BufferedReader( fr );
    String line = br.readLine();

    while( line != null ) {
      result.add( line );
      line = br.readLine();
    }
    br.close();
    fr.close();

    return result;
  }

  private static IStringMap<String> loadStringMapFromFile( String aFile )
      throws IOException {
    IStringMapEdit<String> result = new StringMap<>();
    FileReader fr = new FileReader( aFile );
    BufferedReader br = new BufferedReader( fr );
    String line = br.readLine();

    String tag = new String();
    while( line != null ) {
      if( tag.length() == 0 ) {
        tag = line.trim();
      }
      else {
        result.put( tag, line );
        tag = new String();
      }

      line = br.readLine();
    }
    br.close();
    fr.close();

    return result;
  }

}

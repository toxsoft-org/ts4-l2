package org.toxsoft.l2.thd.s7;

import static org.toxsoft.l2.thd.s7.IS7ThdConstants.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.thd.opc.*;

import Moka7.*;

/**
 * Одно устройство работающее по протоколу Siemens S7. <br>
 * IP + Rack + Slot <br>
 * один девайс - один коннект
 *
 * @author dima
 */
public class S7Device
    extends AbstractSpecificDevice
    implements ITsOpc {

  /**
   * Интервал между запросами.
   */
  private static final long REQUESTS_INTERVAL = 100L; // 20L;

  private static final int MAX_PERMIS_READ_ERROR_COUNT = 3;

  private static final S7Client Client = new S7Client();

  /**
   * массив читателей из блоков данных
   */
  private IListEdit<DataBlockReader> dbReaders = new ElemArrayList<>();

  /**
   * массив писателей в блоки данных
   */
  private IListEdit<DataBlockWriter> dbWriters = new ElemArrayList<>();

  /**
   * Все теги.
   */
  private IStringMapEdit<ITag> tags = new StringMap<>();

  /**
   * Конструктор по идентфикатору и настройкам
   *
   * @param aId - идентфикатор
   * @param aDescription - описание
   * @param aDefinition - настройки
   * @param aErrorProcessor - обработчик ошибок
   */
  public S7Device( String aId, String aDescription, IAvTree aDefinition, IHalErrorProcessor aErrorProcessor ) {
    super( aId, aDescription, aErrorProcessor );
    configYourself( aDefinition );
  }

  /**
   * Проводит настройку устройства (создание соединения, тегов и буферов)
   *
   * @param aDefinition {@link IAvTree} - все необходимые для настройки параметры.
   */
  private void configYourself( IAvTree aDefinition ) {
    // конфигурация соединения
    configConnection( aDefinition.fields() );
    // config db readers and writers
    IAvTree dbArray = aDefinition.nodes().findByKey( DATABLOCKS_ARRAY_NAME );
    for( int i = 0; i < dbArray.arrayLength(); i++ ) {
      IAvTree dbParams = dbArray.arrayElement( i );
      configReadDataBlock( dbParams );
      configWriteDataBlock( dbParams );
    }
  }

  private void configReadDataBlock( IAvTree aDbParams ) {
    int dbNumber = aDbParams.fields().getInt( DB_NUM );
    DataBlockReader dbReader = new DataBlockReader( Client, dbNumber );
    dbReader.configYourself( aDbParams );
    dbReaders.add( dbReader );
    tags.putAll( dbReader.getTags() );
  }

  private void configWriteDataBlock( IAvTree aDbParams ) {
    int dbNumber = aDbParams.fields().getInt( DB_NUM );
    DataBlockWriter dbWriter = new DataBlockWriter( Client, dbNumber );
    dbWriter.configYourself( aDbParams );
    dbWriters.add( dbWriter );
    tags.putAll( dbWriter.getTags() );
  }

  @SuppressWarnings( { "nls", "boxing" } )
  private static void configConnection( IOptionSet aConnectionParams ) {
    String IpAddress = aConnectionParams.getStr( DEVICE_IP_ADDRESS, TsLibUtils.EMPTY_STRING );
    int rack = aConnectionParams.getInt( DEVICE_RACK, 0 ); // Default 0 for S7300
    int slot = aConnectionParams.getInt( DEVICE_SLOT, 2 ); // Default 2 for S7300
    Client.SetConnectionType( S7.OP );
    int Result = Client.ConnectTo( IpAddress, rack, slot );
    if( Result == 0 ) {
      LoggerUtils.debug( "Connected to IP : %s (Rack=%d, Slot= %d)", IpAddress, rack, slot );
      LoggerUtils.debug( "PDU negotiated : %d bytes", Client.PDULength() );
    }
  }

  @Override
  public ITag tag( String aTagId ) {
    return tags.findByKey( aTagId );
  }

  @Override
  public IStringMap<ITag> tags() {
    return tags;
  }

  @SuppressWarnings( { "boxing", "nls" } )
  @Override
  protected void readValuesFromLL()
      throws TsMultipleApparatRtException {
    LoggerUtils.debug( "DB count = %s", String.valueOf( dbReaders.size() ) );
    for( DataBlockReader dbReader : dbReaders ) {
      // just read whole block form Siemens only
      int errCode = dbReader.readDB();
      if( errCode != 0 ) {
        LoggerUtils.error( "data block : %d, error code = ", dbReader.dbNumber(), errCode );
      }
    }
    try {
      Thread.sleep( REQUESTS_INTERVAL );
    }
    catch( InterruptedException e ) {
      LoggerUtils.error( e );
    }

  }

  @Override
  protected void writeValuesOnLL()
      throws TsMultipleApparatRtException {
    for( DataBlockWriter dbWriter : dbWriters ) {
      dbWriter.writeDB();
    }
  }

  @Override
  protected void putInBufferOutputValues() {
    for( DataBlockWriter dbWriter : dbWriters ) {
      dbWriter.fromTags2OutputBuffer();
    }

  }

  @Override
  protected void getFromBufferInputValues() {
    for( DataBlockReader dbReader : dbReaders ) {
      dbReader.fromBuffer2Tags();
    }
  }

  @Override
  protected void closeApparatResources()
      throws Exception {
    // TODO
  }

  // for test only
  public void readValuesFromLL1()
      throws TsMultipleApparatRtException {
    LoggerUtils.debug( "DB count = %s", String.valueOf( dbReaders.size() ) );
    for( DataBlockReader dbReader : dbReaders ) {
      // just read whole block form Siemens only
      int errCode = dbReader.readDB();
      if( errCode != 0 ) {
        LoggerUtils.error( "data block : %d, error code = ", dbReader.dbNumber(), errCode );
      }
    }
    try {
      Thread.sleep( REQUESTS_INTERVAL );
    }
    catch( InterruptedException e ) {
      LoggerUtils.error( e );
    }
    for( DataBlockReader dbReader : dbReaders ) {
      dbReader.fromBuffer2Tags();
    }

  }

}

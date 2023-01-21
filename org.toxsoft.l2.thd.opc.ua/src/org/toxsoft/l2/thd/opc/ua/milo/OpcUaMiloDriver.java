package org.toxsoft.l2.thd.opc.ua.milo;

import static ru.toxsoft.l2.thd.opc.IOpcConstants.*;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.sdk.client.api.config.*;
import org.eclipse.milo.opcua.sdk.client.api.identity.*;
import org.eclipse.milo.opcua.stack.core.security.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Реализация UA моста opc 2 S5 на движке milo
 *
 * @author dima
 */
public class OpcUaMiloDriver
    extends AbstractSpecificDevice
    implements ITsOpc {

  @SuppressWarnings( "nls" )
  private static final String SYNC_GROUP   = "ops2s5_sync_data_group";
  @SuppressWarnings( "nls" )
  private static final String ASYNC_GROUP  = "ops2s5_async_data_group";
  @SuppressWarnings( "nls" )
  private static final String OUTPUT_GROUP = "ops2s5_output_tags_group";

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  private NodesReader opcUaNodesReader;

  private NodesWriter opcUaNodesWriter;

  private OpcUaClient client;

  private StringMap<ITag> tags = new StringMap<>();
  private String          host = new String();
  private String          user = new String();
  private String          pass = new String();

  /**
   * @param aId String - строковый идентификатор.
   * @param aDescription String - описание.
   * @param aErrorProcessor {@link IHalErrorProcessor} - обработчик ошибок, возникающих при работе с НУ.
   * @param aCi - информация о соединении
   * @param aSyncTags - описание синхронных тегов
   * @param aUpdatePeriod - период обновления
   * @param aAsyncTags - описание асинхронных тегов
   * @param aOutputTags - описание тегов на запись
   */
  public OpcUaMiloDriver( String aId, String aDescription, IHalErrorProcessor aErrorProcessor, IAvTree aCfgInfo ) {
    super( aId, aDescription, aErrorProcessor );

    // create connection information
    // final org.openscada.opc.lib.common.ConnectionInformation ci =
    // new org.openscada.opc.lib.common.ConnectionInformation();
    // ci.setHost( aCi.host() );
    // ci.setUser( aCi.user() );
    // ci.setPassword( aCi.password() );
    // ci.setClsid( aCi.clsId() );

    host = aCfgInfo.fields().getStr( HOST_PARAM_NAME );
    user = aCfgInfo.fields().getStr( USER_PARAM_NAME, TsLibUtils.EMPTY_STRING );
    pass = aCfgInfo.fields().getStr( PASSWORD_PARAM_NAME, TsLibUtils.EMPTY_STRING );

    // Создаем соединение с сервером
    // server = new Server( ci, Executors.newSingleThreadScheduledExecutor() );
    // autoReconnectController.connect();
    try {
      client = createClient();// new OpcUaClient( null, null );// TODO
      client.connect().get();
      opcUaNodesReader = new NodesReader( client );
      opcUaNodesWriter = new NodesWriter( client );

      IAvTree groupsConfig = aCfgInfo.nodes().findByKey( GROUPS_PARAM_NAME );

      opcUaNodesReader.config( groupsConfig );
      opcUaNodesWriter.config( groupsConfig );

      IMapEdit<String, TagImpl> readTags = opcUaNodesReader.getTags();
      IMap<String, TagImpl> writeTags = opcUaNodesWriter.getTags();

      // поиск совпадающих тегов на запись и на чтение
      IList<String> writeTagsKeys = writeTags.keys();
      for( String writeTagKey : writeTagsKeys ) {
        if( readTags.hasKey( writeTagKey ) ) {
          TagImpl wTagImpl = writeTags.getByKey( writeTagKey );
          // если идентификаторы тегов чтения и записи совпадают - поменять тип тега и заменить тег на чтение
          wTagImpl.setKind( EKind.RW );
          readTags.put( writeTagKey, wTagImpl );
        }

        // просто добавить все теги на запись в список тегов
        tags.put( writeTagKey, writeTags.getByKey( writeTagKey ) );
      }

      // просто добавить все теги на чтение в список тегов
      IList<String> readTagsKeys = readTags.keys();
      for( String readTagKey : readTagsKeys ) {
        tags.put( readTagKey, readTags.getByKey( readTagKey ) );
      }

      // список тегов печать
      IList<String> tagsKeys = tags.keys();
      for( String tagKey : tagsKeys ) {
        logger.info( "Tag '%s' of type %s", tagKey, tags.getByKey( tagKey ).kind().getName() );
      }
    }
    catch( Exception ex ) {
      logger.error( ex );
    }
  }

  @Override
  public void readValuesFromLL() {
    opcUaNodesReader.readValuesFromNodes();
  }

  @Override
  public void writeValuesOnLL()
      throws TsMultipleApparatRtException {

    try {
      opcUaNodesWriter.writeValuesToNodes();
    }
    catch( InterruptedException | ExecutionException ex ) {
      // TODO Auto-generated catch block
      logger.error( ex );
    }
  }

  @Override
  public void putInBufferOutputValues() {
    opcUaNodesWriter.writeValuesToBuffer();
  }

  @Override
  public void getFromBufferInputValues() {
    opcUaNodesReader.readValuesFromBuffer();
  }

  @Override
  public void closeApparatResources() {
    try {
      // stop async reading
      // asyncGroup.unbind();
      // server.disconnect();
      client.disconnect().get();
    }
    catch( Exception ex ) {
      logger.error( ex );
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

  private OpcUaClient createClient()
      throws Exception {
    Path securityTempDir = Paths.get( System.getProperty( "java.io.tmpdir" ), "security" );
    Files.createDirectories( securityTempDir );
    if( !Files.exists( securityTempDir ) ) {
      throw new Exception( "unable to create security dir: " + securityTempDir );
    }

    logger.info( "security temp dir: %s", securityTempDir.toAbsolutePath().toString() );

    KeyStoreLoader loader = new KeyStoreLoader().load( securityTempDir );

    Function<List<EndpointDescription>, Optional<EndpointDescription>> selectEndpoint = aEendpoints -> {
      Optional<EndpointDescription> result = aEendpoints.stream().filter( endpointFilter() ).findFirst();
      return result;
    };

    Function<OpcUaClientConfigBuilder, OpcUaClientConfig> buildConfig = aConfigBuilder -> {
      OpcUaClientConfig result =
          aConfigBuilder.setApplicationName( LocalizedText.english( "eclipse milo opc-ua client" ) )
              .setApplicationUri( "urn:eclipse:milo:examples:client" ).setCertificate( loader.getClientCertificate() )
              .setKeyPair( loader.getClientKeyPair() ).setIdentityProvider( getIdentityProvider() )
              .setRequestTimeout( Unsigned.uint( 5000 ) ).build();
      return result;
    };

    return OpcUaClient.create( getEndpointUrl(), selectEndpoint, buildConfig );
  }

  String getEndpointUrl() {
    return host;
    // return "opc.tcp://192.168.153.1:4850"; //poligon
    // return "opc.tcp://localhost:12686/milo";

  }

  Predicate<EndpointDescription> endpointFilter() {
    return e -> true;
  }

  SecurityPolicy getSecurityPolicy() {
    return SecurityPolicy.None;
  }

  IdentityProvider getIdentityProvider() {
    if( user.length() > 0 ) {
      // return new UsernameProvider("admin","123"); //poligon
      return new UsernameProvider( user, pass );
    }

    return new AnonymousProvider();
  }

}

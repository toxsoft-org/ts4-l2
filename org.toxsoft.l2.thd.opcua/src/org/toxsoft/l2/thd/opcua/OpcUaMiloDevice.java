package org.toxsoft.l2.thd.opcua;

import static org.toxsoft.l2.thd.opcua.IOpcConstants.*;

import java.nio.file.*;
import java.util.*;
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
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.rwkind.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * Реализация устройства OPC-UA
 *
 * @author max
 */
public class OpcUaMiloDevice
    extends L2AbstractHalDevice {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  protected NodesReader opcUaNodesReader;

  private NodesWriter opcUaNodesWriter;

  private OpcUaClient client;

  private String host = new String();
  private String user = new String();
  private String pass = new String();

  public OpcUaMiloDevice( IL2SharedContext aL2Context, L2ModuleConfigFile aDeviceCfg ) {
    super( aL2Context, aDeviceCfg );
    // TODO Auto-generated constructor stub
  }

  @Override
  protected IStridablesList<L2AbstractTag> doCreateTags() {
    IStridablesListEdit<L2AbstractTag> tags = new StridablesList<>();
    try {
      // dima 26.05.25
      // try until got connection to OPC UA server
      boolean connected = false;
      while( !connected ) {
        try {
          client = createClient();// new OpcUaClient( null, null );// TODO
          client.connect().get();
          logger.debug( "Driver has connected to OPC UA server on host: '%s'", host ); //$NON-NLS-1$
          connected = true;
        }
        catch( Exception connectioEx ) {
          logger.error( "Exception in process of connection to OPC UA server. Error message: '%s'", //$NON-NLS-1$
              connectioEx.getMessage() );
        }
      }
      TagsBuilder builder = new TagsBuilder( client, this, config() );
      builder.build();
      opcUaNodesReader = new NodesReader( client, builder.getSyncTags(), builder.getAsyncTags() );
      opcUaNodesWriter = new NodesWriter( client, builder.getOutputTags() );

      IAvTree groupsConfig = config().nodes().findByKey( GROUPS_PARAM_NAME );

      opcUaNodesReader.config( groupsConfig );
      opcUaNodesWriter.config( groupsConfig );

      IStridablesList<L2AbstractTag> readTags = opcUaNodesReader.getTags();
      IStridablesList<L2AbstractTag> writeTags = opcUaNodesWriter.getTags();

      for( L2AbstractTag writeTag : writeTags ) {
        // просто добавить все теги на запись в список тегов
        tags.put( writeTag );
      }

      // просто добавить все теги на чтение в список тегов
      for( L2AbstractTag readTag : readTags ) {
        tags.put( readTag );
      }
      // dima 04.09.25 сразу прочитаем все теги на чтение
      // без следующих 2-х строчек кода на этапе инициализаци DLM все синхронные теги !isAssigned()
      opcUaNodesReader.readValuesFromNodes();
      opcUaNodesReader.readValuesFromBuffer();

      // список тегов печать
      IList<String> tagsKeys = tags.keys();
      for( String tagKey : tagsKeys ) {
        IL2Tag tag = tags.getByKey( tagKey );
        ERwKind tagKind = tag.kind();

        boolean isRead = (tagKind == ERwKind.R || tagKind == ERwKind.RW);
        String value = isRead ? "null" : new String();
        if( isRead && tag.get() != null ) {
          value = "Not assigned";
          if( tag.get().isAssigned() ) {
            value = tag.get().asString();
          }
        }

        logger.info( "Tag '%s' of type %s, value = %s", tagKey, tagKind.name(), value );
      }
    }
    catch( Exception ex ) {
      logger.error( "Global ua cfg driver exception: '%s'", ex.getMessage() );
    }
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

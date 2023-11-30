package org.toxsoft.l2.thd.opc.ua.milo.test;

import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.function.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.sdk.client.api.config.*;
import org.eclipse.milo.opcua.sdk.client.api.identity.*;
import org.eclipse.milo.opcua.sdk.client.nodes.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.thd.opc.ua.milo.*;

/**
 * Тест непрерывного соединения с сервером opc ua
 *
 * @author max
 */
public class TestContinuousOpcUaConnection {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

  private OpcUaClient client;

  private NodeId nodeId;

  public TestContinuousOpcUaConnection() {
    // Слежение за файлом конфигурации журнала log4j.xml
    LoggerWrapper.setScanPropertiesTimeout( 10000 );
    nodeId = createNodeId();
    try {
      client = createClient();
      client.connect().get();

      testRead();

      client.disconnect().get();
    }
    catch( Exception ex ) {
      ex.printStackTrace();
    }
  }

  private void testRead() {
    logger.info( "\n\nTest programm started %s", TIME_FORMAT.format( new Date() ) );

    long lastDisconnectionTime = 0;

    long lastTimeOfAliveMessage = 0;

    for( int i = 0;; i++ ) {
      String timeMessage =
          lastDisconnectionTime > 0
              ? String.format( ", last time of disconnetion is %s",
                  TIME_FORMAT.format( new Date( lastDisconnectionTime ) ) )
              : "";
      try {
        UaVariableNode dNode = client.getAddressSpace().getVariableNode( nodeId );
        DataValue dValue = dNode.readValue();
        logger.debug( "Node %s, Value=%s%s", dNode.getNodeId().toParseableString(),
            dValue.getValue().getValue().toString(), timeMessage );
      }
      catch( Exception e ) {
        lastDisconnectionTime = System.currentTimeMillis();
        logger.error( e );
      }
      try {
        Thread.sleep( 1000L );
      }
      catch( InterruptedException ex ) {
        // nop
      }

      if( System.currentTimeMillis() - lastTimeOfAliveMessage > 3L * 1000L * 60L ) {
        logger.info( "I am alive%s", timeMessage );
        lastTimeOfAliveMessage = System.currentTimeMillis();
      }
    }

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
    // return "opc.tcp://localhost:12686/milo";
    return "opc.tcp://192.168.153.1:4850"; // poligon
    // return "opc.tcp://localhost:12686/milo";
    // return "opc.tcp://192.168.0.19:4840";
  }

  Predicate<EndpointDescription> endpointFilter() {
    return e -> true;
  }

  IdentityProvider getIdentityProvider() {
    return new AnonymousProvider();
  }

  NodeId createNodeId() {
    return NodeId.parse( "" );
  }

  public static void main( String[] a ) {

    String host = a.length > 0 ? a[0] : // "opc.tcp://192.168.0.19:4840";
        "opc.tcp://192.168.153.1:4850"; // poligon
    // "opc.tcp://localhost:12686/milo";//milo

    String testNodeId = a.length > 1 ? a[1] : "";

    String user = a.length > 2 ? a[1] : new String();
    String pass = a.length > 3 ? a[2] : new String();

    new TestContinuousOpcUaConnection() {

      @Override
      NodeId createNodeId() {
        return NodeId.parse( testNodeId );
      }

      @Override
      String getEndpointUrl() {
        // return "opc.tcp://192.168.153.1:4850"; // poligon
        return host;
      }

      // @Override
      @Override
      IdentityProvider getIdentityProvider() {
        if( user.length() > 0 ) {
          // return new UsernameProvider("admin","123"); //poligon
          return new UsernameProvider( user, pass );
        }

        return new AnonymousProvider();
      }
    };
  }
}

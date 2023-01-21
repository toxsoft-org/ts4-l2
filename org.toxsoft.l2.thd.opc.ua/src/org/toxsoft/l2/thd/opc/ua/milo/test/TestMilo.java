package org.toxsoft.l2.thd.opc.ua.milo.test;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.sdk.client.api.config.*;
import org.eclipse.milo.opcua.sdk.client.api.identity.*;
import org.eclipse.milo.opcua.sdk.client.nodes.*;
import org.eclipse.milo.opcua.stack.core.*;
import org.eclipse.milo.opcua.stack.core.security.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.eclipse.milo.opcua.stack.core.util.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.thd.opc.ua.milo.*;

import com.google.common.collect.*;

public class TestMilo {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  private OpcUaClient client;

  // private String host = new String();
  // private String user = new String();
  // private String pass = new String();

  //
  // ---------------------------------
  // test

  private TestMilo() {

    try {
      client = createClient();
      client.connect().get();

      // testRead();
      // testBrowse();
      testWrite();

      client.disconnect().get();
    }
    catch( Exception ex ) {
      ex.printStackTrace();
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
    return "opc.tcp://localhost:12686/milo";
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
    return new AnonymousProvider();
  }

  private void testWrite()
      throws UaException,
      InterruptedException,
      ExecutionException {
    List<NodeId> nodeIds = ImmutableList.of( new NodeId( 3, "\"Data_block_1\".\"tempInt\"" ) );

    Variant v = new Variant( (short)27 );

    // don't write status or timestamps
    DataValue dv = new DataValue( v, null, null );

    // write asynchronously....
    CompletableFuture<List<StatusCode>> f = client.writeValues( nodeIds, ImmutableList.of( dv ) );

    // ...but block for the results so we write in order
    List<StatusCode> statusCodes = f.get();
    StatusCode status = statusCodes.get( 0 );

    if( status.isGood() ) {
      System.out.println( String.format( "Wrote '%s' to nodeId=%s", v, nodeIds.get( 0 ) ) );
    }

  }

  private void testRead()
      throws UaException,
      InterruptedException {
    // poligon test
    // UaVariableNode dNode = client.getAddressSpace().getVariableNode(new NodeId(2,96));
    // UaVariableNode dNode = client.getAddressSpace().getVariableNode( new NodeId( 1, 71 ) );

    UaVariableNode dNode =
        client.getAddressSpace().getVariableNode( new NodeId( 3, "\"Data_block_1\".\"pulse05Hz\"" ) );

    // local test UaVariableNode dNode = client.getAddressSpace().getVariableNode(new
    // NodeId(2,"HelloWorld/Dynamic/Double"));
    // Ge Test UaVariableNode dNode = client.getAddressSpace().getVariableNode(new NodeId(2,"TEST SERVER.PLC TAGS FOR
    // OPC SERVER.COMPRESSOR ON"));
    // UaVariableNode dNode = client.getAddressSpace().getVariableNode(new NodeId(2,"COMPRESSOR ON"));

    for( int i = 0; i < 10; i++ ) {
      DataValue dValue = dNode.readValue();
      System.out.println( String.format( "Node %s Value=%s", dNode.getNodeId().toParseableString(),
          dValue.getValue().getValue().toString() ) );
      Thread.sleep( 1777L );
    }

  }

  private void testBrowse() {
    // start browsing at root folder
    browseNode2( "", client, new NodeId( 3, "\"Data_block_1\"" ) );// Identifiers.RootFolder );
    // browseNode2( "", client, Identifiers.RootFolder );
  }

  private void browseNode( String indent, OpcUaClient client, NodeId browseRoot ) {
    BrowseDescription browse = new BrowseDescription( browseRoot, BrowseDirection.Forward, Identifiers.References, true,
        Unsigned.uint( NodeClass.Object.getValue() | NodeClass.Variable.getValue() ),
        Unsigned.uint( BrowseResultMask.All.getValue() ) );

    try {
      BrowseResult browseResult = client.browse( browse ).get();

      List<ReferenceDescription> references = ConversionUtil.toList( browseResult.getReferences() );

      for( ReferenceDescription rd : references ) {
        logger.info( "%s Node=%s", indent, rd.getBrowseName().toString() );
        logger.info( "%s NodE=  %s", indent, rd.getNodeId() );// .toParseableString());
        System.out.println( String.format( "%s NodE=  %s | %s", indent, rd.getNodeId().toParseableString(),
            rd.getTypeId().toParseableString() ) );
        // recursively browse to children
        rd.getNodeId().toNodeId( client.getNamespaceTable() )
            .ifPresent( nodeId -> browseNode( indent + "  ", client, nodeId ) );
      }
    }
    catch( InterruptedException |

        ExecutionException e ) {
      logger.error( "Browsing nodeId=%s failed: %s", browseRoot, e.getMessage(), e );
    }
  }

  private void browseNode2( String indent, OpcUaClient client, NodeId browseRoot ) {
    try {
      List<? extends UaNode> nodes = client.getAddressSpace().browseNodes( browseRoot );

      for( UaNode node : nodes ) {
        logger.info( "%s Node=%s (%s) [%s]", indent, node.getNodeId().toParseableString(),
            node.getDisplayName().getText(), node.getClass().getName() ); // getBrowseName());//.getName());

        System.out.println( String.format( "%s Node=%s", indent, node.getNodeId().toParseableString() ) );

        if( node instanceof UaVariableNode vNode ) {
          System.out.println( String.format( "%s NodeType=%s", indent, vNode.getTypeDefinition().getDisplayName() ) );
        }

        // recursively browse to children
        browseNode2( indent + "  ", client, node.getNodeId() );
      }
    }
    catch( UaException e ) {
      logger.error( "Browsing nodeId=%s failed: %s", browseRoot, e.getMessage(), e );
    }
  }

  public static void main( String[] a ) {

    String host = a.length > 0 ? a[0] : "opc.tcp://192.168.0.11:4840";
    // "opc.tcp://192.168.153.1:4850"; // poligon
    // "opc.tcp://localhost:12686/milo";//milo

    String user = a.length > 1 ? a[1] : new String();
    String pass = a.length > 2 ? a[2] : new String();

    new TestMilo() {

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

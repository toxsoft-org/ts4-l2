package ge.toxsoft.gwp.opcuabridge;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.stack.core.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.l2.thd.opc.ua.milo.*;

/**
 * Thread for server level processing (read, write from/on sk server)
 *
 * @author max
 */
public class OpcUaBridge
    implements IProcessingModel {

  private IProcessingModel dataModule;

  private IProcessingModel cmdModule;

  private IProcessingModel eventModule;

  private NodesReader opcUaNodesReader;

  private NodesWriter opcUaNodesWriter;

  private OpcUaThread l2Tread;

  private volatile boolean isRun = false;

  public OpcUaBridge() {
    OpcUaClient client = new OpcUaClient( null, null );// TODO
    opcUaNodesReader = new NodesReader( client );
    opcUaNodesWriter = new NodesWriter( client );

    l2Tread = new OpcUaThread( opcUaNodesReader, opcUaNodesWriter );

  }

  @Override
  public void config( IAvTree aCfgInfo ) {
    try {
      opcUaNodesReader.config( aCfgInfo );
      opcUaNodesWriter.config( aCfgInfo );
    }
    catch( UaException ex ) {
      // TODO Auto-generated catch block

    }

    dataModule.config( aCfgInfo );
    cmdModule.config( aCfgInfo );
    eventModule.config( aCfgInfo );
  }

  @Override
  public void start() {
    isRun = true;

    Thread opcL2Thread = new Thread( l2Tread );
    opcL2Thread.run();

    dataModule.start();
    cmdModule.start();
    eventModule.start();
  }

  @Override
  public void job() {

    if( isRun ) {

      opcUaNodesReader.readValuesFromBuffer();

      eventModule.job();

      dataModule.job();

      cmdModule.job();

      opcUaNodesWriter.writeValuesToBuffer();

    }

  }

  @Override
  public void stop() {
    isRun = false;

    eventModule.stop();

    dataModule.stop();

    cmdModule.stop();

    l2Tread.stopRunning();

  }

  @Override
  public boolean isStopped() {
    return eventModule.isStopped() && dataModule.isStopped() && cmdModule.isStopped();// && l2Tread.;

  }
}

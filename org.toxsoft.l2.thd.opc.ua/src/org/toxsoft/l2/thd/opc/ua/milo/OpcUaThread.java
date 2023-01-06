package org.toxsoft.l2.thd.opc.ua.milo;

import java.util.concurrent.*;

import org.toxsoft.core.tslib.utils.logs.impl.*;

/**
 * Thread for low level processing (read, write from/on opc ua server)
 *
 * @author max
 */
public class OpcUaThread
    implements Runnable {

  private NodesReader opcUaNodesReader;

  private NodesWriter opcUaNodesWriter;

  private volatile boolean isRun = false;

  public OpcUaThread( NodesReader aOpcUaNodesReader, NodesWriter aOpcUaNodesWriter ) {
    opcUaNodesReader = aOpcUaNodesReader;
    opcUaNodesWriter = aOpcUaNodesWriter;
  }

  @Override
  public void run() {
    isRun = true;

    while( isRun ) {

      try {
        opcUaNodesReader.readValuesFromNodes();
        opcUaNodesWriter.writeValuesToNodes();
      }
      catch( InterruptedException | ExecutionException ex ) {
        // TODO Auto-generated catch block
        LoggerUtils.errorLogger().error( ex );
      }

      try {
        Thread.sleep( 100L );
      }
      catch( InterruptedException ex ) {
        // TODO Auto-generated catch block
        LoggerUtils.errorLogger().error( ex );
      }
    }
  }

  public void stopRunning() {
    isRun = false;
  }

}

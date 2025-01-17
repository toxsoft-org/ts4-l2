package org.toxsoft.l2.thd.modbus.common.test;

import java.util.*;

import gnu.io.*;

public class PortsTest {

  public static void main( String[] args ) {
    System.setProperty( "gnu.io.rxtx.SerialPorts", "/dev/ttyUSB0" );
    Enumeration portList = CommPortIdentifier.getPortIdentifiers();
    while( portList.hasMoreElements() ) {
      CommPortIdentifier portId = (CommPortIdentifier)portList.nextElement();
      /* only iterate through serial ports */ if( portId.getPortType() == CommPortIdentifier.PORT_SERIAL ) {
        /* attempt to open the port */
        try {
          if( portId.isCurrentlyOwned() ) {
            System.out.println( portId.getName() + " is owned" );
          }
          else {
            System.out.println( portId.getName() + " is not   owned" );
          }
          SerialPort serialPort = (SerialPort)portId.open( "Square2Plus", 2000 );
        }
        catch( PortInUseException e ) { // if the port is unavailable, move to the next one
          System.out.println( portId.getName() + " is owned by " + portId.getCurrentOwner() );
          continue;
        }
      }
    }
  }
}

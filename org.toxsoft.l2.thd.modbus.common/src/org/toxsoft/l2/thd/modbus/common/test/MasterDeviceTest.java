package org.toxsoft.l2.thd.modbus.common.test;

import java.net.InetAddress;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

public class MasterDeviceTest {

   private static String address = "192.168.2.101";

 // private static String address = "127.0.0.1";

  public static void main( String[] args )
      throws Exception {
    TCPMasterConnection con = openConnection();

    // формирование запроса

    ReadInputRegistersRequest cr = new ReadInputRegistersRequest( 0, 1 );

    cr.setUnitID( 5 );

    // транзакция
    ModbusTCPTransaction trans = new ModbusTCPTransaction( con );
    trans.setRequest( cr );

    for( int i = 0; i < 100; i++ ) {

      // испонение транзакции
      try {
        trans.execute();

        ReadInputRegistersResponse res = (ReadInputRegistersResponse)trans.getResponse();
        int val = res.getRegister( 0 ).getValue();

        System.out.println("* "+ val );

      }
      catch( Exception e ) {
        e.printStackTrace();
      }
      
      Thread.sleep( 7000 );
    }
  }

  public static TCPMasterConnection openConnection()
      throws Exception {
    InetAddress addr = InetAddress.getByName( address );

    TCPMasterConnection retVal = new TCPMasterConnection( addr );

    retVal.setPort( 1500 );
    retVal.connect();
    System.out.println( "Connection established" );
    return retVal;
  }
}

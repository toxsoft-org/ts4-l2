package org.toxsoft.l2.thd.modbus.common.test;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleDigitalOut;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

public class SlaveDeviceTest {

  public static void main(String[] args) {
    ModbusTCPListener listener = null;
    
    int port = 1500;// Modbus.DEFAULT_PORT;
    try {
        if (args != null && args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("jModbus Modbus Slave (Server)");
        setImage(5);
        // 3. create a listener with 3 threads in pool
        if (Modbus.debug) {
            System.out.println("Listening...");
        }
        listener = new ModbusTCPListener(3);
        listener.setPort(port);
        listener.start();
        
        for(int i = 10 ; i < 100 ; i++){
          
          Thread.sleep( 5000 );
          int c = i*100;
          setImage(c);
          System.out.println( c );
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
  
  static void setImage(int aVal){
 // 1. prepare a process image
    SimpleProcessImage   spi = new SimpleProcessImage();
    spi.addDigitalOut(new SimpleDigitalOut(true));
    spi.addDigitalOut(new SimpleDigitalOut(true));
    spi.addDigitalIn(new SimpleDigitalIn(false));
    spi.addDigitalIn(new SimpleDigitalIn(true));
    spi.addDigitalIn(new SimpleDigitalIn(false));
    spi.addDigitalIn(new SimpleDigitalIn(true));
    // allow checking LSB/MSB order
    spi.addDigitalIn(new SimpleDigitalIn(true));
    spi.addDigitalIn(new SimpleDigitalIn(true));
    spi.addDigitalIn(new SimpleDigitalIn(true));
    spi.addDigitalIn(new SimpleDigitalIn(true));
    spi.addRegister(new SimpleRegister(251));
    spi.addInputRegister(new SimpleInputRegister(aVal));
    // 2. create the coupler holding the image
    ModbusCoupler.getReference().setProcessImage(spi);
    ModbusCoupler.getReference().setMaster(false);
    ModbusCoupler.getReference().setUnitID(6);
    
    
  }

}

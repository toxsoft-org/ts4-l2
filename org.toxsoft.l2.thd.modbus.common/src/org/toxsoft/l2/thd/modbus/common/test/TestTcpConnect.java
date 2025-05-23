package org.toxsoft.l2.thd.modbus.common.test;

import static org.toxsoft.l2.thd.modbus.common.IModbusThdConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;
import org.toxsoft.l2.thd.modbus.common.connections.*;

import net.wimpi.modbus.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.procimg.*;

public class TestTcpConnect {

  private static ITransactionCreator transactionCreator;

  public static void main( String[] args ) {

    transactionCreator = new TcpTransactionCreator();

    OptionSet optSet = new OptionSet();
    // optSet.setStr( IP_PARAM_ID, "192.168.0.123" ); // OWEN ВК№1
    // optSet.setStr( IP_PARAM_ID, "192.168.0.124" ); // OWEN ВК№2
    // optSet.setStr( IP_PARAM_ID, "192.168.0.125" ); // OWEN ВК№3
    // optSet.setStr( IP_PARAM_ID, "192.168.0.126" ); // OWEN ВК№4
    // optSet.setStr( IP_PARAM_ID, "192.168.0.222" ); // БРВ №3 ВКМ 360А ссука!
    optSet.setStr( IP_PARAM_ID, "192.168.0.220" ); // payload
    // optSet.setStr( IP_PARAM_ID, "192.168.0.132" ); // газодувки, регистратор новый
    // optSet.setStr( IP_PARAM_ID, "192.168.0.139" ); // АК №7
    // optSet.setStr( IP_PARAM_ID, "192.168.0.140" ); // АК №8
    // optSet.setStr( IP_PARAM_ID, "192.168.0.141" ); // АК №11
    // optSet.setStr( IP_PARAM_ID, "192.168.0.142" ); // АК №13
    // optSet.setStr( IP_PARAM_ID, "192.168.0.129" ); // БРВ №2
    // optSet.setStr( IP_PARAM_ID, "192.168.0.147" ); // рег. № 1225
    // optSet.setStr( IP_PARAM_ID, "192.168.0.131" ); // рег. № 1240 Элметро в АКО
    optSet.setInt( PORT_PARAM_ID, 502 );

    transactionCreator.config( optSet );

    String cmd = args[0];
    int adress = 1;
    int reg = Integer.parseInt( args[1] );
    // int reg = Integer.parseInt( args[1].substring( 2 ), 16 );
    int regVal = Integer.parseInt( args[2] );

    try {
      switch( cmd ) {
        case "do":
          sendDo( adress, reg, regVal > 0 );
          break;
        case "rdo":
          readDo( adress, reg, regVal );
          break;
        case "ao":
          sendRequaest( adress, reg, regVal );
          break;
        case "mao":
          sendMultiRequaest( adress, reg, regVal );
          break;
        case "rao":
          readAOReg( adress, reg, regVal );
          break;
        case "ai":
          // 0 is first register
          // owenReadAIReg( 1, 0, 13 );
          // rmt59ReadAIReg( 1, 0x500, 9 );
          // rmt79ReadAIReg( 1, 0x4000, 4 );
          vkm360ReadAIReg( 1, 4000, 2 );
          // elmetroReadAIReg( 1, 0, 3 );
          break;

        case "di":
          readDi( adress, reg, regVal );
          break;
      }

    }
    catch( Exception e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    transactionCreator.close();
  }

  /**
   * Чтение выходных дискретных регистров INPUT STATUS #2
   *
   * @param aAdress device Id
   * @param aReg адрес начального регистра
   * @param aCount кол-во регистров для чтения
   */

  public static void readDi( int aAdress, int aReg, int aCount ) {
    // формирование запроса
    ReadInputDiscretesRequest cr = new ReadInputDiscretesRequest( aReg, aCount );
    cr.setUnitID( aAdress );
    // cr.setHeadless();

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Read " + aAdress );
      ModbusResponse r = trans.getResponse();
      for( int i = 0; i < aCount; i++ ) {
        System.out.println( "Response val: " + ((ReadInputDiscretesResponse)r).getDiscreteStatus( i ) );
      }
    }
    catch( ModbusException e ) {
      System.out.println( "Havnt read " + aAdress );
      e.printStackTrace();
    }

  }

  /**
   * Чтение состояния аналоговых выходов HOLDING REGISTER #3 (Read Holding Registers)
   *
   * @param aAdress device Id
   * @param aReg адрес начального регистра
   * @param aCount кол-во регистров для чтения
   */
  public static void readAOReg( int aAdress, int aReg, int aCount ) {
    // формирование запроса
    // ReadMultipleRegistersRequest cr = new ReadMultipleRegistersRequest( aReg, aCount );
    // cr.setUnitID( aAdress );
    // // cr.setHeadless();
    //
    // // транзакция
    // ModbusTransaction trans = transactionCreator.createModbusTransaction();
    // trans.setRequest( cr );
    //
    // // испонение транзакции
    // try {
    // trans.execute();
    // System.out.println( "Read " + aAdress );
    // ModbusResponse r = trans.getResponse();
    // for( int i = 0; i < aCount; i++ ) {
    // System.out.println( "Response val: " + ((ReadMultipleRegistersResponse)r).getRegisterValue( i ) );
    // }
    // }
    // catch( ModbusException e ) {
    // System.out.println( "Havnt read " + aAdress );
    // e.printStackTrace();
    // }
    for( int ri = 0; ri < aCount; ri++ ) {
      int currReg = aReg + 3 * ri;

      // формирование запроса
      ReadMultipleRegistersRequest cr = new ReadMultipleRegistersRequest( currReg, 3 );
      cr.setUnitID( aAdress );
      // cr.setHeadless();

      // транзакция
      ModbusTransaction trans = transactionCreator.createModbusTransaction();
      trans.setRequest( cr );

      // испонение транзакции
      try {
        trans.execute();
        System.out.println( "\nPen №" + (ri + 1) + " read reg = " + Integer.toHexString( currReg ) );
        ModbusResponse r = trans.getResponse();
        byte[] hiBytes = ((ReadMultipleRegistersResponse)r).getRegister( 0 ).toBytes();
        byte[] lowBytes = ((ReadMultipleRegistersResponse)r).getRegister( 1 ).toBytes();
        int penError = ((ReadMultipleRegistersResponse)r).getRegister( 2 ).getValue();
        byte[] bytes = new byte[4];
        bytes[0] = hiBytes[0];
        bytes[1] = hiBytes[1];
        bytes[2] = lowBytes[0];
        bytes[3] = lowBytes[1];
        float val = registersToFloat( bytes );
        System.out.println( "Pen float val: " + val );
        System.out.println( "Pen error: " + penError );
      }
      catch( ModbusException e ) {
        System.out.println( "Havnt read " + aAdress );
        e.printStackTrace();
      }
    }
  }

  /**
   * Чтение выходных аналоговых регистров ВКМ 360
   *
   * @param aAdress
   * @param aReg
   */
  public static void vkm360ReadAIReg( int aAdress, int aReg, int aCount ) {
    // aReg = 2028;
    // aCount = 3;
    for( int ri = 0; ri < aCount; ri++ ) {
      int currReg = aReg + 4 * ri;
      // int currReg = aReg + 100 * ri;
      // формирование запроса FC4
      ReadInputRegistersRequest cr = new ReadInputRegistersRequest( currReg, 3 );
      cr.setUnitID( aAdress );
      // cr.setHeadless();

      // транзакция
      ModbusTransaction trans = transactionCreator.createModbusTransaction();
      trans.setRequest( cr );

      // испоkнение транзакции
      try {
        System.out.println( "\nLogical input №" + (ri + 1) + " read reg = " + currReg );
        trans.execute();

        ModbusResponse r = trans.getResponse();
        byte[] hiBytes = ((ReadInputRegistersResponse)r).getRegister( 0 ).toBytes();
        byte[] lowBytes = ((ReadInputRegistersResponse)r).getRegister( 1 ).toBytes();
        byte[] bytes = new byte[4];
        bytes[0] = hiBytes[0];
        bytes[1] = hiBytes[1];
        bytes[2] = lowBytes[0];
        bytes[3] = lowBytes[1];
        float val = registersToFloat( bytes );
        System.out.println( "Response float val in hour: " + val * 3600 );
        System.out.println( "Response float val: " + val );
        // тут печатаем код состояния канала
        int penErr = ((ReadInputRegistersResponse)r).getRegisterValue( 2 );
        System.out.println( "penErr : " + penErr );
      }
      catch( ModbusException e ) {
        System.out.println( "Havnt read " + aAdress );
        e.printStackTrace();
      }
    }
  }

  public static void owenReadAIReg( int aAdress, int aReg, int aCount ) {
    for( int ri = 0; ri < aCount; ri++ ) {
      int currReg = aReg + 4 * ri;

      // формирование запроса FC3
      ReadMultipleRegistersRequest cr = new ReadMultipleRegistersRequest( currReg, 3 );
      cr.setUnitID( aAdress );
      // cr.setHeadless();

      // транзакция
      // long t1 = System.currentTimeMillis();
      ModbusTransaction trans = transactionCreator.createModbusTransaction();
      trans.setRequest( cr );
      // System.out.println( "time1: " + (System.currentTimeMillis() - t1) );

      // исполнение транзакции
      try {
        System.out.println( "\nChannel №" + (ri + 1) + " read reg = " + Integer.toHexString( currReg ) );
        // t1 = System.currentTimeMillis();
        trans.execute();
        // System.out.println( "time2: " + (System.currentTimeMillis() - t1) );

        ModbusResponse r = trans.getResponse();
        // byte[] hiBytes = ((ReadMultipleRegistersResponse)r).getRegister( 0 ).toBytes();
        // byte[] lowBytes = ((ReadMultipleRegistersResponse)r).getRegister( 1 ).toBytes();
        // int penError = ((ReadMultipleRegistersResponse)r).getRegister( 2 ).getValue();
        // byte[] bytes = new byte[4];
        // bytes[0] = hiBytes[0];
        // bytes[1] = hiBytes[1];
        // bytes[2] = lowBytes[0];
        // bytes[3] = lowBytes[1];
        // float val = registersToFloat( bytes );
        // System.out.println( "Pen float val: " + val );
        // System.out.println( "Pen error: " + penError );
        int[] ints = new int[2];
        ints[0] = ((ReadMultipleRegistersResponse)r).getRegisterValue( 0 );
        ints[1] = ((ReadMultipleRegistersResponse)r).getRegisterValue( 1 );
        IAtomicValue fval = translateCDAB( ints );
        System.out.println( "channel float fval CDAB: " + fval );
        // fval = translateABCD( ints );
        // System.out.println( "channel float fval ABCD: " + fval );
        // тут печатаем код состояния канала
        int penErr = ((ReadMultipleRegistersResponse)r).getRegisterValue( 2 );
        System.out.println( "penErr : " + penErr );
      }
      catch( ModbusException e ) {
        System.out.println( "Havnt read " + aAdress );
        e.printStackTrace();
      }
    }
  }

  public static void rmt59ReadAIReg( int aAdress, int aReg, int aCount ) {
    for( int ri = 0; ri < aCount; ri++ ) {
      int currReg = aReg + 3 * ri;

      // формирование запроса FC3
      ReadMultipleRegistersRequest cr = new ReadMultipleRegistersRequest( currReg, 3 );
      cr.setUnitID( aAdress );
      // cr.setHeadless();

      // транзакция
      ModbusTransaction trans = transactionCreator.createModbusTransaction();
      trans.setRequest( cr );

      // исполнение транзакции
      try {
        System.out
            .println( "\nPen №" + (ri + 1) + " read reg = " + Integer.toHexString( currReg ) + " , count = " + 3 );
        trans.execute();

        ModbusResponse r = trans.getResponse();
        // byte[] hiBytes = ((ReadMultipleRegistersResponse)r).getRegister( 0 ).toBytes();
        // byte[] lowBytes = ((ReadMultipleRegistersResponse)r).getRegister( 1 ).toBytes();
        // int penError = ((ReadMultipleRegistersResponse)r).getRegister( 2 ).getValue();
        // byte[] bytes = new byte[4];
        // bytes[0] = hiBytes[0];
        // bytes[1] = hiBytes[1];
        // bytes[2] = lowBytes[0];
        // bytes[3] = lowBytes[1];
        // float val = registersToFloat( bytes );
        // System.out.println( "Pen float val: " + val );
        // System.out.println( "Pen error: " + penError );
        int[] ints = new int[2];
        ints[0] = ((ReadMultipleRegistersResponse)r).getRegisterValue( 0 );
        ints[1] = ((ReadMultipleRegistersResponse)r).getRegisterValue( 1 );
        IAtomicValue fval = translateCDAB( ints );
        // System.out.println( "channel float fval CDAB: " + fval );
        fval = translateABCD( ints );
        System.out.println( "channel float fval ABCD: " + fval );
        // тут печатаем код состояния канала
        int penErr = ((ReadMultipleRegistersResponse)r).getRegisterValue( 2 );
        System.out.println( "penErr : " + penErr );
      }
      catch( ModbusException e ) {
        System.out.println( "Havnt read " + aAdress );
        e.printStackTrace();
      }
    }
  }

  public static void rmt79ReadAIReg( int aAdress, int aReg, int aCount ) {
    for( int ri = 0; ri < 29; ri++ ) {
      int currReg = aReg + 4 * ri;
      // формирование запроса FC4
      ReadInputRegistersRequest cr = new ReadInputRegistersRequest( currReg, aCount );
      cr.setUnitID( aAdress );
      // cr.setHeadless();

      // транзакция
      ModbusTransaction trans = transactionCreator.createModbusTransaction();
      trans.setRequest( cr );

      // исполнение транзакции
      try {
        // System.out.println( "Канал №" + (ri + 1) + " Read reg = " + Integer.toHexString( currReg ) );
        System.out.println( "Канал №" + (ri + 1) + " Read reg = " + currReg );
        trans.execute();

        ModbusResponse r = trans.getResponse();

        int[] ints = new int[2];
        ints[0] = ((ReadInputRegistersResponse)r).getRegisterValue( 0 );
        ints[1] = ((ReadInputRegistersResponse)r).getRegisterValue( 1 );
        // IAtomicValue fval = translateCDAB( ints );
        // System.out.println( "channel float fval CDAB: " + fval );
        IAtomicValue fval = translateABCD( ints );
        System.out.println( "channel float fval ABCD: " + fval );
        // тут печатаем код состояния канала
        int channelState = ((ReadInputRegistersResponse)r).getRegisterValue( 2 );
        System.out.println( "channelState val: " + channelState );
        // тут печатаем регистр десятичной точки в десятичном формате
        // int decPointLocation = ((ReadInputRegistersResponse)r).getRegisterValue( 3 );
        // System.out.println( "decimal PointLocation val: " + decPointLocation );
        // ReadInputRegistersResponse res = (ReadInputRegistersResponse)trans.getResponse();
        // InputRegister[] regs = res.getRegisters();
        // System.out.println( "start reg = " + aReg + ",regs count = " + aCount ); //$NON-NLS-1$ //$NON-NLS-2$
        // int[] inputMassive = new int[regs.length];
        //
        // for( int j = 0; j < inputMassive.length; j++ ) {
        // inputMassive[j] = regs[j].getValue();
        //
        // System.out.print( regs[j].getValue() + "," ); //$NON-NLS-1$
        // }
        // IAtomicValue result = translate( inputMassive );
        // System.out.print( result );
      }
      catch( ModbusException e ) {
        System.out.println( "Havnt read " + aAdress );
        e.printStackTrace();
      }
    }
  }

  /**
   * Чтение выходных аналоговых регистров INPUT REGISTER #4
   *
   * @param aAdress device Id
   * @param aReg адрес начального регистра
   * @param aCount кол-во регистров для чтения
   */
  public static void elmetroReadAIReg( int aAdress, int aReg, int aCount ) {
    // ReadInputRegistersRequest cr = new ReadInputRegistersRequest( aReg, aCount );
    // // ReadInputRegistersRequest cr = new ReadInputRegistersRequest( 0x0C, 2 );
    // cr.setUnitID( aAdress );
    // // cr.setHeadless();
    //
    // // транзакция
    // ModbusTransaction trans = transactionCreator.createModbusTransaction();
    // trans.setRequest( cr );
    //
    // // испонение транзакции
    // try {
    // System.out.println( "Read reg = " + Integer.toHexString( aReg ) + " , count = " + aCount );
    // trans.execute();
    //
    // ModbusResponse r = trans.getResponse();
    // for( int i = 0; i < aCount; i++ ) {
    // int resp = ((ReadInputRegistersResponse)r).getRegisterValue( i );
    // System.out.println( "Response val: " + resp );
    // }
    // }
    // catch( ModbusException e ) {
    // System.out.println( "Havnt read " + aAdress );
    // e.printStackTrace();
    // }

    for( int ri = 0; ri < aCount; ri++ ) {
      int currReg = aReg + 2 * ri;
      // формирование запроса
      ReadInputRegistersRequest cr = new ReadInputRegistersRequest( currReg, 2 );
      // ReadInputRegistersRequest cr = new ReadInputRegistersRequest( aReg, aCount );
      // ReadInputRegistersRequest cr = new ReadInputRegistersRequest( 0x0C, 1 );
      // ReadInputRegistersRequest cr = new ReadInputRegistersRequest( 4012, 4 );
      cr.setUnitID( aAdress );
      // cr.setHeadless();

      // транзакция
      ModbusTransaction trans = transactionCreator.createModbusTransaction();
      trans.setRequest( cr );

      // испонение транзакции
      try {
        System.out.println( "\nChannel №" + (ri + 1) + " read reg = " + Integer.toHexString( currReg ).toUpperCase() );
        trans.execute();

        ModbusResponse r = trans.getResponse();
        // for( int i = 0; i < 4; i++ ) {
        // int resp = ((ReadInputRegistersResponse)r).getRegisterValue( i );
        // System.out.println( "Response val: " + resp );
        // }
        // int status = ((ReadInputRegistersResponse)r).getRegisterValue( 2 );
        // System.out.println( "channel status: " + status );
        // int decPointLoc = ((ReadInputRegistersResponse)r).getRegisterValue( 3 );
        // System.out.println( "channel decimal point location: " + decPointLoc );

        // working version
        byte[] hiBytes = ((ReadInputRegistersResponse)r).getRegister( 0 ).toBytes();
        byte[] lowBytes = ((ReadInputRegistersResponse)r).getRegister( 1 ).toBytes();
        byte[] bytes = new byte[4];
        // Elmetro low/hi
        bytes[2] = hiBytes[0];
        bytes[3] = hiBytes[1];
        bytes[0] = lowBytes[0];
        bytes[1] = lowBytes[1];
        // classic hi/low
        // bytes[0] = hiBytes[0];
        // bytes[1] = hiBytes[1];
        // bytes[2] = lowBytes[0];
        // bytes[3] = lowBytes[1];
        float val = registersToFloat( bytes );

        // IAtomicValue val = translate( bytes );
        System.out.println( "channel float val: " + val );

        int[] ints = new int[2];
        ints[0] = ((ReadInputRegistersResponse)r).getRegister( 0 ).getValue();
        ints[1] = ((ReadInputRegistersResponse)r).getRegister( 1 ).getValue();
        IAtomicValue fval = translateCDAB( ints );
        System.out.println( "channel float fval CDAB: " + fval );
        fval = translateABCD( ints );
        System.out.println( "channel float fval ABCD: " + fval );
        // тут печатаем код состояния канала
        // int channelState = ((ReadInputRegistersResponse)r).getRegisterValue( 2 );
        // System.out.println( "channelState val: " + channelState );
        // тут печатаем регистр десятичной точки в десятичном формате
        // int decPointLocation = ((ReadInputRegistersResponse)r).getRegisterValue( 3 );
        // System.out.println( "decimal PointLocation val: " + decPointLocation );
        // ReadInputRegistersResponse res = (ReadInputRegistersResponse)trans.getResponse();
        // InputRegister[] regs = res.getRegisters();
        // System.out.println( "start reg = " + aReg + ",regs count = " + aCount ); //$NON-NLS-1$ //$NON-NLS-2$
        // int[] inputMassive = new int[regs.length];
        //
        // for( int j = 0; j < inputMassive.length; j++ ) {
        // inputMassive[j] = regs[j].getValue();
        //
        // System.out.print( regs[j].getValue() + "," ); //$NON-NLS-1$
        // }
        // IAtomicValue result = translate( inputMassive );
        // System.out.print( result );

      }
      catch( ModbusException e ) {
        System.out.println( "Havnt read " + aAdress );
        e.printStackTrace();
      }
    }
  }

  private static IAtomicValue translateCDAB( int[] aWords ) {
    if( aWords.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += aWords[i] << (16 * i);
      }
      return AvUtils.avFloat( Float.intBitsToFloat( value ) );
    }

    if( aWords.length == 4 ) {
      long value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += ((long)aWords[i]) << (16 * i);
      }
      return AvUtils.avFloat( Double.longBitsToDouble( value ) );
    }
    return AvUtils.avFloat( aWords[0] );
  }

  private static IAtomicValue translateABCD( int[] aWords ) {
    if( aWords.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += aWords[i] << (16 * (aWords.length - 1 - i));
      }
      return AvUtils.avFloat( Float.intBitsToFloat( value ) );
    }

    if( aWords.length == 4 ) {
      long value = 0;
      for( int i = 0; i < aWords.length; i++ ) {
        value += ((long)aWords[i]) << (16 * (aWords.length - 1 - i));
      }
      return AvUtils.avFloat( Double.longBitsToDouble( value ) );
    }
    return AvUtils.avFloat( aWords[0] );
  }

  public static IAtomicValue translate( int[] aBytes ) {
    if( aBytes.length == 2 ) {
      int value = 0;
      for( int i = 0; i < aBytes.length; i++ ) {
        value += aBytes[i] << (16 * (aBytes.length - 1 - i));
      }
      float floatVal = Float.intBitsToFloat( value );
      // int reg0 = aBytes[0] << 16;
      // int reg1 = aBytes[1];
      // value = reg0 + reg1;
      // float floatVal = Float.intBitsToFloat( value );
      return AvUtils.avFloat( floatVal );
    }

    if( aBytes.length == 4 ) {
      long value = 0;
      for( int i = 0; i < aBytes.length; i++ ) {
        value += ((long)aBytes[i]) << (16 * i);
      }

      return AvUtils.avFloat( Double.longBitsToDouble( value ) );
    }
    return AvUtils.avFloat( aBytes[0] );
  }

  /**
   * Converts a byte[4] binary float value to a float primitive.
   *
   * @param bytes the byte[4] containing the float value.
   * @return a float value.
   */
  public static final float registersToFloat( byte[] bytes ) {
    return Float.intBitsToFloat(
        (((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff)) );
  }// registersToFloat

  /**
   * Чтение состояния выходов реле Read Coils #1
   *
   * @param aAdress device Id
   * @param aReg адрес начального регистра
   * @param aCount кол-во регистров для чтения
   */
  public static void readDo( int aAdress, int aReg, int aCount ) {
    // формирование запроса
    ReadCoilsRequest cr = new ReadCoilsRequest( aReg, aCount );
    cr.setUnitID( aAdress );
    cr.setHeadless();

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Read " + aAdress );
      ModbusResponse r = trans.getResponse();
      for( int i = 0; i < aCount; i++ ) {
        System.out.println( "Response val: " + ((ReadCoilsResponse)r).getCoilStatus( i ) );
      }
    }
    catch( ModbusException e ) {
      System.out.println( "Havnt read " + aAdress );
      e.printStackTrace();
    }

  }

  public static void sendDo( int aAdress, int aReg, boolean aVal ) {
    // формирование запроса
    WriteCoilRequest cr = new WriteCoilRequest( aReg, aVal );
    cr.setUnitID( aAdress );

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Request sent" );
      WriteCoilResponse r = (WriteCoilResponse)trans.getResponse();
      System.out.println( "Response val: " + r.getCoil() );
    }
    catch( ModbusException e ) {
      System.out.println( "Request havnt sent" );
      e.printStackTrace();
    }

  }

  public static void sendRequaest( int aAdress, int aReg, int aVal ) {
    // формирование запроса
    WriteSingleRegisterRequest cr = new WriteSingleRegisterRequest( aReg, new SimpleRegister( aVal ) );
    cr.setUnitID( aAdress );

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Request sent" );
      WriteSingleRegisterResponse r = (WriteSingleRegisterResponse)trans.getResponse();
      System.out.println( "Response val: " + r.getRegisterValue() );
    }
    catch( ModbusException e ) {
      System.out.println( "Request havnt sent" );
      e.printStackTrace();
    }

  }

  public static void sendMultiRequaest( int aAdress, int aReg, int aVal ) {
    // формирование запроса
    WriteMultipleRegistersRequest cr =
        new WriteMultipleRegistersRequest( aReg, new Register[] { new SimpleRegister( aVal ) } );
    cr.setUnitID( aAdress );

    // транзакция
    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    trans.setRequest( cr );

    // испонение транзакции
    try {
      trans.execute();
      System.out.println( "Multi Request sent" );
      WriteMultipleRegistersResponse r = (WriteMultipleRegistersResponse)trans.getResponse();
      // System.out.println("Response val: " + r.getRegisterValue() );
    }
    catch( ModbusException e ) {
      System.out.println( "Request havnt sent" );
      e.printStackTrace();
    }

  }

}

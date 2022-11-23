package ru.toxsoft.l2.dlm.opc_bridge;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import javax.script.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;

public class ScriptTest {

  static String initScript = "var prev = null";

  public static void main( String[] args ) {
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName( JAVA_SCRIPT_NAME );
    AvUtils.avBool( false );
    try {
      engine.eval( "var myVall = Packages.ru.toxsoft.tslib.datavalue.impl.DvUtils.avBool(true)" );
      IAtomicValue value = (IAtomicValue)engine.get( "myVall" );
      System.out.println( value );
    }
    catch( ScriptException ex ) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }
  }

}

package org.toxsoft.l2.thd.s7.gui;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;

/**
 * Utils for S7 devices editor
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
public interface S7DeviceOptionsUtils {

  /**
   * device IP address
   */
  IDataDef OP_TCP_IP_ADDRESS = create( "ip.address", STRING, //$NON-NLS-1$
      TSID_NAME, "IP address", //
      TSID_DESCRIPTION, "IP address in TCP network", //
      TSID_DEFAULT_VALUE, avStr( "127.0.0.1" ) //
  ); //

  /**
   * device rack
   */
  IDataDef OP_RACK = create( "rack", INTEGER, //$NON-NLS-1$
      TSID_NAME, "rack", //
      TSID_DESCRIPTION, "number of rack", //
      TSID_DEFAULT_VALUE, avInt( 0 ) //
  ); //

  /**
   * device slot
   */
  IDataDef OP_SLOT = create( "slot", INTEGER, //$NON-NLS-1$
      TSID_NAME, "slot", //
      TSID_DESCRIPTION, "number of slot", //
      TSID_DEFAULT_VALUE, avInt( 1 ) //
  ); //

  @SuppressWarnings( "javadoc" )
  static IStridablesList<IDataDef> getParamDefinitions() {
    return new StridablesList<>( OP_TCP_IP_ADDRESS, OP_RACK, OP_SLOT );
  }

  @SuppressWarnings( "javadoc" )
  static IOptionSet getParamDefaultValues() {
    OptionSet defaultVals = new OptionSet();

    IList<IDataDef> paramDefenitions = S7DeviceOptionsUtils.getParamDefinitions();
    for( IDataDef dDef : paramDefenitions ) {
      dDef.setValue( defaultVals, dDef.defaultValue() );
    }
    return defaultVals;
  }

}

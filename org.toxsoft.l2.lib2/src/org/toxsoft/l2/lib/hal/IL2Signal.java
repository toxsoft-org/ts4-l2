package org.toxsoft.l2.lib.hal;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.utils.rwkind.*;

/**
 * The signal is the single data (signal, I/O, etc) provided by the I/O hardware driver to the L2 DLMs.
 * <p>
 * TODO describe L2 signal usage, difference with L2 <code>IPin</code>.
 *
 * @author hazard157
 */
public interface IL2Signal
    extends IStridableParameterized {

  ERwKind kind();

  // TODO ETagHealth health();

  IDataType dataType();

  IAtomicValue get();

  void set( IAtomicValue aValue );

  // TODO source - the info from the provider of the tag

  // ------------------------------------------------------------------------------------
  // inline methods for convenience

  @SuppressWarnings( "javadoc" )
  default EAtomicType atomicType() {
    return dataType().atomicType();
  }

}

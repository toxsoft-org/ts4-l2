package org.toxsoft.l2.lib.hal;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.utils.errors.*;
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

  /**
   * Determines if signal can be read/written or both.
   *
   * @return {@link ERwKind} - the R/W kind
   */
  ERwKind kind();

  // TODO ETagHealth health();

  /**
   * Returns the data type of the signal.
   *
   * @return {@link IDataType} - the signal data type
   */
  IDataType dataType();

  /**
   * Returns the input signal value if applicable.
   *
   * @return {@link IAtomicValue} - the signal value, never is <code>null</code>
   * @throws TsUnsupportedFeatureRtException reading is not supported by this signal
   */
  IAtomicValue get();

  /**
   * Sets the output signal value if applicable.
   *
   * @param aValue {@link IAtomicValue} - the value to set
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsUnsupportedFeatureRtException writing is not supported by this signal
   * @throws AvTypeCastRtException argument type is not allowed
   */
  void set( IAtomicValue aValue );

  // TODO source - the info from the provider of the tag

  // ------------------------------------------------------------------------------------
  // inline methods for convenience

  @SuppressWarnings( "javadoc" )
  default EAtomicType atomicType() {
    return dataType().atomicType();
  }

}

package org.toxsoft.l2.lib.api.module.hal;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.rwkind.*;

/**
 * The tag is a single atomic value (tag, I/O, etc) provided by the I/O hardware driver to the L2 DLMs.
 * <p>
 * The tag is an interface (the means to exchange data) between physical device (represented by the HAL driver) and the
 * clients, such as DLM or Net module.
 * <p>
 * FIXME tags API description (for clients, for HDLH)
 *
 * <pre>
 *      +------------------*
 *      |  DLM, NetModule  |
 *      +------------------*
 *          read  write
 *            ^    |       <=== <b>IL2Tag</b>
 *            |    v
 *   +--------------------------+
 *   |  +-----------------*     | HAL Driver
 *   |  |  Tags (values)  |     |
 *   |  +-----------------*     |
 *   |        ^    |
 *   |        |    v       <=== <b>IL2TagForHDLH</b>
 *   |      put    get
 *   |  +-----------------*     |
 *   |  |      HDLH       |     |
 *   |  +-----------------*     |
 *   +--------------------------+
 * </pre>
 *
 * @author hazard157
 */
public interface IL2Tag
    extends IStridableParameterized {

  /**
   * Determines if tag can be read/written or both.
   *
   * @return {@link ERwKind} - the R/W kind
   */
  ERwKind kind();

  /**
   * Returns the type of the tag value.
   *
   * @return {@link IDataType} - the tag value atomic type
   */
  EAtomicType atomicType();

  /**
   * Determines if there was new value received from the hardware since last {@link #read()}.
   * <p>
   * Note: "new value" means "fresh value", that is value actually received from the hardware however the atomic value
   * may remain the same, it is just "fresher" than previous the value.
   *
   * @return boolean - new value flag<br>
   *         <b>true</b> - there is a fresh value available via the method {@link #read()};<br>
   *         <b>false</b> - {@link #read()} will return the same as the last call.
   */
  boolean isNewValueToRead();

  /**
   * Returns the input tag value if applicable.
   *
   * @return {@link IAtomicValue} - the tag value, never is <code>null</code>
   * @throws TsUnsupportedFeatureRtException reading is not supported by this tag
   */
  IAtomicValue read();

  /**
   * Sets the output tag value if applicable.
   *
   * @param aValue {@link IAtomicValue} - the value to set
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsUnsupportedFeatureRtException writing is not supported by this tag
   * @throws AvTypeCastRtException argument type is not allowed
   */
  void write( IAtomicValue aValue );

  /**
   * Returns the driver that provides this tag.
   *
   * @return {@link IL2HalDriver} - the driver providing the tag
   */
  IL2HalDriver driver();

}

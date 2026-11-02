package org.toxsoft.l2.lib.api.module.hal;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * L2 tag API to be used from the HDLH.
 *
 * @author hazard157
 */
public interface IL2TagForHDLH
    extends IStridableParameterized {

  /**
   * Returns the type of the tag value.
   *
   * @return {@link IDataType} - the tag value atomic type
   */
  EAtomicType atomicType();

  /**
   * Determines if upper part has wrote value in the tag after last call to {@link #get()}.
   *
   * @return boolean - new value flag<br>
   *         <b>true</b> - there is a new value available via the method {@link #get()};<br>
   *         <b>false</b> - {@link #get()} will return the same as the last call.
   */
  boolean isNewValueToGet();

  /**
   * Gets the value written in the tag and resets {@link #isNewValueToGet()} flag.
   *
   * @return {@link IAtomicValue} - value written to the tag
   */
  IAtomicValue get();

  /**
   * Sets the tag value to be read by the upper part.
   *
   * @param aValue {@link IAtomicValue} - the value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws AvTypeCastRtException value type is not compatible to the tag {@link #atomicType()}
   */
  void set( IAtomicValue aValue );

}

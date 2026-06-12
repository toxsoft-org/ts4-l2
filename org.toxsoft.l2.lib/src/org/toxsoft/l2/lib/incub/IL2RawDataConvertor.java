package org.toxsoft.l2.lib.incub;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

public interface IL2RawDataConvertor {

  public record Config ( String id, IOptionSet params )
      implements IParameterized {
  }

  /**
   * Reads atomic value from the device input represented as a raw bytes.
   *
   * @param aBytes byte[] - raw bytes to be read from
   * @param aOffset int - index of the first byte to be read from the array
   * @return {@link IAtomicValue} - the read value, never is <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException invalid offset, negative or too big
   */
  IAtomicValue readFrom( byte[] aBytes, int aOffset );

  /**
   * Writes atomic value back to the MODBUS registers as a raw bytes to be send to the device..
   *
   * @param aValue {@link IAtomicValue} - the value to write
   * @param aBytes byte[] - raw bytes to be written
   * @param aOffset int - index of the first byte to be written to the array
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException invalid offset, negative or too big
   */
  void writeTo( IAtomicValue aValue, byte[] aBytes, int aOffset );

}

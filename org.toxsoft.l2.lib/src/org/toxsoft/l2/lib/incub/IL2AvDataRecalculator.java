package org.toxsoft.l2.lib.incub;

import org.toxsoft.core.tslib.av.*;

public interface IL2AvDataRecalculator {

  IAtomicValue rawToTagValue( IAtomicValue aRawValue );

  IAtomicValue tagToRawValue( IAtomicValue aTagValue );

}

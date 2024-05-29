package org.toxsoft.l2.thd.modbus.common.translators;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.l2.thd.modbus.common.*;

/**
 * Транслятор дискретного сигнала - один бит в одно значение.
 *
 * @author Max
 */
public class BooleanCommonDiscretsTranslator
    implements IDiscretTranslator {

  @Override
  public IAtomicValue translate( boolean[] aBytes ) {
    return AvUtils.avBool( aBytes[0] );
  }

}

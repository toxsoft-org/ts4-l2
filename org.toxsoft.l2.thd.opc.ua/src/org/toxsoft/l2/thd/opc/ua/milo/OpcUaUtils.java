package org.toxsoft.l2.thd.opc.ua.milo;

import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.tslib.av.*;

/**
 * Утилитный класс для работы с OPC
 *
 * @author max
 */
public class OpcUaUtils {

  /**
   * Закрытый конструктор
   */
  private OpcUaUtils() {
  }

  public static IAtomicValue convertFromOpc( Variant aValue, EAtomicType aTagType ) {
    return IAtomicValue.NULL;
  }

  public static Variant convertToOpc( IAtomicValue aValue, EAtomicType aTagType ) {
    return Variant.NULL_VALUE;
  }
}

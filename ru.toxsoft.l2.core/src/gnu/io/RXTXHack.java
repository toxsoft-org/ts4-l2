package gnu.io;

import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Локализация ошибки закрытия com-порта. Источник: https://community.oracle.com/thread/1294323
 *
 * @author mvk
 */
public final class RXTXHack {

  private RXTXHack() {

  }

  /**
   * Закрыть порт
   *
   * @param aPort {@link RXTXPort} закрываемый порт
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void closeRxtxPort( RXTXPort aPort ) {
    TsNullArgumentRtException.checkNull( aPort );
    aPort.IOLocked = 0;
    aPort.close();
  }
}

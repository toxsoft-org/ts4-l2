package org.toxsoft.l2.lib_old.reserve;

/**
 * Определитель меандра
 * 
 * @author max
 */
public interface IMeanderDetector {

  /**
   * Возвращает период меандра.
   * 
   * @return int - период меандра,
   *         <li>0 - сигнал 0,
   *         <li>{@link Integer#MAX_VALUE} - сигнал 1,
   *         <li>-1 - ошибка детектирования.
   */
  int getMeanderPeriod();
}

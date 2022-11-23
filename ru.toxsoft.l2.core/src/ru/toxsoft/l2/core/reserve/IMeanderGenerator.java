package ru.toxsoft.l2.core.reserve;

/**
 * Генератор меандра
 * 
 * @author max
 */
public interface IMeanderGenerator {

  /**
   * Устанавливает период меандра.
   * 
   * @param aPeriod int - период меандра,
   *          <li>0 - сигнал 0,
   *          <li>{@link Integer#MAX_VALUE} - сигнал 1.
   */
  void setMeanderPeriod( int aPeriod );
}

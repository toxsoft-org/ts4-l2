package ru.toxsoft.l2.core.reserve;

import org.toxsoft.core.tslib.bricks.*;

/**
 * Слушатель здоровья шкафа.
 *
 * @author max
 */
public interface IHealthListener
    extends ICooperativeWorkerComponent {

  /**
   * Возвращает здоровье шкафа
   *
   * @return int - здоровье шкафа
   */
  int getHealth();

  /**
   * Возвращает признак того, что здоровье не позволяет функционировать шкафу в штатном режиме.
   *
   * @return true - здоровье не позволяет функционировать шкафу в штатном режиме,
   *         <p>
   *         false - шкаф может работать в штатном режиме.
   */
  boolean isSick();
}

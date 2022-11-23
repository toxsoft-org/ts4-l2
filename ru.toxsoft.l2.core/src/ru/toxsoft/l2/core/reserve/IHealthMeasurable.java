package ru.toxsoft.l2.core.reserve;

/**
 * Интерфейс элемента, способного оценить своё состояние в цифрах от 0 до 100.
 * 
 * @author max
 */
public interface IHealthMeasurable {

  /**
   * Возвращает степень исправности в процентах (0-100).
   * 
   * @return int - степень исправности.
   */
  default int getHealth(){
    return 100;
  }

  /**
   * Относительный вклад в исправность системы (1-10).
   * 
   * @return int - вклад в исправность системы.
   */
  default int getWeight(){
    return 5;
  }
  
  
}

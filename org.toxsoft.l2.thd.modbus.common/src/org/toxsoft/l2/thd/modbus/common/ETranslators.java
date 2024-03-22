package org.toxsoft.l2.thd.modbus.common;

import java.lang.reflect.*;

import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Перечень стандартных трансляторов: переводчиков из входных данных modbus в данные типа tslib
 *
 * @author max
 */
public enum ETranslators
    implements IStridable {

  ;

  private String id;

  private String descr;

  private Class<?> translatorClass;

  ETranslators( String aId, String aDescr, Class<?> aTranslatorClass ) {
    this.id = aId;
    this.descr = aDescr;
    this.translatorClass = aTranslatorClass;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String description() {
    return descr;
  }

  /**
   * Создаёт транслятор данного по идентификатору или классу
   *
   * @param aTranslator - идентификатор транслятора
   * @param aParams - параметры транслятора в строковом виде.
   * @return Object - транслятор
   * @throws Exception - ошибка создания транслятора
   */
  public static Object createTranslator( String aTranslator, String aParams )
      throws Exception {
    Class<?> translatorClass = null;

    for( ETranslators trans : values() ) {
      if( trans.id().equals( aTranslator ) ) {
        translatorClass = trans.translatorClass;
        break;
      }
    }

    if( translatorClass == null ) {
      translatorClass = Class.forName( aTranslator );
    }

    try {
      Constructor<?> constructor = translatorClass.getConstructor( new Class<?>[] { String.class } );
      return constructor.newInstance( new Object[] { aParams } );
    }
    catch( @SuppressWarnings( "unused" ) NoSuchMethodException e ) {
      // конструктор с параметром не найден - это не ошибка, а отсутствие конструктора.
    }

    return translatorClass.getConstructor().newInstance();
  }

  @Override
  public String nmName() {
    return id;
  }
}

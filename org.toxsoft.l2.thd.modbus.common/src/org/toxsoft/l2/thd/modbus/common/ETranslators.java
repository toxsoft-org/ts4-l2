package org.toxsoft.l2.thd.modbus.common;

import java.lang.reflect.*;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.l2.thd.modbus.common.translators.*;

/**
 * Перечень стандартных трансляторов: переводчиков из входных данных modbus в данные типа tslib
 *
 * @author max
 */
@SuppressWarnings( "nls" )
public enum ETranslators
    implements IStridable {

  /**
   * Boolean from modbus discrets translator
   */
  BOOLEAN_DISCRETS( "BOOLEAN_DISCRETS", "Boolean from modbus discrets translator",
      BooleanCommonDiscretsTranslator.class ),

  /**
   * Boolean from modbus registers translator
   */
  BOOLEAN_REGISTERS( "BOOLEAN_REGISTERS", "Boolean from modbus registers translator",
      BooleanCommonRegistersTranslator.class ),

  /**
   * Integer from modbus registers translator
   */
  INTEGER_REGISTERS( "INTEGER_REGISTERS", "Integer from modbus registers translator",
      IntegerCommonRegistersTranslator.class ),

  /**
   * Float from modbus registers translator
   */
  FLOAT_REGISTERS( "FLOAT_REGISTERS", "Float from modbus registers translator", FloatCommonRegistersTranslator.class );

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

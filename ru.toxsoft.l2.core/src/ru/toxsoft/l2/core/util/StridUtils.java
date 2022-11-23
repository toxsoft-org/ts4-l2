package ru.toxsoft.l2.core.util;

import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.nio.charset.*;

import org.toxsoft.core.tslib.bricks.strio.chario.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Вспомогательные методы
 *
 * @author mvk
 */
public class StridUtils {

  /**
   * Строка нулевой длины
   */
  public static final String EMPTY_STRING = TsLibUtils.EMPTY_STRING;

  /**
   * Полностью загрузить файл в поток чтения символов
   *
   * @param aFile {@link File} читаемый файл
   * @return {@link ICharInputStream} поток чтения символьных данных
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIoRtException ошибка чтения
   */
  public static ICharInputStream loadCharInputStreamFromFile( File aFile ) {
    return loadCharInputStreamFromFile( aFile, UTF_8 );
  }

  /**
   * Полностью загрузить файл в поток чтения символов
   *
   * @param aFile {@link File} читаемый файл
   * @param aCharset String кодировка читаемых символов
   * @return {@link ICharInputStream} поток чтения символьных данных
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIoRtException ошибка чтения
   */
  public static ICharInputStream loadCharInputStreamFromFile( File aFile, Charset aCharset ) {
    TsNullArgumentRtException.checkNulls( aFile, aCharset );
    try {
      try( FileInputStream file = new FileInputStream( aFile ) ) {
        byte[] buffer = new byte[file.available()];
        file.read( buffer );
        String text = new String( buffer, aCharset );
        return new CharInputStreamString( text );
      }
    }
    catch( IOException e ) {
      throw new TsIoRtException( e );
    }
  }

  /**
   * Выбрасывает исключение если аргумент null или пустая строка.
   *
   * @param aString String - проверяемая строка
   * @return String - переданные аргумент
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException aString - пустая строка (строка из 0 символов)
   */
  public static String checkNonEmpty( String aString ) {
    if( aString == null ) {
      throw new TsNullArgumentRtException();
    }
    if( aString.length() == 0 ) {
      throw new TsIllegalArgumentRtException();
    }
    return aString;
  }

  /**
   * Начальное значение при вычилении хеш-кода.
   * <p>
   * Для использования см. комментарии к {@link #PRIME}.
   *
   * @see #PRIME
   */
  public static final int INITIAL_HASH_CODE = 1;

  /**
   * Простое число, используемое как множитель при добавлении хеш-кода очередного поля объекта (элемента коллекции).
   * <p>
   * Пример подсчета хеш-кода:
   *
   * <pre>
   * ...
   * &#064;Override
   * public int hashCode() {
   *   int result = CollectionsUtils.INITIAL_HASH_CODE;
   *   result = CollectionsUtils.PRIME * result + <b>objectField</b>.hashCode();
   *   result = CollectionsUtils.PRIME * result + (<b>booleanField</b> ? 1 : 0);
   *   result = CollectionsUtils.PRIME * result + <b>intField</b>;
   *   result = CollectionsUtils.PRIME * result + (int)(<b>longField</b> ^ (<b>longField</b> >>> 32));
   *   int fltval = Float.floatToRawIntBits( <b>floatField</b> );
   *   result = CollectionsUtils.PRIME * result + fltval;
   *   long dblval = Double.doubleToRawLongBits( <b>doubleField</b> );
   *   result = CollectionsUtils.PRIME * result + (int)(dblval ^ (dblval >>> 32));
   *   result = CollectionsUtils.PRIME * result + <b>stringField</b>.hashCode();
   *   return result;
   * }
   * </pre>
   */
  public static final int PRIME = 31;
}

package ru.toxsoft.l2.core.cfg.impl;

import static ru.toxsoft.l2.core.cfg.impl.IL2Resources.*;
import static ru.toxsoft.l2.core.util.StridUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.files.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

//import ru.toxsoft.tslib.datavalue.IDvReader;
//import ru.toxsoft.tslib.datavalue.impl.DvReader;
import ru.toxsoft.l2.core.cfg.*;

/**
 * Абстрактная реализация загрузчика конфигураций.
 *
 * @author max
 * @param <T> - класс конфигурации, наследник {@link IUnitConfig}.
 */
public abstract class AbstractUnitConfigLoader<T extends IUnitConfig> {

  /**
   * Фильтр файлов по расширениям
   */
  private final FileFilter CFG_FILE_FILTER;

  private String cfgKeyword;

  private final IStridablesListEdit<T> unitConfigs = new StridablesList<>();

  /**
   * Конструктор загрузчика
   *
   * @param aCfgFileExtension - расширение фалов конфигураций
   * @param aCfgKeyword - ключевое слово, с которого начинается конфигурация в файле.
   */
  public AbstractUnitConfigLoader( final String aCfgFileExtension, String aCfgKeyword ) {

    cfgKeyword = aCfgKeyword;

    CFG_FILE_FILTER = aPath -> {
      if( aPath.isFile() ) {
        String name = aPath.getName().toLowerCase();
        if( name.endsWith( aCfgFileExtension ) ) {
          return true;
        }
      }
      return false;
    };
  }

  /**
   * Производит чтение конфигурационных файлов из указанного директория.
   * <p>
   * Обратите внимание, что повторное чтение приводит к удалению всей ранее считанной информации.
   *
   * @param aConfigDir {@link File} - директория расположения конфигурационных файлов
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIoRtException (NOT_FOUND) директория не найдена
   * @throws TsIoRtException (NOT_A_DIR) это не директория (файл или еще что-то)
   * @throws TsIoRtException (NO_READ_RIGHTS) нет прав на чтение
   */
  public void loadConfig( File aConfigDir ) {
    TsFileUtils.checkDirReadable( aConfigDir );
    File[] cfgFiles = aConfigDir.listFiles( CFG_FILE_FILTER );
    for( File f : cfgFiles ) {
      // mvk: буферизированное чтение из текстового файла
      // CharInputStreamFile chIn = null;
      try {
        // mvk: буферизированное чтение из текстового файла
        // chIn = new CharInputStreamFile( f );
        ICharInputStream chIn = loadCharInputStreamFromFile( f );
        IStrioReader sr = new StrioReader( chIn );
        sr.setSkipMode( EStrioSkipMode.SKIP_COMMENTS );
        // IDvReader dr = new DvReader( sr );

        // сначала считаем KEYWORD_THDCFG =
        sr.ensureString( cfgKeyword );
        char CHAR_EQUAL = '=';
        sr.ensureChar( CHAR_EQUAL );
        // а потом конфигурацию драйвера
        T unitCfg = load( sr );

        // проверка на наличие повторяющихся идов
        if( unitConfigs.hasKey( unitCfg.id() ) ) {
          LoggerUtils.errorLogger().error( ERR_LIST_ALREADY_CONTAINS_CONFIG, unitCfg.id(), f.getName() );
        }
        else {
          unitConfigs.add( unitCfg );
        }
      }
      catch( Exception e ) {
        LoggerUtils.errorLogger().error( e, ERR_CONFIG_LOADING_N_CREATION, f.getName() );
      }
      // finally {
      // if( chIn != null ) {
      // chIn.close();
      // }
      // }
    }
  }

  /**
   * Загружает конфигурацию из читателя типов и значений, реализация зависит от конкретного класса конфигураций.
   *
   * @param aFile File - файл с конфигурацией.
   * @return T - загруженная конфигурация.
   */
  abstract protected T load( IStrioReader aFile );

  /**
   * Возвращает загруженные конфигурации.
   *
   * @return IStridablesList&lt;{@link IUnitConfig}&gt; - список всех конфигураций
   */
  public IStridablesList<T> listThDriverConfigs() {
    return unitConfigs;
  }

  /**
   * Возвращает конфигурацию с указанным идентификатором.
   *
   * @param aUnitConfigId String - идентфикатор конфигурации.
   * @return T - конфигурация с указанным идентификатором, или null - если такой конфигурации нет.
   */
  public T getUnitConfig( String aUnitConfigId ) {
    for( T cfg : unitConfigs ) {
      if( cfg.id().equals( aUnitConfigId ) ) {
        return cfg;
      }
    }
    return null;
  }
}

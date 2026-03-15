package org.toxsoft.l2.main.old_refactor.helpers;

import static org.toxsoft.l2.main.old_refactor.helpers.IL2MainSharedResources.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.misc.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.main.*;
import org.toxsoft.l2.lib.main.impl.*;
import org.toxsoft.l2.main.old_refactor.*;

/**
 * Статические и вспомогательные методы работы с компонентой проектно-зависимого функционала.
 *
 * @author goga
 */
public class L2AppUtils {

  /**
   * Версия программы НУ.
   */
  public static final TsVersion VERSION = new TsVersion( 1, 1 );

  /**
   * Журнал работы
   */
  private static ILogger logger = LoggerWrapper.getLogger( L2AppUtils.class.getName() );

  /**
   * Запрет на создание экземпляров.
   */
  private L2AppUtils() {

  }

  /**
   * Создаёт и возвращает компонент работы с APP.
   *
   * @param aGlobalContext - глобальный контекст.
   * @return IAppComponent - компонент работы с APP.
   */
  public static IAppComponent createApp( GlobalContext aGlobalContext ) {
    IAppComponent result = createAppImpl( aGlobalContext );
    // FIXME aGlobalContext.setAppApi( result );
    return result;
  }

  private static IAppComponent createAppImpl( GlobalContext aGlobalContext ) {
    String appClassName = aGlobalContext.globalOps().getStr( IGlobalOps.APP_CLASS_NAME );

    try {
      Class<?> appClass = Class.forName( appClassName );
      Constructor<?> appClassConstructor = appClass.getConstructor( IGlobalContext.class );

      IAppComponent result = (IAppComponent)appClassConstructor.newInstance( aGlobalContext );
      return result;
    }
    catch( Exception e ) {
      logger.error( e );
    }
    return null;
  }

  /**
   * Формирует набор параметров из аргументов командной строки.
   * <p>
   * Командная строка должна состять из пар <b>-opName opValue</b>, разделенных проблами.Здесь opName - имя параметра
   * (ИД-путь), а opValue - атомарное значение в формате {@link IDvWriter#writeAtomicValue(IAtomicValue)}. Если значение
   * не может быть интерпретировано как атомарное значение, оно запоминается как атомарное значение типа
   * {@link EAtomicType#STRING}.
   * <p>
   * Метод не выбрасывает исключений, все исключения ловятся и логируются. В случае ошибки возвращает пустой список
   * параметров.
   *
   * @param aArgs String[] - аргументы командной строки (из метода {@link L2CoreMain#main(String[])}
   * @return {@link IOptionSetEdit} - сформированный или пустой набор параметров
   */
  public static IOptionSet parseCmdLine( String[] aArgs ) {

    // OptionSetKeeper.KEEPER.
    try {
      TsIllegalArgumentRtException.checkTrue( aArgs.length % 2 != 0, MSG_ERR_UNEVEN_CMD_LINE_ARGS );
      CharInputStreamString chIn = new CharInputStreamString();
      IStrioReader sr = new StrioReader( chIn );
      // IDvReader dr = new DvReader( sr );
      IOptionSetEdit ops = new OptionSet();
      for( int i = 0; i < aArgs.length; i += 2 ) {
        TsIllegalArgumentRtException.checkTrue( aArgs[i].charAt( 0 ) != '-', MSG_ERR_NO_HYPHEN_IN_CMD_LINE_OP_NAME,
            aArgs[i] );
        TsIllegalArgumentRtException.checkTrue( aArgs[i].length() <= 1, MSG_ERR_NO_OP_NAME );
        String opName = aArgs[i].substring( 1 );
        TsIllegalArgumentRtException.checkFalse( StridUtils.isValidIdPath( opName ), MSG_ERR_INV_OP_NAME, opName );
        chIn.setSource( aArgs[i + 1] );
        IAtomicValue av;
        try {
          av = new AvTextParser().parse( sr.readLine() );
        }
        catch( Exception e ) {
          av = AvUtils.avStr( aArgs[i + 1] );
        }
        ops.setValue( opName, av );
      }
      return ops;
    }
    catch( Exception e ) {
      String msg = String.format( MSG_ERR_INV_CMD_LINE );
      L2CoreMain.globalLogger.error( msg, e );
      return IOptionSet.NULL;
    }
  }

  /**
   * Выводит в консоль приветственное сообщение программы.
   */
  public static void sayHello( long aStartTime ) {
    L2CoreMain.globalLogger.info( MSG_STARTUP, VERSION.toString(), Long.valueOf( aStartTime ) );
  }

  public static void threadInfoToLogs() {
    Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
    L2CoreMain.globalLogger.debug( "+++++++++++++++++++++++++++++++++++++++++++++++++++++" ); //$NON-NLS-1$
    L2CoreMain.globalLogger.debug( "thread count = %d", Integer.valueOf( threads.size() ) ); //$NON-NLS-1$
    for( Thread t : threads.keySet() ) {
      L2CoreMain.globalLogger.debug( "=====================================================" ); //$NON-NLS-1$
      L2CoreMain.globalLogger.debug( "thread = %s", t.getName() ); //$NON-NLS-1$
      StackTraceElement[] seArray = threads.get( t );
      for( int index = 0, n = seArray.length; index < n; index++ ) {
        StackTraceElement se = seArray[index];
        L2CoreMain.globalLogger.debug( "   %s, %d, %s", se.getFileName(), Integer.valueOf( se.getLineNumber() ), //$NON-NLS-1$
            se.getMethodName() );
      }
    }
  }

  /**
   * Считывает набор параметров из файла конфигурации.
   * <p>
   * Формат файла конфигурации соответствует {@link OptionSetKeeper#KEEPER} и не допускает никаких комментариев или
   * других данных.
   * <p>
   * Метод не выбрасывает исключений, все исключения ловятся и логируются. В случае ошибки возвращает пустой список
   * параметров.
   *
   * @param aFile {@link File} - файл для чтения конфигурации
   * @return {@link IOptionSetEdit} - счтанный или пустой набор параметров
   */
  public static IOptionSet readConfigFile( File aFile ) {
    try {
      // mvk: буферизированное чтение из текстового файла
      // ICharInputStream chIn = new CharInputStreamFile( aFile );
      // ICharInputStream chIn = loadCharInputStreamFromFile( aFile );

      // IStridReader sr = new StridReader( chIn );
      // IDvReader dr = new DvReader( sr );

      IOptionSet result = OptionSetKeeper.KEEPER.read( aFile );
      L2CoreMain.globalLogger.info( MSG_CFG_FILE_READ_OK, aFile.getAbsolutePath() );
      return result;
    }
    catch( Exception e ) {
      L2CoreMain.globalLogger.warning( e, MSG_ERR_CANT_READ_CFG_FILE, aFile.getAbsolutePath() );
      return IOptionSet.NULL;
    }
  }

  /**
   * Выводит в консоль завершающее сообщение программы.
   *
   * @param aQuitCommand {@link IProgramQuitCommand} - команда, по которой завершается программа
   */
  public static void sayGoodbye( IProgramQuitCommand aQuitCommand, long aStartTime ) {
    long endTime = System.currentTimeMillis();
    long deltaSecs = (endTime - aStartTime) / 1000;
    long days = deltaSecs / 86400L;
    int secs = (int)(deltaSecs % 86400L);
    int hours = secs / 3600;
    secs %= 3600;
    int mins = secs / 60;
    secs %= 60;
    L2CoreMain.globalLogger.info( MSG_GOODBYE, aQuitCommand.message(), Integer.valueOf( aQuitCommand.programRetCode() ),
        Long.valueOf( days ), Integer.valueOf( hours ), Integer.valueOf( mins ), Integer.valueOf( secs ) );
  }

}

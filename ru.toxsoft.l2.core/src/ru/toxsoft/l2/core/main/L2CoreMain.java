package ru.toxsoft.l2.core.main;

import static ru.toxsoft.l2.core.main.IL2HardConstants.*;
import static ru.toxsoft.l2.core.main.IL2Resources.*;

import java.io.*;
import java.lang.management.*;
import java.util.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.misc.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.chario.impl.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.skf.rri.lib.impl.*;
import org.toxsoft.uskat.core.impl.*;

import ru.toxsoft.l2.core.app.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.dlm.impl.*;
import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.main.impl.*;
import ru.toxsoft.l2.core.net.*;
import ru.toxsoft.l2.core.reserve.*;

/**
 * Начало выполнения программы нижнего уровня.
 *
 * @author max
 */
public class L2CoreMain {

  /**
   * Версия программы НУ.
   */
  public static final TsVersion VERSION = new TsVersion( 1, 1 );

  /**
   * Время начала выполнения программы НУ.
   */
  private static long startTime = System.currentTimeMillis();

  /**
   * Глобальный логер программы.
   */
  static ILogger globalLogger = null;

  /**
   * Команда, устанавливаемая шатдаун хуком.
   */
  private static IProgramQuitCommand shutdownHookQuitCmd = null;

  /**
   * Поток шатдаун хука.
   */
  private static ShutdownHookThread shutdownHookThread = new ShutdownHookThread();

  /**
   * Признак, что можно завершать поток шатдаун хука
   */
  private static boolean shutedDown = false;

  /**
   * Устанавливает признак завершения потока шатдаун хука.
   */
  static void setShutedDown() {
    shutedDown = true;
  }

  /**
   * Возвращает признак возмоности завершения потока шатдаун хука.
   *
   * @return true - поток шатдаун хука можно завершить.
   */
  static boolean isShutedDown() {
    return shutedDown;
  }

  /**
   * Возвращает команду завершения программы или null, если она не поступала.
   *
   * @return IProgramQuitCommand - команда завершения программы.
   */
  public static synchronized IProgramQuitCommand getShutdownHookQuitCmd() {
    return shutdownHookQuitCmd;
  }

  /**
   * Устанавливает команду завершения программы. Если команда установлена, то программа вскоре будет корректно
   * завершена.
   *
   * @param aShutdownHookQuitCmd IProgramQuitCommand - команда завершения программы.
   */
  public static synchronized void setShutdownHookQuitCmd( IProgramQuitCommand aShutdownHookQuitCmd ) {
    shutdownHookQuitCmd = aShutdownHookQuitCmd;
  }

  /**
   * Выводит в консоль приветственное сообщение программы.
   */
  private static void sayHello() {
    globalLogger.info( MSG_STARTUP, VERSION.toString(), Long.valueOf( startTime ) );
  }

  /**
   * Выводит в консоль завершающее сообщение программы.
   *
   * @param aQuitCommand {@link IProgramQuitCommand} - команда, по которой завершается программа
   */
  private static void sayGoodbye( IProgramQuitCommand aQuitCommand ) {
    long endTime = System.currentTimeMillis();
    long deltaSecs = (endTime - startTime) / 1000;
    long days = deltaSecs / 86400L;
    int secs = (int)(deltaSecs % 86400L);
    int hours = secs / 3600;
    secs %= 3600;
    int mins = secs / 60;
    secs %= 60;
    globalLogger.info( MSG_GOODBYE, aQuitCommand.message(), Integer.valueOf( aQuitCommand.programRetCode() ),
        Long.valueOf( days ), Integer.valueOf( hours ), Integer.valueOf( mins ), Integer.valueOf( secs ) );
  }

  /**
   * Считывает набор параметров из файла конфигурации.
   * <p>
   * Формат файла конфигурации соответствует {@link IDvWriter#writeOptionSet(IDvNamedVars)} и не допускает никаких
   * комментариев или других данных.
   * <p>
   * Метод не выбрасывает исключений, все исключения ловятся и логируются. В случае ошибки возвращает пустой список
   * параметров.
   *
   * @param aFile {@link File} - файл для чтения конфигурации
   * @return {@link IOptionSetEdit} - счтанный или пустой набор параметров
   */
  private static IOptionSet readConfigFile( File aFile ) {
    try {
      // mvk: буферизированное чтение из текстового файла
      // ICharInputStream chIn = new CharInputStreamFile( aFile );
      // ICharInputStream chIn = loadCharInputStreamFromFile( aFile );

      // IStridReader sr = new StridReader( chIn );
      // IDvReader dr = new DvReader( sr );

      IOptionSet result = OptionSetKeeper.KEEPER.read( aFile );
      globalLogger.info( MSG_CFG_FILE_READ_OK, aFile.getAbsolutePath() );
      return result;
    }
    catch( Exception e ) {
      globalLogger.warning( e, MSG_ERR_CANT_READ_CFG_FILE, aFile.getAbsolutePath() );
      return IOptionSet.NULL;
    }
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
   * @param aArgs String[] - аргументы командной строки (из метода {@link #main(String[])}
   * @return {@link IOptionSetEdit} - сформированный или пустой набор параметров
   */
  private static IOptionSet parseCmdLine( String[] aArgs ) {

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
      globalLogger.error( msg, e );
      return IOptionSet.NULL;
    }
  }

  private static boolean stopComponent( ICooperativeWorkerComponent aComponent, long aTimeoutMSecs ) {
    TsNullArgumentRtException.checkNull( aComponent );
    TsIllegalArgumentRtException.checkTrue( aTimeoutMSecs < 0 || aTimeoutMSecs > 120 * 1000 );
    if( aComponent.queryStop() ) {
      return true;
    }
    long t0 = System.currentTimeMillis();
    while( !aComponent.isStopped() ) {
      long t1 = System.currentTimeMillis();
      if( t1 - t0 > aTimeoutMSecs ) {
        return false;
      }

      // try {
      // Thread.sleep( 10 );
      // }
      // catch( InterruptedException e ) {
      // // TODO Auto-generated catch block
      // e.printStackTrace();
      // }
    }
    return true;
  }

  private static boolean stopComponent2( String aComponentName, ICooperativeWorkerComponent aComponent,
      long aTimeoutMSecs ) {
    TsNullArgumentRtException.checkNull( aComponent );
    TsIllegalArgumentRtException.checkTrue( aTimeoutMSecs < 0 || aTimeoutMSecs > 120 * 1000 );

    globalLogger.info( "Start stopping component: %s", aComponentName );
    long startStoppingTime = System.currentTimeMillis();
    boolean result = aComponent.queryStop();
    // if( aComponentName.equals( "RESERVE" ) ) {
    // globalLogger.debug( "!!! result queryStopReserve = %b", result );
    // }
    if( !result ) {
      result = true;
      long t0 = System.currentTimeMillis();
      boolean isStopped = aComponent.isStopped();
      // if( aComponentName.equals( "RESERVE" ) ) {
      // globalLogger.debug( "!!! isStopped = %b", isStopped );
      // }
      while( !isStopped ) {
        long t1 = System.currentTimeMillis();
        if( t1 - t0 > aTimeoutMSecs ) {
          result = false;
          break;
        }
        isStopped = aComponent.isStopped();
        // if( aComponentName.equals( "RESERVE" ) ) {
        // globalLogger.debug( "!!! isStopped in while = %b", isStopped );
        // }
      }
    }
    globalLogger.info( "Component: %s - stopped successful (%b) in %d msec", aComponentName, result,
        (System.currentTimeMillis() - startStoppingTime) );

    return result;
  }

  private static void startComponent2( String aComponentName, ICooperativeWorkerComponent aComponent ) {
    TsNullArgumentRtException.checkNull( aComponent );

    globalLogger.info( "Start starting component: %s", aComponentName );
    long startStartingTime = System.currentTimeMillis();
    aComponent.start();
    globalLogger.info( "Component: %s - started in %d msec", aComponentName,
        (System.currentTimeMillis() - startStartingTime) );

  }

  /**
   * Начало выполнения программы.
   *
   * @param aArgs String[] - аргументы командной строки
   */
  public static void main( String[] aArgs ) {
    // Слежение за файлом конфигурации журнала log4j.xml
    LoggerWrapper.setScanPropertiesTimeout( 10000 );
    // инициализация глобального логера
    globalLogger = LoggerWrapper.getLogger( L2CoreMain.class.getName() );
    LoggerUtils.setErrorLogger( globalLogger ); // to Goga scandir 2015.09.17 by Max

    // регистрируем службу НСИ
    SkCoreUtils.registerSkServiceCreator( SkRegRefInfoService.CREATOR );

    sayHello();

    Thread monitor = new Thread( new ThreadsMonitor(), "L2 Threads Monitor Thread" );
    monitor.setDaemon( true );
    monitor.start();

    // инициализация счётчика загруженности
    // cpuLoadCalculator.init();

    // контекст
    GlobalContext globalContext = null;

    // драйвера
    IHalComponent hal = null;

    // работа с сетью
    INetworkComponent net = null;

    // работа с резервированием
    IReserveComponent reserve = null;

    // работа с функциональными модулями
    IDlmManagerComponent dlmMgr = null;

    // приложение НУ
    IAppComponent appComponent = null;

    long readDevicesDelay = 0;
    long writeDevicesDelay = 0;

    try {
      // LoggerUtils.defaultLogger();
      // LoggerUtils.createFileLogger( new File( "l2Log.log" ) );

      // инициализировать конфигурацию: умолчания - файл - командная строка
      OptionSet ops = new OptionSet( EGlobalOps.asOptionSet() );
      // OptionSetUtils.nvAdd( ops, readConfigFile( new File( L2_MAIN_CFG_FILE_NAME ) ) );
      ops.addAll( readConfigFile( new File( L2_MAIN_CFG_FILE_NAME ) ) );
      // OptionSetUtils.nvAdd( ops, parseCmdLine( aArgs ) );
      ops.addAll( parseCmdLine( aArgs ) );

      globalLogger.info( MSG_CREATE_CONTEXT );
      globalContext = new GlobalContext( ops, globalLogger, startTime );

      // подключиться к НУ: установить связь, считать конфигурацию, начать информирование "я живой"
      globalLogger.info( MSG_CREATE_HAL );
      hal = HalUtils.createHal( globalContext );

      // подключиться сети (к верхнему уровню, КТНС и т.п.)
      globalLogger.info( MSG_CREATE_NETWORK );
      net = NetworkUtils.createNetwork( globalContext );

      // создание механизма резервирования
      globalLogger.info( "Creation of Reserve module" );
      reserve = ReserveUtils.createReserve( globalContext );

      // инициализировать компонент спец функционала
      globalLogger.info( MSG_CREATE_APP );
      appComponent = AppUtils.createApp( globalContext );

      // инициализировать менеджер плагинов и модулей
      globalLogger.info( MSG_CREATE_DLM );
      dlmMgr = DlmManagerUtils.createDlmManager( globalContext );

      // запустить главный цикл / поток
      globalLogger.info( MSG_START_HAL );
      startComponent2( "HAL", hal );

      globalLogger.info( MSG_START_NETWORK );
      startComponent2( "NET", net );

      globalLogger.info( "Stating reserve module" );
      startComponent2( "RESERVE", reserve );

      // globalLogger.info( MSG_START_DLM );
      // dlmMgr.start();

      globalLogger.info( MSG_START_APP );
      startComponent2( "APP", appComponent );

      readDevicesDelay = hal.options().getLong( EHalOps.MAX_TYPICAL_READ_DEVICE_TIME );
      writeDevicesDelay = hal.options().getLong( EHalOps.MAX_TYPICAL_WRITE_DEVICE_TIME );

      globalLogger.info( MSG_READ_DEVICES_DELAY, Long.valueOf( readDevicesDelay ) );
      globalLogger.info( MSG_WRITE_DEVICES_DELAY, Long.valueOf( writeDevicesDelay ) );

      globalLogger.info( MSG_HOOK_SHUTDOWN );
      Runtime.getRuntime().addShutdownHook( shutdownHookThread );
    }
    catch( Throwable e ) {
      // Ошибка запуска программы. Ситуация возможна при hot swapping)
      globalLogger.error( e, MSG_CANT_START );
      // Форсированное завершение (могут быть потоки останавливающие выгрузку программы)
      Runtime.getRuntime().halt( -2 );
    }

    // первый раз проверка потоков - для проверки этого функционала
    // {
    // double cpuLoad = cpuLoadCalculator.getCpuLoad();
    // globalLogger.debug( "Test Cpu L2 Thread First attept. L2CpuLoad = %.2f", cpuLoad );
    // threadInfoToLogs();
    // }

    globalLogger.info( MSG_MAIN_LOOP_STARTED );
    IProgramQuitCommand quitCmd = null;

    // первоначально - модуль dlm не запущен
    // boolean isSuspend = true;
    try {
      while( quitCmd == null ) {
        try {
          long c = System.currentTimeMillis();
          long s = c;

          // long start = System.currentTimeMillis();

          if( hal != null ) {
            // инициация чтения
            hal.doReadDevices();
          }

          s = System.currentTimeMillis();
          long d0 = s - c;
          c = s;

          if( net != null ) {
            // работа с сервером
            net.doJob();
          }

          // засыпание потока, необходимое для загрузки данных с устройств
          Thread.sleep( readDevicesDelay );// настраиваемый параметр

          s = System.currentTimeMillis();

          // long dur = System.currentTimeMillis() - start;
          // System.out.println( "MAIN THREAD DUR = " + dur );
          if( hal != null ) {
            // чтение из буфера НУ
            hal.doReadValues();
          }

          s = System.currentTimeMillis();
          long d1 = s - c;
          c = s;

          if( reserve != null ) {
            reserve.doJob();
          }
          s = System.currentTimeMillis();
          // System.out.println( String.format( "Reserve Job = %d", (s - c) ) );
          c = s;

          if( reserve != null && reserve.isDLMsRun() && dlmMgr != null ) {
            // работа модулей
            dlmMgr.doJob();
          }

          s = System.currentTimeMillis();
          long d2 = s - c;
          // System.out.println( String.format( "Dlm Job = %d", d2 ) );
          c = s;

          // работа приложения
          if( appComponent != null ) {
            appComponent.doJob();
          }

          s = System.currentTimeMillis();
          long d3 = s - c;
          c = s;

          if( hal != null ) {
            // обмен с буфером и инициация записи на устройства
            hal.doWriteValues();
          }

          s = System.currentTimeMillis();
          long d4 = s - c;
          c = s;

          // засыпание потока, необходимое для записи данных на устройства
          Thread.sleep( writeDevicesDelay );// настраиваемый параметр

          if( globalContext != null ) {
            Thread.sleep( globalContext.globalOps().getLong( EGlobalOps.MAIN_LOOP_SLEEP_MSECS ) );
            // TODO: mvkd
            if( d0 + d1 + d2 + d3 + d4 + readDevicesDelay + writeDevicesDelay
                + globalContext.globalOps().getLong( EGlobalOps.MAIN_LOOP_SLEEP_MSECS ) > 1000 ) {
              System.out.println( "+++++++++++++++++++++++ MVK d0 = " + d0 + ", d1 = " + d1 + ", d2 = " + d2 + ", d3 = "
                  + d3 + ", d4 = " + d4 );
            }

            // System.out.println( "hal read = " + d0 + ", val read = " + d1 + ", dlm job = " + d2 + ", app job = " + d3
            // + ", val erite = " + d4 );
          }

          // проверка потоков
          // checkThreads();
        }
        catch( InterruptedException e ) {
          globalLogger.warning( e, MSG_MAIN_LOOP_INTERRUPTED );
          quitCmd = new ProgramQuitCommand( 1, MSG_MAIN_LOOP_INTERRUPTED );
        }
        if( quitCmd == null && net != null ) {
          quitCmd = net.getQuitCommandIfAny();
        }

        if( quitCmd == null ) {
          quitCmd = getShutdownHookQuitCmd();
        }
        else {
          Runtime.getRuntime().removeShutdownHook( shutdownHookThread );
        }
      }
    }
    catch( Throwable e ) {
      // Аварийное завершение программы
      globalLogger.error( e, MSG_UNEXPECTED_SHUTDOWN );
      quitCmd = new ProgramQuitCommand( -1, MSG_UNEXPECTED_SHUTDOWN );
    }

    // отработка команды как ТРОЛЛЬ-5 "запусти новую версию", "запусти предыдущую версию", т.е. штатный перезапуск
    // TODO что надо делать по команде завершения??? какие нужны аргументы команды?

    // штатное завершение: завершить менеджеры, отключиться от ВУ, потом НУ, потом перезапуск с кодом выхода
    try {
      try {
        stopComponent2( "APP", appComponent, 5000 );
        stopComponent2( "RESERVE", reserve, 5000 );
        // if( !isSuspend ) {
        // stopComponent2( "DLM", dlmMgr, 5000 );
        // }
        stopComponent2( "NET", net, 5000 );
        stopComponent2( "HAL", hal, 5000 );

        if( appComponent != null ) {
          appComponent.destroy();
          appComponent = null;
        }
        if( reserve != null ) {
          reserve.destroy();
          reserve = null;
        }
        if( dlmMgr != null ) {
          dlmMgr.destroy();
          dlmMgr = null;
        }
        if( net != null ) {
          net.destroy();
          net = null;
        }
        if( hal != null ) {
          hal.destroy();
          hal = null;
        }

        sayGoodbye( quitCmd );
        synchronized (L2CoreMain.class) {
          setShutedDown();
          L2CoreMain.class.notifyAll();
        }
      }
      catch( Throwable e ) {
        // Неожиданная ошибка завершения работы программы
        globalLogger.error( e, MSG_CANT_SHUTDOWN );
      }
    }
    finally {
      globalLogger.info( MSG_SHUTDOWN, Integer.valueOf( quitCmd.programRetCode() ) );
      // Форсированное завершение (могут быть потоки останавливающие выгрузку программы)
      Runtime.getRuntime().halt( quitCmd.programRetCode() );
    }
  }

  /**
   * Класс потока завершения приложения (CTRL+C)
   *
   * @author max
   */
  static class ShutdownHookThread
      extends Thread {

    @Override
    public void run() {
      globalLogger.error( "ShutdownHookThread Started" ); //$NON-NLS-1$
      setShutdownHookQuitCmd( new ProgramQuitCommand( 0, MSG_SHUTDOWN_BY_CTRL_C ) );

      synchronized (L2CoreMain.class) {
        while( !isShutedDown() ) {
          try {
            L2CoreMain.class.wait();
          }
          catch( InterruptedException e ) {
            e.printStackTrace();
          }
        }
      }

      globalLogger.error( "ShutdownHookThread Quit" ); //$NON-NLS-1$
    }

  }

  //
  // ------------------------------------------------------------------------------------------------------------
  // методы и классы диагностики потоков
  /**
   * Период проверки загруженности системы программой НУ.
   */
  private static long CHECK_CPU_LOAD_PERIOD = 60000L;

  /**
   * Уровень загруженности системы программой НУ при котором следует выводить отладочную информацию по потокам.
   */
  private static double CPU_LOAD__DEBUG_INFO_LEVEL = 0.6d;

  /**
   * Объект для плдсчёта загруженности системы программой НУ.
   */
  private static CpuLoadCalculator cpuLoadCalculator = new CpuLoadCalculator();

  private static long prevCheckThreadsTime = 0;

  static void checkThreads() {
    if( System.currentTimeMillis() - prevCheckThreadsTime > CHECK_CPU_LOAD_PERIOD ) {
      double cpuLoad = cpuLoadCalculator.getCpuLoad();
      if( cpuLoad > CPU_LOAD__DEBUG_INFO_LEVEL ) {
        threadInfoToLogs();
      }

      prevCheckThreadsTime = System.currentTimeMillis();
    }

  }

  private static void threadInfoToLogs() {
    Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
    globalLogger.debug( "+++++++++++++++++++++++++++++++++++++++++++++++++++++" ); //$NON-NLS-1$
    globalLogger.debug( "thread count = %d", Integer.valueOf( threads.size() ) ); //$NON-NLS-1$
    for( Thread t : threads.keySet() ) {
      globalLogger.debug( "=====================================================" ); //$NON-NLS-1$
      globalLogger.debug( "thread = %s", t.getName() ); //$NON-NLS-1$
      StackTraceElement[] seArray = threads.get( t );
      for( int index = 0, n = seArray.length; index < n; index++ ) {
        StackTraceElement se = seArray[index];
        globalLogger.debug( "   %s, %d, %s", se.getFileName(), Integer.valueOf( se.getLineNumber() ), //$NON-NLS-1$
            se.getMethodName() );
      }
    }
  }

  /**
   * Класс счётчика загруженности. Реализация с суммированием всех потоков программы (а не системы целиком)
   *
   * @author max
   */
  static class CpuLoadCalculator
      implements Runnable {

    private static long calcPeriod = 5000L;

    private double cpuLoad = 0;

    long prevSystemTime;
    long prevThreadsTime;
    int  cpuCount;

    void init() {
      try {
        prevSystemTime = System.nanoTime();
        prevThreadsTime = 0;
        cpuCount = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
      }
      catch( Exception e ) {
        globalLogger.error( e );
        return;
      }

      Thread t = new Thread( this );
      t.setName( "L2 Thread CpuLoadCalculator" ); //$NON-NLS-1$
      t.setDaemon( true );
      t.start();
      globalLogger.info( "CPU Load Calculator started" ); //$NON-NLS-1$
    }

    synchronized double getCpuLoad() {
      return cpuLoad;
    }

    synchronized void setCpuLoad( double aCpuLoad ) {
      cpuLoad = aCpuLoad;
    }

    @Override
    public void run() {
      while( true ) {
        try {
          Thread.sleep( calcPeriod );
          calcCpuLoad();
        }
        catch( Exception e ) {
          globalLogger.error( e );
          break;
        }
      }

    }

    private void calcCpuLoad() {

      ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
      long[] threadIds = mxBean.getAllThreadIds();

      long total = 0;
      for( long id : threadIds ) {
        total += mxBean.getThreadCpuTime( id );
      }

      long currSystemTime = System.nanoTime();

      double cpuLoadVal =
          ((double)(total - prevThreadsTime) / ((double)(cpuCount * (currSystemTime - prevSystemTime))));

      setCpuLoad( cpuLoadVal );

      prevThreadsTime = total;
      prevSystemTime = currSystemTime;

    }
  }

  static class ThreadsMonitor
      implements Runnable {

    @Override
    public void run() {
      globalLogger.info( "L2 Threads Monitor Thread Started" );
      long count = 0;
      boolean first = true;
      while( true ) {
        count++;
        // проверка раз в секунду
        try {
          Thread.sleep( 1000L );
        }
        catch( InterruptedException e ) {
          globalLogger.error( e );
        }

        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

        long[] deadlockedThreads = mxBean.findDeadlockedThreads();
        long[] monitorDeadlockedThreads = mxBean.findMonitorDeadlockedThreads();

        // раз в 10 мин или если таки случилась блокировка (первый раз)
        if( count % 600 == 0 || ((deadlockedThreads != null || monitorDeadlockedThreads != null)) && first ) {
          // на тот случай если блокировка пропдёт (такое возможно вообще?)
          first = deadlockedThreads == null && monitorDeadlockedThreads == null;

          globalLogger.info( "<-------------" );

          globalLogger.info( "deadlockedThreads = %d", (deadlockedThreads == null ? 0 : deadlockedThreads.length) );
          globalLogger.info( "monitorDeadlockedThreads = %d",
              (monitorDeadlockedThreads == null ? 0 : monitorDeadlockedThreads.length) );

          if( deadlockedThreads != null ) {
            for( long tId : deadlockedThreads ) {
              ThreadInfo threadInfo = mxBean.getThreadInfo( tId );
              globalLogger.info( "deadlocked - %s - %s - %s", threadInfo.getThreadName(), threadInfo.getLockOwnerName(),
                  threadInfo.getLockInfo().toString() );
            }
          }

          if( monitorDeadlockedThreads != null ) {
            for( long tId : monitorDeadlockedThreads ) {
              ThreadInfo threadInfo = mxBean.getThreadInfo( tId );
              globalLogger.info( "monitorDeadlocked - %s - %s - %s", threadInfo.getThreadName(),
                  threadInfo.getLockOwnerName(), threadInfo.getLockInfo().toString() );
            }
          }

          globalLogger.info( "------------->" );

        }

      }

    }

  }

}

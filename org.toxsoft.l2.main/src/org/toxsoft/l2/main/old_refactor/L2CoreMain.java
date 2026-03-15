package org.toxsoft.l2.main.old_refactor;

import static org.toxsoft.l2.lib.main.IL2HardConstants.*;
import static org.toxsoft.l2.main.old_refactor.helpers.IL2MainSharedResources.*;
import static org.toxsoft.l2.main.old_refactor.helpers.L2AppUtils.*;

import java.io.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.dlm.*;
import org.toxsoft.l2.lib.dlm.impl.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.main.*;
import org.toxsoft.l2.lib.main.impl.*;
import org.toxsoft.l2.lib.net.*;
import org.toxsoft.l2.lib.reserve.*;
import org.toxsoft.l2.main.old_refactor.helpers.*;
import org.toxsoft.skf.dq.lib.impl.*;
import org.toxsoft.skf.rri.lib.impl.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Начало выполнения программы нижнего уровня.
 *
 * @author max
 */
public class L2CoreMain {

  // Время начала выполнения программы
  private static long startTime = System.currentTimeMillis();

  // Глобальный логер программы
  public static ILogger globalLogger = null;

  // устанавливается шатдаун хуком
  private static IProgramQuitCommand shutdownHookQuitCmd = null;

  // Поток шатдаун хука.
  private static ShutdownHookThread shutdownHookThread;

  // Признак, что можно завершать поток шатдаун хука
  private static boolean shutedDown = false;

  // Объект для подсчёта загруженности системы программой НУ.
  private static CpuLoadCalculator cpuLoadCalculator;

  // ------------------------------------------------------------------------------------
  //
  //

  public L2CoreMain() {
    // TODO Auto-generated constructor stub
  }

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
  public static boolean isShutedDown() {
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

  private static boolean stopComponent( String aComponentName, ICooperativeWorkerComponent aComponent,
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

  private static void startComponent( String aComponentName, ICooperativeWorkerComponent aComponent ) {
    TsNullArgumentRtException.checkNull( aComponent );

    globalLogger.info( "Start starting component: %s", aComponentName );
    long startStartingTime = System.currentTimeMillis();
    aComponent.start();
    globalLogger.info( "Component: %s - started in %d msec", aComponentName,
        (System.currentTimeMillis() - startStartingTime) );

  }

  /**
   * Application startup.
   *
   * @param aArgs String[] - command line arguments
   */
  public static void main( String[] aArgs ) {
    // Слежение за файлом конфигурации журнала log4j.xml
    LoggerWrapper.setScanPropertiesTimeout( 10000 );
    // инициализация глобального логера
    globalLogger = LoggerWrapper.getLogger( L2CoreMain.class.getName() );
    LoggerUtils.setErrorLogger( globalLogger ); // to Goga scandir 2015.09.17 by Max
    shutdownHookThread = new ShutdownHookThread( globalLogger );
    cpuLoadCalculator = new CpuLoadCalculator( globalLogger );

    // регистрируем службу НСИ
    SkCoreUtils.registerSkServiceCreator( SkRegRefInfoService.CREATOR );
    SkCoreUtils.registerSkServiceCreator( SkDataQualityService.CREATOR );

    sayHello( startTime );

    Thread monitor = new Thread( new ThreadsMonitor( globalLogger ), "L2 Threads Monitor Thread" );
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
      IOptionSetEdit ops = new OptionSet();
      OptionSetUtils.initOptionSet( ops, IGlobalOps.ALL_L2_GLOBAL_OPS );

      // OptionSetUtils.nvAdd( ops, readConfigFile( new File( L2_MAIN_CFG_FILE_NAME ) ) );
      ops.addAll( L2AppUtils.readConfigFile( new File( L2_MAIN_CFG_FILE_NAME ) ) );
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
      appComponent = createApp( globalContext );

      // инициализировать менеджер плагинов и модулей
      globalLogger.info( MSG_CREATE_DLM );
      dlmMgr = DlmManagerUtils.createDlmManager( globalContext );

      // запустить главный цикл / поток
      globalLogger.info( MSG_START_HAL );
      // TODO: 2024-02-10 mvkd
      startComponent( "HAL", hal );

      globalLogger.info( MSG_START_NETWORK );
      startComponent( "NET", net );

      globalLogger.info( "Stating reserve module" );
      startComponent( "RESERVE", reserve );

      // globalLogger.info( MSG_START_DLM );
      // dlmMgr.start();

      globalLogger.info( MSG_START_APP );
      startComponent( "APP", appComponent );

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

    globalLogger.info( MSG_MAIN_LOOP_STARTED );
    IProgramQuitCommand quitCmd = null;

    // первоначально - модуль dlm не запущен
    try {
      while( quitCmd == null ) {
        try {
          long c = System.currentTimeMillis();
          long s = c;

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
            Thread.sleep( globalContext.globalOps().getLong( IGlobalOps.MAIN_LOOP_SLEEP_MSECS ) );
            // TODO: mvkd
            if( d0 + d1 + d2 + d3 + d4 + readDevicesDelay + writeDevicesDelay
                + globalContext.globalOps().getLong( IGlobalOps.MAIN_LOOP_SLEEP_MSECS ) > 1000 ) {
              System.out.println( "+++++++++++++++++++++++ MVK d0 = " + d0 + ", d1 = " + d1 + ", d2 = " + d2 + ", d3 = "
                  + d3 + ", d4 = " + d4 );
            }

          }
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

    // штатное завершение: завершить менеджеры, отключиться от ВУ, потом НУ, потом перезапуск с кодом выхода
    try {
      try {
        stopComponent( "APP", appComponent, 5000 );
        stopComponent( "RESERVE", reserve, 5000 );
        // if( !isSuspend ) {
        // stopComponent2( "DLM", dlmMgr, 5000 );
        // }
        stopComponent( "NET", net, 5000 );
        stopComponent( "HAL", hal, 5000 );

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

        L2AppUtils.sayGoodbye( quitCmd, startTime );
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

}

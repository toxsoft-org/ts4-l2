package ru.toxsoft.l2.core.hal.devices;

import static ru.toxsoft.l2.core.hal.IHalHardConstants.*;

import java.util.concurrent.locks.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.reserve.*;

/**
 * Аппарат ввода-вывода.
 *
 * @author MAX
 */
public abstract class AbstractHalIoDevice
    extends Stridable
    implements Runnable, IHealthMeasurable {

  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Режим, в котором работает поток аппарата.
   *
   * @author max
   */
  protected enum EMode {
    READ,
    WRITE,
    EMPTY,
    ERROR,
    CLOSING
  }

  /**
   * Перечисление возможных методов, вызываемых контейнером.
   *
   * @author max
   */
  protected enum ENextMethod {
    READ_DEVICE,
    WRITE_VALUES,
    READ_VALUES,
    ANY
  }

  /**
   * Обработчик исключительных ситуаций НУ.
   */
  protected IHalErrorProcessor errorProcessor;

  /**
   * Монитор потока обмена с физ. устройств.
   */
  private Lock lock = new ReentrantLock();

  private Condition readyCondition = lock.newCondition();

  /**
   * Монитор переменной, отображающей состояние
   */
  private ReentrantReadWriteLock modeLock = new ReentrantReadWriteLock();

  /**
   * Переменная, отображающая состояние "Идёт процесс записи на физ. устройство".
   */
  private EMode mode = EMode.EMPTY;

  /**
   * Монитор переменной, отображающей состояние "Аппарат работает".
   */
  // private Object isRunLock = new HallRunLock();

  /**
   * Переменная, отображающая состояние "Аппарат работает" - запущен поток аппапата.
   */
  // private boolean isRun = false;

  /**
   * Указание на то, какой метод должен быть вызван контейнером. вызов другого метода будет проигнорирован.
   */
  protected ENextMethod nextMethod = ENextMethod.ANY;

  /**
   * Поток аппарата.
   */
  private Thread apparatThread;

  protected Exception error = null;

  /**
   * Конструктор.
   *
   * @param aId String - идентификатор.
   * @param aDescription String - описание.
   * @param aErrorProcessor IHalErrorProcessor - обработчик ошибок.
   */
  public AbstractHalIoDevice( String aId, String aDescription, IHalErrorProcessor aErrorProcessor ) {
    super( aId, aDescription, aDescription, true );

    errorProcessor = aErrorProcessor;
  }

  //
  // ------------------------------------------------------------------------------------
  // внутренние потокозащищённые методы проверки и установки состояния

  protected EMode getMode() {

    modeLock.readLock().lock();
    try {
      return mode;
    }
    finally {
      modeLock.readLock().unlock();
    }
  }

  private void setMode( EMode aMode ) {

    modeLock.writeLock().lock();
    try {
      mode = aMode;
    }
    finally {
      modeLock.writeLock().unlock();
    }
  }

  //
  // ------------------------------------------------------------------------------------------
  // методы вызывамые каркасом

  /**
   * Запускает вспомогательный поток записи-чтения с физ уровня.
   */
  public void startThread() {
    // if( isRun() ) {
    // return;
    // }
    // setRun( true );
    setMode( EMode.EMPTY );
    // logger.debug( "Start L2 Thread of Device: %s", id() ); //$NON-NLS-1$
    apparatThread = new Thread( this );
    apparatThread.setName( String.format( "L2 Thread of Device: %s", id() ) ); //$NON-NLS-1$
    apparatThread.setPriority( Thread.MIN_PRIORITY );
    apparatThread.start();
  }

  /**
   * Начинает процесс закрытия (освобождения ресурсов). Должен забирать управление на короткое время.
   */
  public void close() {
    // setRun( false );

    // если поток заблокирован и ожидает изменение состояния
    lock.lock();
    try {
      setMode( EMode.CLOSING );
      readyCondition.signal();
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * Небезопасное завершение работы драйвера. Не гарантирует остановки драйвера, сообщая, что драйвер завершил работу.
   */
  public void destroy() {
    apparatThread = null;
  }

  /**
   * Проверяет закрытие (освобождение ресурсов). Должен забирать управление на короткое время.
   *
   * @return boolean - <b>true</b> - спец оборудование закрыто,
   *         <p>
   *         <b>false</b> - спец оборудование не закрыто.
   */
  public boolean isClosed() {
    // logger.error( "isClosed %b", (apparatThread == null || !apparatThread.isAlive()) );
    return apparatThread == null || !apparatThread.isAlive();
  }

  /**
   * Записывает текущие значения в буфер и инициирует процесс записи знчений из буфера на физ. уровень. Если выполнение
   * метода невозможно в силу того, что предыдущий процесс записи или чтения не был завершён, будет вызван метод
   * {@link #writeValuesWrongMode(EMode)}.
   */
  public void writeValues() {
    EMode currMode = checkErrorMode();

    if( currMode != EMode.EMPTY ) {

      if( nextMethod == ENextMethod.ANY ) {
        nextMethod = ENextMethod.WRITE_VALUES;
      }

      writeValuesWrongMode( currMode );
      return;
    }

    if( nextMethod == ENextMethod.ANY || nextMethod == ENextMethod.WRITE_VALUES ) {
      nextMethod = ENextMethod.ANY;

      if( currMode != EMode.EMPTY ) {
        logger.error( "CHECK HAL THREADS: writeValues Mode must be EMPTY" );
      }

      // блокируем буфер значений на запись
      lock.lock();
      try {
        putInBufferOutputValues();

        setMode( EMode.WRITE );

        readyCondition.signal();
      }
      finally {
        lock.unlock();
      }
    }
  }

  /**
   * Инициирует процесс чтения значений с физ. уровня. Если выполнение метода невозможно в силу того, что предыдущий
   * процесс записи или чтения не был завершён, будет вызван метод {@link #readDevicesWrongMode(EMode)}.
   */
  public void readDevices() {
    EMode currMode = checkErrorMode();

    if( currMode != EMode.EMPTY ) {

      if( nextMethod == ENextMethod.ANY ) {
        nextMethod = ENextMethod.READ_DEVICE;
      }

      readDevicesWrongMode( currMode );
      return;
    }

    if( nextMethod == ENextMethod.ANY || nextMethod == ENextMethod.READ_DEVICE ) {
      nextMethod = ENextMethod.ANY;

      if( currMode != EMode.EMPTY ) {
        logger.error( "CHECK HAL THREADS: readDevices Mode must be EMPTY" );
      }
      lock.lock();
      try {
        setMode( EMode.READ );
        readyCondition.signal();
      }
      finally {
        lock.unlock();
      }
    }
  }

  /**
   * Читает значения из буфера. Если выполнение метода невозможно в силу того, что предыдущий процесс записи или чтения
   * не был завершён, будет вызван метод {@link #readValuesWrongMode(EMode)}.
   */
  public void readValues() {
    EMode currMode = checkErrorMode();

    if( currMode != EMode.EMPTY ) {

      if( nextMethod == ENextMethod.ANY ) {
        nextMethod = ENextMethod.READ_VALUES;
      }

      readValuesWrongMode( currMode );
      return;
    }

    if( nextMethod == ENextMethod.ANY || nextMethod == ENextMethod.READ_VALUES ) {
      nextMethod = ENextMethod.ANY;

      if( currMode != EMode.EMPTY ) {
        logger.error( "CHECK HAL THREADS: readValues Mode must be EMPTY" );
      }
      lock.lock();
      try {
        getFromBufferInputValues();
      }
      finally {
        lock.unlock();
      }
    }

  }

  /**
   * Проверяет режим на значение "режим ошибки". В случае этого режима - передает сообщение "наверх" и продолжает
   * нормальную работу.
   *
   * @return EMode - режим потока аппарата после проверки.
   */
  private EMode checkErrorMode() {
    EMode cMode = getMode();
    if( cMode == EMode.ERROR ) {
      lock.lock();
      try {
        if( error != null ) {
          onError( error );
        }
        error = null;
        setMode( EMode.EMPTY );
        return EMode.EMPTY;
      }
      finally {
        lock.unlock();
      }
    }
    return cMode;
  }

  //
  // -----------------------------------------------------------------------------------
  // реализация отдельного аппаратного потока

  @Override
  public void run() {
    // logger.error( "Поток Начинается" ); //$NON-NLS-1$
    while( true ) {
      lock.lock();
      try {
        if( getMode() == EMode.CLOSING ) {
          break;
        }
        makeStep();
      }
      finally {
        lock.unlock();
      }
    }

    logger.info( "APPARAT %s is goining to close resources", id() );

    // теперь можно закрыть ресурсы (после отработки последнего шага) - во вспомогательном потоке
    try {
      closeApparatResources();
    }
    catch( Exception e ) {
      LoggerUtils.errorLogger().error( e );
    }
  }

  private void makeStep() {
    // функционал записи (чтения) на физ уровень
    // logger.error( "Поток продолжается" );

    // поток ждёт разрешение на доступ к буферу и обмену с физ. уровнем
    while( getMode() == EMode.EMPTY || getMode() == EMode.ERROR ) {
      try {
        readyCondition.await();
      }
      catch( InterruptedException e ) {
        logger.error( e );
      }
    }

    // запись (чтение) в физ. уровнь идёт сразу из
    // буфера - буфер синхронизирован и запись (чтение) в него из другого потока не
    // может вестись
    // пока не закончится обмен с физ. уровнем

    try {
      switch( getMode() ) {
        case WRITE: {
          // далее запись на физ уровень
          writeValuesOnLL();
          if( getMode() != EMode.WRITE ) {
            logger.error( "CHECK HAL THREADS: Mode must be WRITE" );
          }
          break;
        }
        case READ: {
          // далее чтение с физ уровень
          readValuesFromLL();
          if( getMode() != EMode.READ ) {
            logger.error( "CHECK HAL THREADS: Mode must be READ" );
          }
          break;
        }
        case EMPTY:
          return;
        case ERROR:
          return;
        case CLOSING:
          return;
        default:
          break;

      }
    }
    catch( Exception e ) {
      error = e;
      setMode( EMode.ERROR );
      return;
    }

    // далее обнуляем режим
    setMode( EMode.EMPTY );

  }

  //
  // --------------------------------------------------------------------------
  // общие служебные методы, которые не понятно куда отнести

  // private void setRun( boolean aIsRun ) {
  // synchronized (isRunLock) {
  // isRun = aIsRun;
  // }
  // }
  //
  // private boolean isRun() {
  // synchronized (isRunLock) {
  // return isRun;
  // }
  // }

  //
  // -----------------------------------------
  // методы выполняющие работу с физ уровнем - делают это уже в отдельном
  // потоке
  // внутри можно организовать ещё один отдельный поток - тогда его завершение
  // не будет контролироваться по времени

  /**
   * Записывает данные из буфера на нижний уровень, в отдельном специальном потоке, контролируется по времени.
   *
   * @throws TsMultipleApparatRtException - ошибка чтения данных с нижнего уровня, учитывается возможность наличия
   *           нескольких ошибок за одно обращение к устройствам (например устройств несколько).
   */
  protected abstract void readValuesFromLL()
      throws TsMultipleApparatRtException;

  /**
   * Считывает данные в буфер с нижнего уровня, в отдельном специальном потоке, контролируется по времени.
   *
   * @throws TsMultipleApparatRtException - ошибка чтения данных с нижнего уровня, учитывается возможность наличия
   *           нескольких ошибок за одно обращение к устройствам (например устройств несколько).
   */
  protected abstract void writeValuesOnLL()
      throws TsMultipleApparatRtException;

  //
  // -------------------------------------------------------
  // методы обмена текущих данных (DLM) с буфером

  /**
   * Записывает в буфер выходные данные (полученные от функциональных модулей), в основном потоке.
   */
  protected abstract void putInBufferOutputValues();

  /**
   * Считывает входные данные (предназначенные для использования в функциональных модулях) из буфера , в основном
   * потоке.
   */
  protected abstract void getFromBufferInputValues();

  //
  // -------------------------------------------------------------------------------------------
  // метод осовобождения низкоуровневых ресурсов

  /**
   * Освобождает низкоуровненвые ресурсы.
   *
   * @throws Exception - ошибка во время закрытия ресурсов
   */
  protected abstract void closeApparatResources()
      throws Exception;

  //
  // ----------------------------------------------------------
  // методы обработки исключительных ситуаций, появляющихся в конкретных методах

  /**
   * Метод обработки исключения: метод {@link #writeValues()} вызван в тот момент, когда не был завершён предыдущий
   * процесс чтения (записи) с физ. уровня.
   *
   * @param currMode {@link EMode} - режим, в котором на момент вызова метода {@link #writeValues()} находился поток
   *          аппарата.
   */

  protected void writeValuesWrongMode( @SuppressWarnings( "unused" ) EMode currMode ) {
    // по мере необходимости реализуется в наследниках
  }

  /**
   * Метод обработки исключения: метод {@link #readDevices()} вызван в тот момент, когда не был завершён предыдущий
   * процесс чтения (записи) с физ. уровня.
   *
   * @param currMode {@link EMode} - режим, в котором на момент вызова метода {@link #readDevices()} находился поток
   *          аппарата.
   */
  protected void readDevicesWrongMode( @SuppressWarnings( "unused" ) EMode currMode ) {
    // по мере необходимости реализуется в наследниках
  }

  /**
   * Метод обработки исключения: метод {@link #readValues()} вызван в тот момент, когда не был завершён предыдущий
   * процесс чтения (записи) с физ. уровня.
   *
   * @param currMode {@link EMode} - режим, в котором на момент вызова метода {@link #readValues()} находился поток
   *          аппарата.
   */
  protected void readValuesWrongMode( @SuppressWarnings( "unused" ) EMode currMode ) {
    // по мере необходимости реализуется в наследниках
  }

  /**
   * Метод передачи ошибки обмена с физ уровнем на уровень выше - вызывается только в основном потоке. Это одна из
   * возможных реализаций метода.
   *
   * @param aError Exception - ошибка обмена с физ уровнем.
   */
  protected void onError( Exception aError ) {
    if( errorProcessor != null ) {

      if( aError instanceof TsMultipleApparatRtException ) {
        errorProcessor.onApparatError( ((TsMultipleApparatRtException)aError).getCauses() );
      }
      else {
        ApparatError apparatError = new ApparatError( id(), COMMON_APPARAT_ERROR_ID, null, aError );
        errorProcessor.onApparatError( new ElemArrayList<>( apparatError ) );
      }
    }

  }

  static class HallLock {
    //
  }

  static class HallModeLock {
    //
  }

  static class HallRunLock {
    //
  }
}

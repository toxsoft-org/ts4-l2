package org.toxsoft.l2.thd.modbus.common;

import static org.toxsoft.l2.thd.modbus.common.IModbusThdConstants.*;

import java.lang.reflect.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.thd.opc.*;

import net.wimpi.modbus.*;
import net.wimpi.modbus.io.*;
import net.wimpi.modbus.msg.*;
import net.wimpi.modbus.procimg.*;
import net.wimpi.modbus.util.*;

/**
 * Универсальное устройство, работающее по протоколу Modbus. <br>
 * ip+port (tcp) или <br>
 * port (rtu) - т.е определяется коннектом <br>
 * один девайс - один коннект (устройства просто рассовываются по разным буферам, если устройств несколько)
 *
 * @author max
 */
public class CommonModbusDevice
    extends AbstractSpecificDevice
    implements ITsOpc {

  /**
   * Интервал между запросами.
   */
  private static final long REQUESTS_INTERVAL = 100L; // 20L;

  private static final int MAX_PERMIS_READ_ERROR_COUNT = 3;

  enum EDataType
      implements IStridable {

    DI( "DI", "Discret Inputs", DIReadValuesBufferImpl.class, null ), //$NON-NLS-1$ //$NON-NLS-2$
    DO( "DO", "Discret Outputs", DOReadValuesBufferImpl.class, DOWriteValuesBuffer.class ), //$NON-NLS-1$ //$NON-NLS-2$
    AI( "AI", "Analog Inputs", AIReadValuesBufferImpl.class, null ), //$NON-NLS-1$ //$NON-NLS-2$
    AO( "AO", "Analog Outputs", AOReadValuesBufferImpl.class, AOWriteValuesBuffer.class );//$NON-NLS-1$ //$NON-NLS-2$

    private String id;

    private String descr;

    private Constructor<?> bufferConstructor;

    private Constructor<?> outBufferConstructor;

    EDataType( String aId, String aDescr, Class<?> aBufferClass, Class<?> aOutBufferClass ) {
      this.id = aId;
      this.descr = aDescr;
      this.bufferConstructor = aBufferClass.getConstructors()[0];
      this.outBufferConstructor = aOutBufferClass != null ? aOutBufferClass.getConstructors()[0] : null;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public String description() {
      return descr;
    }

    public ValuesBufferImpl<?> createInBuffer( CommonModbusDevice aParent, int aDevice, int aStartReg )
        throws InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {
      return (ValuesBufferImpl<?>)bufferConstructor.newInstance( aParent, Integer.valueOf( aDevice ),
          Integer.valueOf( aStartReg ) );
    }

    public IValuesBuffer createOutBuffer( CommonModbusDevice aParent, WriteTagImpl aTag, int aDevice, int aStartReg )
        throws InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {
      return (IValuesBuffer)outBufferConstructor.newInstance( aParent, aTag, Integer.valueOf( aDevice ),
          Integer.valueOf( aStartReg ) );
    }

    @Override
    public String nmName() {
      return id;
    }
  }

  /**
   * массив буферов
   */
  private IListEdit<IValuesBuffer> buffers = new ElemArrayList<>();

  /**
   * массив буферов на запись
   */
  private IListEdit<IValuesBuffer> writeBuffers = new ElemArrayList<>();

  /**
   * Все теги.
   */
  private IStringMapEdit<ITag> tags = new StringMap<>();

  /**
   * Создатель транзакций
   */
  private ITransactionCreator transactionCreator;

  /**
   * Конструктор по идентфикатору и настройкам
   *
   * @param aId - идентфикатор
   * @param aDescription - описание
   * @param aDefinition - настройки
   * @param aErrorProcessor - обработчик ошибок
   */
  public CommonModbusDevice( String aId, String aDescription, IAvTree aDefinition,
      IHalErrorProcessor aErrorProcessor ) {
    super( aId, aDescription, aErrorProcessor );
    configYourself( aDefinition );
  }

  /**
   * Проводит настройку устройства (создание соединения, тегов и буферов)
   *
   * @param aDefinition {@link IAvTree} - все необходимые для настройки параметры.
   */
  private void configYourself( IAvTree aDefinition ) {
    // конфигурация соединения
    configConnection( aDefinition.fields() );
    IAvTree devicesMassiv = aDefinition.nodes().findByKey( DEVICES_PARAM_ID );

    for( int i = 0; i < devicesMassiv.arrayLength(); i++ ) {
      IAvTree deviceParams = devicesMassiv.arrayElement( i );
      configDevice( deviceParams );
    }
  }

  @SuppressWarnings( "null" )
  private void configDevice( IAvTree aDeviceParams ) {
    IMapEdit<EDataType, IListEdit<ValuesBufferImpl<?>>> deviceBuffers = new ElemMap<>();
    IListEdit<ValuesBufferImpl<?>> typeBuffers = null;

    IAvTree tagsMassiv = aDeviceParams.nodes().findByKey( TAGS_PARAM_ID );
    int tagDevAddress = aDeviceParams.fields().getInt( DEV_ADDRESS_PARAM_ID, 1 );
    for( int i = 0; i < tagsMassiv.arrayLength(); i++ ) {
      IAvTree tagParams = tagsMassiv.arrayElement( i );
      String tagId = tagParams.fields().getStr( ID_PARAM_ID );
      int tagRegister = tagParams.fields().getInt( REGISTER_PARAM_ID );

      String tagRequestType = tagParams.fields().getStr( REQUEST_TYPE_PARAM_ID );
      EDataType requestType = EDataType.valueOf( tagRequestType );

      // втавка для DO
      if( tagParams.fields().hasValue( IS_OUTPUT_PARAM_ID ) && tagParams.fields().getBool( IS_OUTPUT_PARAM_ID ) ) {
        WriteTagImpl wTag = new WriteTagImpl( tagId );
        try {
          IValuesBuffer wBuffer =
              requestType.createOutBuffer( CommonModbusDevice.this, wTag, tagDevAddress, tagRegister );
          // Добавляем созданный тег в общий массив тегов
          tags.put( wTag.tagId(), wTag );
          // Добавляем созданный буфер в массив буферов на запись
          writeBuffers.add( wBuffer );
        }
        catch( Exception e ) {
          LoggerUtils.errorLogger().error( e );
        }

        continue;
      }

      int tagWordsCount = tagParams.fields().getInt( WORDS_COUNT_PARAM_ID );
      if( deviceBuffers.hasKey( requestType ) ) {
        typeBuffers = deviceBuffers.getByKey( requestType );
      }
      else {
        typeBuffers = new ElemArrayList<>();
        deviceBuffers.put( requestType, typeBuffers );
      }

      String tagTranslator = tagParams.fields().getStr( TRANSLATOR_PARAM_ID );
      String tagTranslatorParams = tagParams.fields().getStr( TRANSLATOR_PARAMS_PARAM_ID, TsLibUtils.EMPTY_STRING );

      ValuesBufferImpl<?> propperBuffer = null;
      for( ValuesBufferImpl<?> buffer : typeBuffers ) {
        if( buffer.getNextRegNumber() == tagRegister ) {
          propperBuffer = buffer;
          break;
        }
      }
      if( propperBuffer == null ) {
        try {
          propperBuffer = requestType.createInBuffer( CommonModbusDevice.this, tagDevAddress, tagRegister );
          typeBuffers.add( propperBuffer );

          // Добавляем созданный буфер в общий массив буферов
          buffers.add( propperBuffer );
        }
        catch( Exception e ) {
          LoggerUtils.errorLogger().error( e );
        }

      }

      try {
        Object translator = ETranslators.createTranslator( tagTranslator, tagTranslatorParams );
        TagImpl tag = new TagImpl( tagId );
        propperBuffer.addTag( tag, tagWordsCount, translator );

        // Добавляем созданный тег в общий массив тегов
        tags.put( tag.tagId(), tag );
      }
      catch( Exception e ) {
        LoggerUtils.errorLogger().error( e );
      }

    }

    // после того как все теги устройства сконфиурированы:
    // добавить все буфера в общий массив

  }

  private void configConnection( IOptionSet aConnectionParams ) {
    String type = aConnectionParams.getStr( MODBUS_TYPE_PARAM_ID );
    try {
      transactionCreator = ETransactionCreators.createTransactionCreator( type );
      transactionCreator.config( aConnectionParams );
    }
    catch( Exception e ) {
      LoggerUtils.errorLogger().error( e );
    }

  }

  @Override
  public ITag tag( String aTagId ) {
    return tags.findByKey( aTagId );
  }

  @Override
  public IStringMap<ITag> tags() {
    return tags;
  }

  @Override
  protected void readValuesFromLL()
      throws TsMultipleApparatRtException {
    LoggerUtils.defaultLogger().info( "Buffer size = %s", String.valueOf( buffers.size() ) );
    for( IValuesBuffer b : buffers ) {
      ValuesBufferImpl vbi = (ValuesBufferImpl)b;
      LoggerUtils.defaultLogger().info( "Buffer: start = %s, count = %s", String.valueOf( vbi.startReg ),
          String.valueOf( vbi.wordsCount ) );
      b.doJob();
      try {
        Thread.sleep( REQUESTS_INTERVAL );
      }
      catch( InterruptedException e ) {
        LoggerUtils.errorLogger().error( e );
      }
    }

  }

  @Override
  protected void writeValuesOnLL()
      throws TsMultipleApparatRtException {
    for( IValuesBuffer b : writeBuffers ) {
      b.doJob();
    }
  }

  @Override
  protected void putInBufferOutputValues() {
    for( IValuesBuffer b : writeBuffers ) {
      b.change();
    }

  }

  @Override
  protected void getFromBufferInputValues() {
    for( IValuesBuffer b : buffers ) {
      b.change();
    }
  }

  @Override
  protected void closeApparatResources()
      throws Exception {
    transactionCreator.close();
  }

  ModbusTransaction createModbusTransaction()
      throws TsIllegalStateRtException {

    ModbusTransaction trans = transactionCreator.createModbusTransaction();
    return trans;
  }

  /**
   * Буфер, имеющий непрерывные адреса регистров (т.е. читается одной пачкой). Очевидно, что для разных адресов
   * устройств - разные объекты буферов. <br>
   * <ul>
   * <li><b>адрес устройства (номер 0-255)</b> - настроечный параметр</li>
   * <li><b>tags map</b> - inner tags atomic values</li>
   * <li><b>start register number</b> - из настроечных</li>
   * <li><b>length</b> - из настроечных</li>
   * <li><b>modbus command type</b> - определяется типом тегов в буфере (DI, DA)</li>
   * <li><b>map tag id</b> - translator (translator params)</li>
   * <li><b>map tag id</b> - start word number, words</li>
   * <li><b>count</b></li>
   * </ul>
   * <br>
   *
   * @author max
   */
  interface IValuesBuffer {

    /**
     * Чтение в буфер (или запись из буфер - в будущем)
     */
    void doJob();

    /**
     * Перемещение данных из буфера в тег (или из тега в буфер - в будущем)
     */
    void change();
  }

  abstract class WriteValuesBufferImpl
      implements IValuesBuffer {

    private int writeErrorCount = 0;

    protected WriteTagImpl tag;

    protected int device;

    protected int reg;

    protected boolean changed = true;

    public WriteValuesBufferImpl( WriteTagImpl aTag, int aDevice, int aReg ) {
      tag = aTag;
      device = aDevice;
      reg = aReg;
    }

    @Override
    public final void doJob() {
      try {
        doJobImpl();
        // если запись ОК - обнулить счётчик ошибок подряд
        writeErrorCount = 0;
      }
      catch( TsIllegalStateRtException | ModbusException e ) {
        writeErrorCount++;
        if( writeErrorCount > MAX_PERMIS_READ_ERROR_COUNT ) {
          // если ошибочных записей подряд больше заданного количества - буферы должны отработать
          writeError();
          // dima 26.02.25 It seems connection break, close resources
          try {
            closeApparatResources();
          }
          catch( Exception ex ) {
            LoggerUtils.errorLogger().error( ex );
          }
        }
        else {
          LoggerUtils.errorLogger().error( e, "Write error count: %s", String.valueOf( writeErrorCount ) );
        }
      }
    }

    public abstract void doJobImpl()
        throws ModbusException;

    protected abstract void writeError();
  }

  /**
   * Буфер чтения данных, расположенных непрерывно, начиная с некоторого регистра на протяжении одного или нескольких
   * регистров , источником которых является одно устройство.
   *
   * @author max
   * @param <T> - класс инжектора.
   */
  abstract class ValuesBufferImpl<T extends TagValueInjector>
      implements IValuesBuffer {

    private int readErrorCount = 0;

    protected int startReg = 0;

    protected int wordsCount = 0;

    protected int device = 0;

    protected IListEdit<T> injectors = new ElemArrayList<>();

    public ValuesBufferImpl( int aDevice, int aStartReg ) {
      this.startReg = aStartReg;
      device = aDevice;
    }

    @Override
    public final void doJob() {
      try {
        doJobImpl();
        // если чтение ОК - обнулить счётчик ошибок подряд
        readErrorCount = 0;
      }
      catch( TsIllegalStateRtException | ModbusException | java.lang.ArrayIndexOutOfBoundsException e ) {
        readErrorCount++;
        if( readErrorCount > MAX_PERMIS_READ_ERROR_COUNT ) {
          // если ошибочных чтений подряд больше заданного количества - буферы должны отработать
          for( T inj : injectors ) {
            inj.readError();
          }
          // dima 26.02.25 It seems connection break, close resources
          try {
            closeApparatResources();
          }
          catch( Exception ex ) {
            LoggerUtils.errorLogger().error( ex );
          }
        }
        else {
          LoggerUtils.errorLogger().error( e, "Read error count: %s", String.valueOf( readErrorCount ) );
        }

      }
    }

    public abstract void doJobImpl()
        throws ModbusException;

    @Override
    public void change() {
      for( T inj : injectors ) {
        inj.injectValue();
      }
    }

    /**
     * Возвращает номер регистра, который должен быть следующим
     *
     * @return int - номер регистра, который должен быть следующим
     */
    int getNextRegNumber() {
      return startReg + wordsCount;
    }

    void addTag( TagImpl aTagImpl, int aLength, Object aTranslator ) {
      T injector = createInjector( aTagImpl, wordsCount, aLength, aTranslator );
      wordsCount += aLength;
      injectors.add( injector );
    }

    protected abstract T createInjector( TagImpl aTagImpl, int aStart, int aLength, Object aTranslator );
  }

  /**
   * Буфер чтения данных, расположенных непрерывно, начиная с некоторого регистра на протяжении одного или нескольких
   * регистров , источником которых является одно устройство. FC 1
   *
   * @author max
   */
  class DOReadValuesBufferImpl
      extends ValuesBufferImpl<DiscretTagValueInjector> {

    public DOReadValuesBufferImpl( int aDevice, int aStartReg ) {
      super( aDevice, aStartReg );
    }

    @Override
    public void doJobImpl()
        throws ModbusException {
      // запрос и получение ответа в байтах

      // запрос FC 1
      ReadCoilsRequest request = new ReadCoilsRequest( startReg, wordsCount );
      request.setUnitID( device );
      // транзакция
      ModbusTransaction trans = createModbusTransaction();
      trans.setRequest( request );

      // исполнение транзакции
      trans.execute();

      // ответ
      ReadCoilsResponse res = (ReadCoilsResponse)trans.getResponse();

      BitVector vector = res.getCoils();

      boolean[] inputMassive = new boolean[vector.size()];
      for( int j = 0; j < vector.size(); j++ ) {
        inputMassive[j] = vector.getBit( j );
      }

      for( DiscretTagValueInjector inj : injectors ) {
        inj.readValue( inputMassive );
      }
    }

    @Override
    protected DiscretTagValueInjector createInjector( TagImpl aTagImpl, int aStart, int aLength, Object aTranslator ) {
      TsIllegalArgumentRtException.checkFalse( aTranslator instanceof IDiscretTranslator );
      IDiscretTranslator translator = (IDiscretTranslator)aTranslator;

      return new DiscretTagValueInjector( aTagImpl, aStart, aLength, translator );
    }

  }

  /**
   * Буфер чтения данных, расположенных непрерывно, начиная с некоторого регистра на протяжении одного или нескольких
   * регистров , источником которых является одно устройство. FC 2
   *
   * @author max
   */
  class DIReadValuesBufferImpl
      extends ValuesBufferImpl<DiscretTagValueInjector> {

    public DIReadValuesBufferImpl( int aDevice, int aStartReg ) {
      super( aDevice, aStartReg );
    }

    @Override
    public void doJobImpl()
        throws ModbusException {
      // запрос и получение ответа в байтах

      // запрос FC 2
      ReadInputDiscretesRequest request = new ReadInputDiscretesRequest( startReg, wordsCount );
      request.setUnitID( device );
      // транзакция
      ModbusTransaction trans = createModbusTransaction();
      trans.setRequest( request );

      // исполнение транзакции
      trans.execute();

      // ответ
      ReadInputDiscretesResponse res = (ReadInputDiscretesResponse)trans.getResponse();

      BitVector vector = res.getDiscretes();

      boolean[] inputMassive = new boolean[vector.size()];
      for( int j = 0; j < vector.size(); j++ ) {
        inputMassive[j] = vector.getBit( j );
      }

      for( DiscretTagValueInjector inj : injectors ) {
        inj.readValue( inputMassive );
      }
    }

    @Override
    protected DiscretTagValueInjector createInjector( TagImpl aTagImpl, int aStart, int aLength, Object aTranslator ) {
      TsIllegalArgumentRtException.checkFalse( aTranslator instanceof IDiscretTranslator );
      IDiscretTranslator translator = (IDiscretTranslator)aTranslator;

      return new DiscretTagValueInjector( aTagImpl, aStart, aLength, translator );
    }
  }

  /**
   * Запрос FC3
   *
   * @author max
   */
  class AOReadValuesBufferImpl
      extends ValuesBufferImpl<AnalogTagValueInjector> {

    public AOReadValuesBufferImpl( int aDevice, int aStartReg ) {
      super( aDevice, aStartReg );
    }

    @Override
    public void doJobImpl()
        throws ModbusException {
      // запрос и получение ответа в байтах

      // запрос FC 3
      ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest( startReg, wordsCount );
      request.setUnitID( device );
      // транзакция
      ModbusTransaction trans = createModbusTransaction();
      trans.setRequest( request );

      // исполнение транзакции
      trans.execute();

      // ответ
      ReadMultipleRegistersResponse res = (ReadMultipleRegistersResponse)trans.getResponse();

      Register[] regs = res.getRegisters();

      int[] inputMassive = new int[regs.length];

      for( int j = 0; j < inputMassive.length; j++ ) {
        inputMassive[j] = regs[j].getValue();
      }

      for( AnalogTagValueInjector inj : injectors ) {
        inj.readValue( inputMassive );
      }
    }

    @Override
    protected AnalogTagValueInjector createInjector( TagImpl aTagImpl, int aStart, int aLength, Object aTranslator ) {
      TsIllegalArgumentRtException.checkFalse( aTranslator instanceof IAnalogTranslator );
      IAnalogTranslator translator = (IAnalogTranslator)aTranslator;

      return new AnalogTagValueInjector( aTagImpl, aStart, aLength, translator );
    }

  }

  /**
   * Запрос FC4
   *
   * @author max
   */
  class AIReadValuesBufferImpl
      extends ValuesBufferImpl<AnalogTagValueInjector> {

    public AIReadValuesBufferImpl( int aDevice, int aStartReg ) {
      super( aDevice, aStartReg );
    }

    @Override
    public void doJobImpl()
        throws ModbusException {
      // запрос и получение ответа в байтах

      // запрос FC 4
      ReadInputRegistersRequest request = new ReadInputRegistersRequest( startReg, wordsCount );
      request.setUnitID( device );
      // транзакция
      ModbusTransaction trans = createModbusTransaction();
      trans.setRequest( request );
      // исполнение транзакции
      trans.execute();

      // ответ
      ReadInputRegistersResponse res = (ReadInputRegistersResponse)trans.getResponse();
      InputRegister[] regs = res.getRegisters();
      LoggerUtils.errorLogger().debug( "start reg = %d, regs count = %d", startReg, wordsCount ); //$NON-NLS-1$
      int[] inputMassive = new int[regs.length];

      for( int j = 0; j < inputMassive.length; j++ ) {
        inputMassive[j] = regs[j].getValue();
        LoggerUtils.errorLogger().debug( "%d,", regs[j].getValue() ); //$NON-NLS-1$
      }
      LoggerUtils.errorLogger().debug( "/n" ); //$NON-NLS-1$

      for( AnalogTagValueInjector inj : injectors ) {
        inj.readValue( inputMassive );
      }
    }

    @Override
    protected AnalogTagValueInjector createInjector( TagImpl aTagImpl, int aStart, int aLength, Object aTranslator ) {
      TsIllegalArgumentRtException.checkFalse( aTranslator instanceof IAnalogTranslator );
      IAnalogTranslator translator = (IAnalogTranslator)aTranslator;

      return new AnalogTagValueInjector( aTagImpl, aStart, aLength, translator );
    }

  }

  /**
   * Класс инжектора (механизма внедрения значения в тег), содержащий один тег, адрес данных в массиве БУФЕРА (а не в
   * общем массиве регистров modbus),транслятор, переводящий входные данные в конкретный тег.
   *
   * @author max
   */
  static abstract class TagValueInjector {

    /**
     * Тег, предоставляемый в виде интерфейса службе DLM.
     */
    private TagImpl tag;

    /**
     * Стартовый байт тега в буферном масиве - пересчитывается из настроечного параметра - номера регистра тега (через
     * номер начального регистра буфера)
     */
    protected int startTag = 0;

    /**
     * Длина тега в словах (битах-для дискретных или байтах-для аналоговых) - является настроечным параметром.
     */
    protected int length = 1;

    protected IAtomicValue bufferValue = IAtomicValue.NULL;

    public TagValueInjector( TagImpl aTag, int aStartTag, int aLength ) {
      this.tag = aTag;
      this.startTag = aStartTag;
      this.length = aLength;
    }

    void injectValue() {
      tag.setValue( bufferValue );
    }

    /**
     * Поведение буфера - чтение с устройства - в случае ошибки чтения с устройства.
     */
    protected abstract void readError();
  }

  /**
   * Класс инжектора для аналоговых запросов, содержащий аналоговый транслятор.
   *
   * @author max
   */
  static class AnalogTagValueInjector
      extends TagValueInjector {

    /**
     * Транслятор тега - его класс является настроечным параметром
     */
    protected IAnalogTranslator translator;

    public AnalogTagValueInjector( TagImpl aTag, int aStartTag, int aLength, IAnalogTranslator aTranslator ) {
      super( aTag, aStartTag, aLength );
      translator = aTranslator;
    }

    protected void readValue( int[] aInputMassive ) {
      int[] transBytes = length == 1 ? new int[] { aInputMassive[startTag] } : new int[length];
      if( length > 1 ) {
        System.arraycopy( aInputMassive, startTag, transBytes, 0, length );
      }
      bufferValue = translator.translate( transBytes );
    }

    @Override
    protected void readError() {
      // в случае ошибки - занулить
      bufferValue = IAtomicValue.NULL;
    }
  }

  /**
   * Класс инжектора для дискретных запросов, содержащий дискретный транслятор.
   *
   * @author max
   */
  static class DiscretTagValueInjector
      extends TagValueInjector {

    /**
     * Транслятор тега - его класс является настроечным параметром
     */
    protected IDiscretTranslator translator;

    public DiscretTagValueInjector( TagImpl aTag, int aStartTag, int aLength, IDiscretTranslator aTranslator ) {
      super( aTag, aStartTag, aLength );
      translator = aTranslator;
    }

    protected void readValue( boolean[] aInputMassive ) {
      boolean[] transBytes = length == 1 ? new boolean[] { aInputMassive[startTag] } : new boolean[length];
      if( length > 1 ) {
        System.arraycopy( aInputMassive, startTag, aInputMassive, 0, length );
      }
      bufferValue = translator.translate( transBytes );
    }

    @Override
    protected void readError() {
      // в случае ошибки - занулить
      bufferValue = IAtomicValue.NULL;
    }
  }

  /**
   * Реализация тега, предоставляемого в виде интерфейса службе DLM.
   *
   * @author max
   */
  static class TagImpl
      implements ITag {

    private IAtomicValue value = IAtomicValue.NULL;

    private ETagHealth health = ETagHealth.UNKNOWN;

    private String id;

    public TagImpl( String aId ) {
      this.id = aId;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public String description() {
      return TsLibUtils.EMPTY_STRING;
    }

    @Override
    public String tagId() {
      return id;
    }

    @Override
    public String name() {
      return TsLibUtils.EMPTY_STRING;
    }

    @Override
    public EKind kind() {
      return EKind.R;
    }

    @Override
    public EAtomicType valueType() {
      return value.atomicType();
    }

    @Override
    public IAtomicValue get() {
      return value;
    }

    @Override
    public void set( IAtomicValue aVal ) {
      // без реализации - снаружи не менять

    }

    @Override
    public ETagHealth health() {
      return health;
    }

    void setValue( IAtomicValue aValue ) {
      // dima 17.01.25
      updateHealthState( aValue );
      value = aValue;
    }

    private void updateHealthState( IAtomicValue aNewValue ) {
      // init stage
      if( health.equals( ETagHealth.UNKNOWN ) && aNewValue.isAssigned() ) {
        health = ETagHealth.WORKING;
        return;
      }
      if( health.equals( ETagHealth.UNKNOWN ) && !aNewValue.isAssigned() ) {
        health = ETagHealth.JUST_BROKEN;
        return;
      }
      // normal working stage
      if( health.equals( ETagHealth.WORKING ) && aNewValue.isAssigned() ) {
        // nothing to do
        return;
      }
      if( health.equals( ETagHealth.WORKING ) && !aNewValue.isAssigned() ) {
        health = ETagHealth.JUST_BROKEN;
        return;
      }
      // just broken stage
      if( health.equals( ETagHealth.JUST_BROKEN ) && !aNewValue.isAssigned() ) {
        health = ETagHealth.DEAD;
        return;
      }
      if( health.equals( ETagHealth.JUST_BROKEN ) && aNewValue.isAssigned() ) {
        health = ETagHealth.JUST_RECOVERED;
        return;
      }
      // just recovered stage
      if( health.equals( ETagHealth.JUST_RECOVERED ) && aNewValue.isAssigned() ) {
        health = ETagHealth.WORKING;
        return;
      }
      if( health.equals( ETagHealth.JUST_RECOVERED ) && !aNewValue.isAssigned() ) {
        health = ETagHealth.JUST_BROKEN;
        return;
      }
      // dead stage
      if( health.equals( ETagHealth.DEAD ) && aNewValue.isAssigned() ) {
        health = ETagHealth.JUST_RECOVERED;
        return;
      }
      if( health.equals( ETagHealth.DEAD ) && !aNewValue.isAssigned() ) {
        // nothing to do
        return;
      }
    }

    @Override
    public String nmName() {
      return id;
    }

    @Override
    public boolean isDirty() {
      return false;
    }
  }

  /**
   * Реализация тега на запись, предоставляемого в виде интерфейса службе DLM.
   *
   * @author max
   */
  static class WriteTagImpl
      implements ITag {

    private IAtomicValue value = IAtomicValue.NULL;

    private String id;

    public WriteTagImpl( String aId ) {
      this.id = aId;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public String description() {
      return TsLibUtils.EMPTY_STRING;
    }

    @Override
    public String tagId() {
      return id;
    }

    @Override
    public String name() {
      return TsLibUtils.EMPTY_STRING;
    }

    @Override
    public EKind kind() {
      return EKind.W;
    }

    @Override
    public EAtomicType valueType() {
      return value.atomicType();
    }

    @Override
    public IAtomicValue get() {
      return value;
    }

    @Override
    public void set( IAtomicValue aVal ) {
      value = aVal;
    }

    @Override
    public String nmName() {
      return id;
    }

    @Override
    public boolean isDirty() {
      return false;
    }

  }

  /**
   * Буфер записи единичного DO .
   *
   * @author max
   */
  class DOWriteValuesBuffer
      extends WriteValuesBufferImpl {

    private Boolean bufferValue = Boolean.FALSE;

    public DOWriteValuesBuffer( WriteTagImpl aTag, int aDevice, int aReg ) {
      super( aTag, aDevice, aReg );
    }

    @Override
    public void doJobImpl()
        throws ModbusException {

      if( changed && bufferValue != null ) {

        // запрос FC 5
        WriteCoilRequest request = new WriteCoilRequest( reg, bufferValue.booleanValue() );
        request.setUnitID( device );
        // транзакция
        ModbusTransaction trans = createModbusTransaction();
        trans.setRequest( request );

        // исполнение транзакции
        trans.execute();

        // ответ
        @SuppressWarnings( "unused" )
        WriteCoilResponse res = (WriteCoilResponse)trans.getResponse();
        changed = false;

      }

    }

    @Override
    public void change() {
      IAtomicValue val = tag.get();
      if( val.isAssigned() ) {
        boolean newVal = val.asBool();
        if( bufferValue == null || newVal != bufferValue.booleanValue() ) {
          changed = true;
          bufferValue = Boolean.valueOf( newVal );
        }
      }

    }

    @Override
    protected void writeError() {
      // TODO Auto-generated method stub

    }

  }

  /**
   * Буфер записи единичного AO .
   *
   * @author max
   */
  class AOWriteValuesBuffer
      extends WriteValuesBufferImpl {

    private Integer bufferValue;

    public AOWriteValuesBuffer( WriteTagImpl aTag, int aDevice, int aReg ) {
      super( aTag, aDevice, aReg );
    }

    @Override
    public void doJobImpl()
        throws ModbusException {

      if( changed && bufferValue != null ) {

        // запрос FC 6
        WriteSingleRegisterRequest request =
            new WriteSingleRegisterRequest( reg, new SimpleRegister( bufferValue.intValue() ) );
        request.setUnitID( device );

        // транзакция
        ModbusTransaction trans = createModbusTransaction();
        trans.setRequest( request );

        // исполнение транзакции
        trans.execute();

        // ответ
        @SuppressWarnings( "unused" )
        WriteSingleRegisterResponse res = (WriteSingleRegisterResponse)trans.getResponse();
        changed = false;
      }

    }

    @Override
    public void change() {
      IAtomicValue val = tag.get();
      if( val.isAssigned() ) {
        int newVal = val.asInt();
        if( bufferValue == null || newVal != bufferValue.intValue() ) {
          changed = true;
          bufferValue = Integer.valueOf( newVal );
        }
      }

    }

    @Override
    protected void writeError() {
      // TODO Auto-generated method stub
    }

  }
  // test emalator ArrayIndexOutOfBoundsException
  // public static void main( String[] a ) {
  // int startTag = 0;
  // int length = 2;
  // int[] aInputMassive = new int[1];
  // int[] transBytes = length == 1 ? new int[] { aInputMassive[startTag] } : new int[length];
  // if( length > 1 ) {
  // System.arraycopy( aInputMassive, startTag, transBytes, 0, length );
  // }
  // }

}

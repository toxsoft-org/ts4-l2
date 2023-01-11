package ge.toxsoft.gwp.opcuabridge.server.events;

import static ge.toxsoft.gwp.opcuabridge.server.events.IDlmsBaseConstants.*;
import static ge.toxsoft.gwp.opcuabridge.server.events.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Класс работы с событиями системы диспетчеризации.
 *
 * @author max
 */
public class EventModule
// extends ConfigurableWorkerModuleBase
{

  private static final String OPC_UA_BRIDGE = "OPC UA Bridge";

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст.
   */
  ITsContext context;

  /**
   * Соединение с сервером.
   */
  ISkConnection connection;

  /**
   * Объекты локальные отправитель сообщений.
   */
  private IListEdit<IEventSender> senders;

  /**
   * Конструктор по контексту.
   *
   * @param aContext {@link ITsContext} - контекст подгружаемых модулей.
   * @param aDlmInfo IDlmInfo - информация о DLM
   */
  public EventModule( ITsContext aContext ) {
    context = aContext;

  }

  protected void doConfigYourself( IAvTree aConfig )
      throws TsIllegalArgumentRtException {
    IAvTree eventDefs = aConfig.nodes().findByKey( EVENT_DEFS );

    senders = new ElemArrayList<>();

    if( eventDefs != null && eventDefs.isArray() ) {
      for( int i = 0; i < eventDefs.arrayLength(); i++ ) {
        IAvTree eventDef = eventDefs.arrayElement( i );

        IEventSender eventSender = createSender( eventDef );
        eventSender.config( eventDef );

        senders.add( eventSender );

      }
    }

  }

  // @Override
  protected void doStartComponent() {
    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_EVENT_MODULE_CANT_BE_STARTED_FORMAT, OPC_UA_BRIDGE );

    connection = context.get( ISkConnection.class );
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    try {
      // запуск локальных отправителей
      for( int i = 0; i < senders.size(); i++ ) {
        IEventSender sender = senders.get( i );
        sender.start( context );
      }
    }
    catch( TsItemNotFoundRtException e ) {
      logger.error( e, ERR_MSG_EVENT_MODULE_IS_NOT_STARTED_FORMAT, OPC_UA_BRIDGE );
      throw e;
    }
    logger.info( MSG_EVENTS_MODULE_IS_STARTED_FORMAT, OPC_UA_BRIDGE );

    // if( attrValueSources.size() > 0 ) {
    //
    // IServerMessageService messageService = connection.serverApi().serverMessageService();
    // // Слушаем сообщения об изменениях набора
    // messageService.addMessageListener( S5ServerMsgObjectOperation.class.getName(), this );
    // }
  }

  private boolean isConfigured() {
    // TODO Auto-generated method stub
    return false;
  }

  // @Override
  protected void doDoJob() {
    // if( attrValueSources.size() > 0 ) {
    // synchronized (attributesValue) {
    // if( attributesValueChanged ) {
    //
    // for( AttributeInputValueSource avs : attrValueSources ) {
    // avs.updateValue();
    // }
    // }
    //
    // attributesValueChanged = false;
    // }
    //
    // }

    long time = System.currentTimeMillis();
    // вызов каждого отправителя для выполнения работы - отправки сообщения - при переходе значения с 0 на 1 на
    // соответствующем пине
    for( int i = 0; i < senders.size(); i++ ) {
      IEventSender sender = senders.get( i );
      sender.sendEvent( time );
    }

  }

  /**
   * Создаёт объект-формирователь события по конфигурационной информации
   *
   * @param aConfig IAvTree - конфигурационная информация
   * @return IEventSender -
   */
  @SuppressWarnings( "unchecked" )
  private static IEventSender createSender( IAvTree aConfig ) {
    // тип передатчика - из конфигурации
    String eventSenderClassStr = aConfig.fields().getStr( EVENT_SENDER_JAVA_CLASS );

    try {
      Class<IEventSender> eventSenderClass = (Class<IEventSender>)Class.forName( eventSenderClassStr );

      IEventSender sender = eventSenderClass.getConstructor().newInstance();

      return sender;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_SENDER_FORMAT, aConfig.structId() );
    }

  }

}

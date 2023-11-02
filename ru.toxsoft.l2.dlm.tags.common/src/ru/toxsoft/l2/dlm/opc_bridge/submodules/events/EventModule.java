package ru.toxsoft.l2.dlm.opc_bridge.submodules.events;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;
import static ru.toxsoft.l2.dlm.opc_bridge.submodules.events.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;

/**
 * Класс работы с событиями системы диспетчеризации.
 *
 * @author max
 */
public class EventModule
    extends ConfigurableWorkerModuleBase {

  /**
   * Журнал работы
   */
  ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Контекст.
   */
  IDlmContext context;

  /**
   * Информация о модуле DLM
   */
  private IDlmInfo dlmInfo;

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
   * @param aContext {@link IDlmContext} - контекст подгружаемых модулей.
   * @param aDlmInfo IDlmInfo - информация о DLM
   */
  public EventModule( IDlmContext aContext, IDlmInfo aDlmInfo ) {
    context = aContext;
    dlmInfo = aDlmInfo;
  }

  @Override
  protected void doConfigYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {
    IAvTree eventDefs = aConfig.params().nodes().findByKey( EVENT_DEFS );

    senders = new ElemArrayList<>();

    if( eventDefs != null && eventDefs.isArray() ) {
      for( int i = 0; i < eventDefs.arrayLength(); i++ ) {
        IAvTree eventDef = eventDefs.arrayElement( i );

        try {
          IEventSender eventSender = createSender( eventDef );
          eventSender.config( eventDef );
          senders.add( eventSender );
        }
        catch( Exception e ) {
          logger.error( e, "Create or Config of event sender error" );
        }

      }
    }

  }

  @Override
  protected void doStartComponent() {
    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_EVENT_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    connection = context.network().getSkConnection();
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    try {
      // запуск локальных отправителей
      for( int i = 0; i < senders.size(); i++ ) {
        IEventSender sender = senders.get( i );
        try {
          sender.start( context );
        }
        catch( Exception e ) {
          logger.error( e, "Start of event sender error" );
        }
      }
    }
    catch( TsItemNotFoundRtException e ) {
      logger.error( e, ERR_MSG_EVENT_MODULE_IS_NOT_STARTED_FORMAT, dlmInfo.moduleId() );
      throw e;
    }
    logger.info( MSG_EVENTS_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );

    // if( attrValueSources.size() > 0 ) {
    //
    // IServerMessageService messageService = connection.serverApi().serverMessageService();
    // // Слушаем сообщения об изменениях набора
    // messageService.addMessageListener( S5ServerMsgObjectOperation.class.getName(), this );
    // }
  }

  @Override
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

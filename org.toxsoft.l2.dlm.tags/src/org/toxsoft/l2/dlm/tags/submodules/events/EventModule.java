package org.toxsoft.l2.dlm.tags.submodules.events;

import static org.toxsoft.l2.dlm.tags.IDlmsBaseConstants.*;
import static org.toxsoft.l2.dlm.tags.submodules.events.IL2Resources.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.dlms.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Класс работы с событиями системы диспетчеризации.
 *
 * @author max
 */
public class EventModule
    extends AbstractTsCoopCompMultiUse {

  private static final String OPC_MODULE_ID = "eventsModule"; //$NON-NLS-1$

  /**
   * Журнал работы
   */
  private ILogger logger;

  private IAvTree cfg;

  /**
   * Контекст.
   */
  private IL2SharedContext context;

  /**
   * Информация о модуле DLM
   */
  private DlmInfo dlmInfo;

  /**
   * Соединение с сервером.
   */
  ISkConnection connection;

  /**
   * Объекты локальные отправитель сообщений.
   */
  private IListEdit<AbstarctEventSender> senders;

  /**
   * Конструктор по контексту.
   *
   * @param aCfg IAvTree - конфигурация DLM
   * @param aDlmInfo IDlmInfo - информация о DLM
   * @param aInstanceId
   */
  public EventModule( IAvTree aCfg, DlmInfo aDlmInfo, String aInstanceId ) {
    cfg = aCfg;
    dlmInfo = aDlmInfo;

    logger = LoggerUtils.getLogger( this.getClass(), aDlmInfo.moduleId(), aInstanceId, OPC_MODULE_ID );
  }

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    context = aArgs.find( IL2SharedContext.class );
    IAvTree eventDefs = cfg.nodes().getByKey( EVENT_DEFS );

    senders = new ElemArrayList<>();

    if( eventDefs != null && eventDefs.isArray() ) {
      for( int i = 0; i < eventDefs.arrayLength(); i++ ) {
        IAvTree eventDef = eventDefs.arrayElement( i );

        try {
          AbstarctEventSender eventSender = createSender( eventDef );
          eventSender.config( eventDef );
          senders.add( eventSender );
        }
        catch( Exception e ) {
          logger.error( e, "Create or Config of event sender error" );
        }

      }
    }
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doStart() {
    boolean isConfigured = true;
    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured, ERR_MSG_EVENT_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    connection = context.net().getSkConnection();
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    try {
      // запуск локальных отправителей
      for( int i = 0; i < senders.size(); i++ ) {
        AbstarctEventSender sender = senders.get( i );
        try {
          Gwid eventGwid = sender.getEventGwid();
          String clasId = eventGwid.classId();
          String objId = eventGwid.strid();
          String evntId = eventGwid.propId();

          ISkClassInfo classInfo = connection.coreApi().sysdescr().findClassInfo( clasId );

          if( classInfo == null ) {
            logger.error( "Class '%s' not found during event '%s' sender registration", clasId, evntId );
            return;
          }

          if( !classInfo.events().list().hasKey( evntId ) ) {
            logger.error( "Event '%s' of class '%s' not found during event sender registration", evntId, clasId );
            return;
          }

          if( connection.coreApi().objService().find( new Skid( clasId, objId ) ) == null ) {
            logger.error( "Object '%s' of class '%s' not found during event '%s' sender registration", objId, clasId,
                evntId );
            return;
          }

          IStridablesList<IDataDef> paramDefs = classInfo.events().list().getByKey( evntId ).paramDefs();

          IGwidValueSetter[] dataSetters = new IGwidValueSetter[paramDefs.size() + 1];

          IOptionSetEdit options = new OptionSet();
          dataSetters[0] = new EventGwidValueSetter( connection.coreApi().eventService(), eventGwid, options );
          for( int j = 0; j < paramDefs.size(); j++ ) {
            dataSetters[j + 1] = new EventParamGwidValueSetter(
                Gwid.createEventParam( eventGwid.skid(), eventGwid.propId(), paramDefs.get( j ).id() ), options );
          }

          IList<String[]> tagIds = sender.getTagIds();

          IStridablesListEdit<IL2Tag> tags = new StridablesList<>();

          // найти все теги
          for( String[] id : tagIds ) {
            // TODO device fix
            IL2Tag tag = context.hal().tags().getByKey( id[1] );

            if( tag == null ) {
              logger.error( "Tag '%s' not found", id[0] + " | " + id[1] );
            }
            else {
              tags.add( tag );
            }
          }

          sender.start( dataSetters, new IGwidValueGetter[0], tags );
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
      IDataGwidTranslator sender = senders.get( i );
      sender.translate( time );
    }

  }

  /**
   * Создаёт объект-формирователь события по конфигурационной информации
   *
   * @param aConfig IAvTree - конфигурационная информация
   * @return IEventSender -
   */
  @SuppressWarnings( "unchecked" )
  private static AbstarctEventSender createSender( IAvTree aConfig ) {
    // тип передатчика - из конфигурации
    String eventSenderClassStr = aConfig.fields().getStr( EVENT_SENDER_JAVA_CLASS );

    try {
      Class<AbstarctEventSender> eventSenderClass = (Class<AbstarctEventSender>)Class.forName( eventSenderClassStr );

      AbstarctEventSender sender = eventSenderClass.getConstructor().newInstance();

      return sender;
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ERR_MSG_CANT_CREATE_INSTANCE_SENDER_FORMAT, aConfig.structId() );
    }

  }

  @Override
  protected boolean doQueryStop() {
    // TODO Auto-generated method stub
    return false;
  }

}

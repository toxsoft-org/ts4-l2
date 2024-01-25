package ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.connection.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.dlm.opc_bridge.*;

/**
 * Класс работы с сложными синтетическими тегами..
 *
 * @author max
 */
public class ComplexTagsModule
    extends ConfigurableWorkerModuleBase {

  private static final String СOMPLEX_TAGS_DEFS = "complexTagsDefs";

  private static final String ERR_MSG_COMPLEX_TAGS_MODULE_CANT_BE_STARTED_FORMAT =
      "Модуль сложных тегов в %s не может быть запущен, так как не сконфигурирован";

  private static final String MSG_EVENTS_MODULE_IS_STARTED_FORMAT = "Работа с событиями в модуле %s стартовала";

  private static final String ERR_MSG_EVENT_MODULE_IS_NOT_STARTED_FORMAT =
      "Подмодуль событий в %s не стартовал:\n Некорректно указаны пины";

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
   * СЛожные синтетические теги
   */
  private StringMap<ComplexTagImpl> complexTags = new StringMap<>();

  /**
   * Конструктор по контексту.
   *
   * @param aContext {@link IDlmContext} - контекст подгружаемых модулей.
   * @param aDlmInfo IDlmInfo - информация о DLM
   */
  public ComplexTagsModule( IDlmContext aContext, IDlmInfo aDlmInfo ) {
    context = aContext;
    dlmInfo = aDlmInfo;
  }

  @Override
  protected void doConfigYourself( IUnitConfig aConfig )
      throws TsIllegalArgumentRtException {
    IAvTree complexTagsDefs = aConfig.params().nodes().findByKey( СOMPLEX_TAGS_DEFS );

    if( complexTagsDefs != null && complexTagsDefs.isArray() ) {
      for( int i = 0; i < complexTagsDefs.arrayLength(); i++ ) {
        IAvTree tagDef = complexTagsDefs.arrayElement( i );

        try {
          ComplexTagImpl complexTag = createComplexTag( tagDef );
          complexTags.put( complexTag.id(), complexTag );
        }
        catch( Exception e ) {
          logger.error( e, "Create or Config of Complex Tag error" );
        }

      }
    }

  }

  @Override
  protected void doStartComponent() {
    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_COMPLEX_TAGS_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    connection = context.network().getSkConnection();
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    try {
      // запуск сложных тегов
      for( String tagId : complexTags.keys() ) {
        // TODO - старт каждого тега если потребуется
        // complexTags.getByKey( tagId ).doJob();
      }
    }
    catch( TsItemNotFoundRtException e ) {
      logger.error( e, ERR_MSG_EVENT_MODULE_IS_NOT_STARTED_FORMAT, dlmInfo.moduleId() );
      throw e;
    }
    logger.info( MSG_EVENTS_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );
  }

  @Override
  protected void doDoJob() {
    for( String tagId : complexTags.keys() ) {
      complexTags.getByKey( tagId ).doJob();
    }
  }

  /**
   * Создаёт сложный синтетический тег
   *
   * @param aConfig IAvTree - конфигурационная информация
   * @return IComplexTag - сложный синтетический тег
   */
  @SuppressWarnings( "unchecked" )
  private static ComplexTagImpl createComplexTag( IAvTree aConfig ) {
    // TODO
    ComplexTagImpl result = new ComplexTagImpl( "id.from.cfg", null, null );
    return result;
  }

  public IComplexTag complexTag( String aTagId ) {
    return complexTags.findByKey( aTagId );
  }

  public IStringMap<IComplexTag> complexTags() {
    IStringMapEdit<IComplexTag> result = new StringMap<>();
    for( String tagId : complexTags.keys() ) {
      result.put( tagId, complexTags.getByKey( tagId ) );
    }
    return result;
  }
}

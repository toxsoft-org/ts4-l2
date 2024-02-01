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
    extends ConfigurableWorkerModuleBase
    implements IComplexTagsContainer {

  private static final String СOMPLEX_TAGS_DEFS = "complexTagsDefs";

  public static final String СOMPLEX_TAG_ID   = "complex.tag.id";
  public static final String СOMPLEX_TAG_TYPE = "complex.tag.type";

  private static final String ERR_MSG_COMPLEX_TAGS_MODULE_CANT_BE_STARTED_FORMAT =
      "Подмодуль сложных тегов в %s не может быть запущен, так как не сконфигурирован";

  private static final String MSG_EVENTS_MODULE_IS_STARTED_FORMAT = "Работа с событиями в модуле %s стартовала";

  private static final String MSG_COMPLEX_TAGS_MODULE_IS_STARTED_FORMAT =
      "Работа подмодуля сложных тегов в модуле %s стартовала";

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
   * Configuration info
   */
  private IAvTree complexTagsDefs;

  /**
   * Соединение с сервером.
   */
  ISkConnection connection;

  /**
   * СЛожные синтетические теги
   */
  private StringMap<AbstractComplexTag> complexTags = new StringMap<>();

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
    if( !aConfig.params().nodes().hasKey( СOMPLEX_TAGS_DEFS ) ) {
      return;
    }
    complexTagsDefs = aConfig.params().nodes().findByKey( СOMPLEX_TAGS_DEFS );

  }

  @Override
  protected void doStartComponent() {
    // если модуль не сконфигурирован - выбросить исключение
    TsIllegalStateRtException.checkFalse( isConfigured(), ERR_MSG_COMPLEX_TAGS_MODULE_CANT_BE_STARTED_FORMAT,
        dlmInfo.moduleId() );

    if( complexTagsDefs == null || !complexTagsDefs.isArray() || complexTagsDefs.arrayLength() == 0 ) {
      logger.warning( "Complex tag of dlm %s not started - couldnt find cfg info", dlmInfo.moduleId() );
      return;
    }

    connection = context.network().getSkConnection();
    // TsIllegalStateRtException.checkFalse( connection.isConnected(), ERR_MSG_CONNECTION_TO_SERVER_IS_NOT_ESTABLISHED
    // );

    // создание и запуск сложных тегов

    for( int i = 0; i < complexTagsDefs.arrayLength(); i++ ) {
      IAvTree tagDef = complexTagsDefs.arrayElement( i );

      try {
        AbstractComplexTag complexTag = createComplexTag( tagDef );
        complexTags.put( complexTag.id(), complexTag );
        // конфигурируем
        complexTag.config( tagDef );
        complexTag.start( context );
      }
      catch( Exception e ) {
        logger.error( e, "Create or start of Complex Tag error" );
      }

    }

    logger.info( MSG_COMPLEX_TAGS_MODULE_IS_STARTED_FORMAT, dlmInfo.moduleId() );
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
  private static AbstractComplexTag createComplexTag( IAvTree aConfig ) {
    TsIllegalArgumentRtException.checkFalse( aConfig.fields().hasKey( СOMPLEX_TAG_TYPE ) );

    String tagType = aConfig.fields().getStr( СOMPLEX_TAG_TYPE );
    String tagId = aConfig.fields().getStr( СOMPLEX_TAG_ID );
    try {
      return EComplexTagType.createComplexTag( tagType, tagId, true );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex );
    }

  }

  @Override
  public IStringList complexTagIds() {
    return complexTags.keys();
  }

  @Override
  public IComplexTag getComplexTagById( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    return complexTags.getByKey( aId );
  }

  // "pin.tag.syntetic1.def",
  // {
  // complex.tag.id ="syntetic1",
  // complex.tag.type="node.with.address.params.feedback",
  // tag.dev.id="opc2s5.bridge.collection.id"
  // write.id.tag = "ns=32770;i=30" ,
  // write.int.param.tag = "ns=32770;i=31" ,
  // write.float.param.tag = "ns=32770;i=32" ,
  // write.str.param.tag = "ns=32770;i=33" ,
  // read.feedback.tag = "ns=32770;i=34" ,
  // }

}

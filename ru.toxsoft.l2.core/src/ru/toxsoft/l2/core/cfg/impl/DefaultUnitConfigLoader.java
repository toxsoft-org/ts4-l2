package ru.toxsoft.l2.core.cfg.impl;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.toxsoft.l2.core.cfg.*;

/**
 * Загрузчик конфигураций по умолчанию.<br>
 *
 * @author max
 */
public class DefaultUnitConfigLoader
    extends AbstractUnitConfigLoader<IUnitConfig> {

  /**
   * Назавание параметра описания.
   */
  private static final String PARAM_DESCRIPTION = "description"; //$NON-NLS-1$

  /**
   * Название параметра идентификатора.
   */
  private static final String PARAM_ID = "id"; //$NON-NLS-1$

  /**
   * Конструктор загрузчика
   *
   * @param aCfgFileExtension - расширение фалов конфигураций
   * @param aCfgKeyword - ключевое слово, с которого начинается конфигурация в файле.
   */
  public DefaultUnitConfigLoader( final String aCfgFileExtension, String aCfgKeyword ) {
    super( aCfgFileExtension, aCfgKeyword );

  }

  @Override
  protected DefaultUnitConfig load( IStrioReader aFile ) {

    TsNullArgumentRtException.checkNull( aFile );

    IAvTree params = AvTreeKeeper.KEEPER.read( aFile );

    IOptionSet oSet = params.fields();
    String id = oSet.getStr( PARAM_ID );
    String description = oSet.getStr( PARAM_DESCRIPTION );

    return new DefaultUnitConfig( id, description, params );
  }

}

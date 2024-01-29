package ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.dlm.*;

public abstract class AbstractComplexTag
    implements IComplexTag {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( AbstractComplexTag.class );

  /**
   * Идентификатор тега
   */
  private String id;

  protected AbstractComplexTag( String aId ) {
    super();
    id = aId;
  }

  @Override
  public String id() {
    return id;
  }

  protected abstract void doJob();

  protected abstract void config( IAvTree aCfg );

  protected abstract void start( IDlmContext aContext );

}

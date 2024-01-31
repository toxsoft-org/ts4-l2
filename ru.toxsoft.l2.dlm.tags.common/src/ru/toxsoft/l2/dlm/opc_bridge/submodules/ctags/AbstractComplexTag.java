package ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Абстрактная реализация комплексного тега
 *
 * @author max
 */
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

  @Override
  public String tagId() {
    return id;
  }

  @Override
  public String name() {
    return id;
  }

  @Override
  public EKind kind() {
    return EKind.RW;
  }

  @Override
  public EAtomicType valueType() {
    return EAtomicType.NONE;
  }

  @Override
  public IAtomicValue get() {
    return IAtomicValue.NULL;
  }

  @Override
  public void set( IAtomicValue aVal ) {
    // non
  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public String nmName() {
    return id;
  }

  @Override
  public String description() {
    return id;
  }

}

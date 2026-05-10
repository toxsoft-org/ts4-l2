package org.toxsoft.l2.lib.app.impl;

import static org.toxsoft.l2.lib.app.impl.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.hal.*;
import org.toxsoft.l2.lib.invokable.*;
import org.toxsoft.l2.lib.main.*;
import org.toxsoft.l2.lib.main.impl.*;

/**
 * Сделать интерфейс, в котором идентификатор и версия базовая реализация APP компоненты. Реализации для конкретных
 * проектов - наследники этого класса.
 *
 * @author max
 */
public class BaseAppImpl
    extends AbstractL2Component
    implements IAppComponent {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Конструктор по интерфейсу глобального контекста.
   *
   * @param aGlobalContext - глобальный контекст.
   */
  public BaseAppImpl( IGlobalContext aGlobalContext ) {
    super( aGlobalContext );

  }

  @Override
  protected void processStart() {
    // нет реализации
  }

  @Override
  protected void processRunStep() {
    // нет реализации
  }

  @Override
  protected boolean processStopQuery() {
    return true;
  }

  //
  // ----------------------------------------------------------------
  // методы интерфейса IInvokable

  @Override
  public IAtomicValue getVar( String aVarId ) {
    // нет реализации
    return null;
  }

  @Override
  public void setVar( String aVarId, IAtomicValue aValue ) {
    // нет реализации
  }

  @Override
  public ECallStatus call( String aMethodId, IOptionSet aArgsValues ) {
    // нет реализации
    return null;
  }

  //
  // ----------------------------------------------------------------
  // методы интерфейса IInvokableInfo

  @Override
  public IStridablesList<IVarInfo> listVarInfoes() {
    // нет реализации
    return null;
  }

  @Override
  public IStridablesList<IMethodInfo> listMethodInfoes() {
    // нет реализации
    return null;
  }

  @Override
  public IOptionSet getStructVar( String aStructId )
      throws TsIllegalArgumentRtException {
    // нет реализации
    return null;
  }

  @Override
  public IStridablesList<IStructInfo> listStructInfoes() {
    // нет реализации
    return null;
  }

  //
  // ----------------------------------------------------------------
  // методы интерфейса IHalErrorProcessor

  @Override
  public void onApparatError( IList<ApparatError> aErrors ) {
    // if(true){
    // return; //TODO отключено для отладки резервирования
    // }
    for( ApparatError ae : aErrors ) {
      StringBuilder extraInfo = new StringBuilder();
      if( ae.getParams() != null ) {
        for( String name : ae.getParams().keys() ) {
          String val = ae.getParams().getValue( name ).asString();
          extraInfo.append( name ).append( EQUALS_STR ).append( val ).append( COMMA_STR );
        }
      }
      if( ae.getException() == null ) {
        logger.error( ERR_L2_ERROR_IN_APPARATUS_ADD_INFO, ae.getErrorId(), ae.getApparatId(), extraInfo.toString() );
      }
      else {
        logger.error( ae.getException(), ERR_L2_ERROR_IN_APPARATUS, ae.getErrorId(), ae.getApparatId() );
      }
    }
  }

}

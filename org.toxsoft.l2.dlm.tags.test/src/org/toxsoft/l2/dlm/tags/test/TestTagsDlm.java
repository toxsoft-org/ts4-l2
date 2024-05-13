package org.toxsoft.l2.dlm.tags.test;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.dlm.impl.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Модуль opc-моста.
 *
 * @author max
 */
public class TestTagsDlm
    extends AbstractDlm {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  private ITag tag;

  private ITag outTag;

  /**
   * Конструктор.
   *
   * @param aInfo {@link IDlmInfo} - информация о модуле.
   * @param aContext {@link IDlmContext} - контекст нижнего уровня, в котором загружается модуль.
   */
  protected TestTagsDlm( IDlmInfo aInfo, IDlmContext aContext ) {
    super( aInfo, aContext );

  }

  @Override
  public void configYourself( IUnitConfig aConfig ) {
    logger.info( "Test Opc Configed: %s", aConfig.params().fields().getStr( "testTag" ) );

  }

  @Override
  protected void doStartComponent() {

    ITsOpc tagsDevice = (ITsOpc)context.hal().listSpecificDevices().getByKey( "connection1.def" );
    tag = tagsDevice.tag( "AI_2_1" );
    outTag = tagsDevice.tag( "AO_2_1" );
    logger.info( "Test Opc Started" );

  }

  @Override
  public void doJob() {
    IAtomicValue atomicVal = tag.get();
    if( !atomicVal.isAssigned() ) {
      return;
    }
    int val = atomicVal.asInt();
    int valOut = val * 5;
    outTag.set( AvUtils.avInt( valOut ) );
    logger.info( "Test Opc Job, val = %s", String.valueOf( valOut ) );

  }

  @Override
  protected boolean doQueryStop() {

    return true;
  }

  @Override
  protected boolean doStopStep() {
    return true;
  }

}

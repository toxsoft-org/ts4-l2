package org.toxsoft.l2.dlm.tags;

import static org.toxsoft.l2.dlm.tags.IL2Resources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.coopcomp.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.dlm.tags.submodules.commands.*;
import org.toxsoft.l2.dlm.tags.submodules.data.*;
import org.toxsoft.l2.dlm.tags.submodules.events.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.dlms.*;

/**
 * Модуль opc-моста.
 *
 * @author max
 */
public class OpcBridgeDlm
    extends L2AbstractDlm {

  /**
   * Журнал работы
   */
  private ILogger logger;

  /**
   * Модули
   */
  private IListEdit<AbstractTsCoopCompMultiUse> modules;

  /**
   * Конструктор.
   *
   * @param aInfo {@link IDlmInfo} - информация о модуле.
   * @param aContext {@link IDlmContext} - контекст нижнего уровня, в котором загружается модуль.
   */
  protected OpcBridgeDlm( String aInstanceId, DlmInfo aDlmInfo, IOptionSet aParams ) {
    super( aInstanceId, aDlmInfo, aParams );

    logger = LoggerUtils.getLogger( getClass(), aInstanceId );

    modules = new ElemArrayList<>();

    IL2SharedContext context = aParams.getValobj( "l2.context" ); // TODO needs context

    // подмодуль комплексных тегов
    // ComplexTagsModule complexTagsModule = new ComplexTagsModule( aContext, info() );
    // modules.add( complexTagsModule );

    // подмодуль текущих данных
    AbstractTsCoopCompMultiUse currDataModule = new OpcCurrDataModule( context, info() );
    modules.add( currDataModule );

    AbstractTsCoopCompMultiUse eventModule = new EventModule( context, info() );
    modules.add( eventModule );

    // создание модуля команд и установка в него модуля комплексных тегов
    AbstractTsCoopCompMultiUse commandsModule = new CommandsModule( context, info() );
    modules.add( commandsModule );

    // dima 25.12.23 add rriModule
    // IConfigurableWorkerModule rriModule =
    // new OpcRriDataModule( aContext, info(), new RriDataTransmittersInitializer(), complexTagsModule );
    // modules.add( rriModule );

    // IConfigurableWorkerModule fcModule = new FreqCounterModule( aContext );
    //
    // modules.add( currDataModule );
    // modules.add( histModule );

    //
    // // генератор меандра - последним
    // modules.add( meandrGenerator );
    //
    // // счётчик
    // modules.add( fcModule );
  }

  @Override
  protected ValidationResult doInit( ITsContextRo aArgs ) {
    for( ITsCooperativeComponent module : modules ) {
      try {
        module.init( aArgs );
      }
      catch( Exception e ) {
        logger.error( e, ERR_MSG_OPC_BRIDGE_SUBMODULE_CONFIG_ERROR );
      }
    }
    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doStart() {

    for( ITsCooperativeComponent module : modules ) {
      try {
        module.start();
      }
      catch( Exception e ) {
        logger.error( e, ERR_MSG_OPC_BRIDGE_SUBMODULE_START_ERROR );
      }
    }

  }

  @Override
  public void doDoJob() {

    for( ITsCooperativeComponent module : modules ) {
      module.doJob();
    }

  }

  @Override
  protected boolean doQueryStop() {

    for( ITsCooperativeComponent module : modules ) {
      module.queryStop();
    }

    return true;
  }

  @Override
  protected boolean doStopping() {
    return true;
  }

}

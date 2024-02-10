package ru.toxsoft.l2.dlm.opc_bridge;

import static ru.toxsoft.l2.dlm.opc_bridge.IL2Resources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.cfg.*;
import ru.toxsoft.l2.core.dlm.*;
import ru.toxsoft.l2.core.dlm.impl.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.commands.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.ctags.*;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.data.CurrDataTransmittersInitializer;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.data.HistDataTransmittersInitializer;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.data.OpcCurrDataModule;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.data.OpcHistDataModule;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.events.EventModule;
import ru.toxsoft.l2.dlm.opc_bridge.submodules.rri.*;

/**
 * Модуль opc-моста.
 *
 * @author max
 */
public class OpcBridgeDlm
    extends AbstractDlm {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Модули
   */
  private IListEdit<IConfigurableWorkerModule> modules;

  /**
   * Конструктор.
   *
   * @param aInfo {@link IDlmInfo} - информация о модуле.
   * @param aContext {@link IDlmContext} - контекст нижнего уровня, в котором загружается модуль.
   */
  protected OpcBridgeDlm( IDlmInfo aInfo, IDlmContext aContext ) {
    super( aInfo, aContext );

    modules = new ElemArrayList<>();

    // подмодуль комплексных тегов
    ComplexTagsModule complexTagsModule = new ComplexTagsModule( aContext, info() );
    modules.add( complexTagsModule );

    // for debug
    // подмодуль текущих данных
     IConfigurableWorkerModule currDataModule =
     new OpcCurrDataModule( aContext, info(), new CurrDataTransmittersInitializer() );
     modules.add( currDataModule );
    
     IConfigurableWorkerModule histModule =
     new OpcHistDataModule( aContext, info(), new HistDataTransmittersInitializer() );
     modules.add( histModule );
    
     IConfigurableWorkerModule eventModule = new EventModule( aContext, info() );
     modules.add( eventModule );

    // создание модуля команд и установка в него модуля комплексных тегов
    IConfigurableWorkerModule commandsModule = new CommandsModule( aContext, info(), complexTagsModule );
    modules.add( commandsModule );

    // dima 25.12.23 add rriModule
//    IConfigurableWorkerModule rriModule =
//        new OpcRriDataModule( aContext, info(), new RriDataTransmittersInitializer(), complexTagsModule );
//    modules.add( rriModule );

    //
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
  public void configYourself( IUnitConfig aConfig ) {
    for( IConfigurableWorkerModule module : modules ) {
      try {
        module.configYourself( aConfig );
      }
      catch( Exception e ) {
        logger.error( e, ERR_MSG_OPC_BRIDGE_SUBMODULE_CONFIG_ERROR );
      }
    }

  }

  @Override
  protected void doStartComponent() {

    for( IConfigurableWorkerModule module : modules ) {
      try {
        module.start();
      }
      catch( Exception e ) {
        logger.error( e, ERR_MSG_OPC_BRIDGE_SUBMODULE_START_ERROR );
      }
    }

  }

  @Override
  public void doJob() {

    for( IConfigurableWorkerModule module : modules ) {
      module.doJob();
    }

  }

  @Override
  protected boolean doQueryStop() {

    for( IConfigurableWorkerModule module : modules ) {
      module.queryStop();
    }

    return true;
  }

  @Override
  protected boolean doStopStep() {
    return true;
  }

}

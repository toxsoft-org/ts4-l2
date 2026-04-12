package org.toxsoft.l2.lib.impl;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.l2.lib.IL2GlobalOptions.*;
import static org.toxsoft.l2.lib.IL2HardConstants.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import java.io.*;
import java.lang.reflect.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.common.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * {@link IL2Hal} implementation.
 *
 * @author hazard157
 */
class L2Hal
    extends L2AbstractComponent
    implements IL2Hal, ICooperativeWorkerComponent {

  private final IStridablesListEdit<L2AbstractTag>       tagsList   = new StridablesList<>();
  private final IStridablesListEdit<L2AbstractHalDevice> deviceList = new StridablesList<>();

  /**
   * Constructor.
   *
   * @param aL2Context {@link IL2SharedContext} - the L2 context
   */
  public L2Hal( IL2SharedContext aL2Context ) {
    super( aL2Context, ALL_HAL_COMP_OPDEFS );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void cleanup() {
    tagsList.clear();
    deviceList.clear();
  }

  private static IL2HalDeviceFactory getFactory( L2ModuleConfigFile aDeviceCfg )
      throws Exception {
    String factoryClassName = aDeviceCfg.cfg().fields().getStr( HAL_DEVICE_PARAM_PRODUCER_CLASS, EMPTY_STRING );
    Class<?> rawClass = Class.forName( factoryClassName );
    Constructor<?> constructor = rawClass.getDeclaredConstructor();
    Object rawFactory = constructor.newInstance();
    return IL2HalDeviceFactory.class.cast( rawFactory );
  }

  private IList<IL2HalDeviceFactory> loadFactories() {
    // read configurations of of the HAL I/O device drivers
    IMap<File, L2ModuleConfigFile> mmDevCfgs = cfgDir().readConfigs();
    // create factories of device drivers
    IListEdit<IL2HalDeviceFactory> llFactories = new ElemArrayList<>();
    for( File f : mmDevCfgs.keys() ) {
      L2ModuleConfigFile cfg = mmDevCfgs.getByKey( f );
      IL2HalDeviceFactory factory;
      try {
        factory = getFactory( cfg );
        llFactories.add( factory );
      }
      catch( Exception ex ) {
        logger().error( FMT_ERR_NO_HAL_DEVICE_FACTORY, f.getName(), ex.getMessage() );
        continue;
      }
    }
    return llFactories;
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doDoInit( ITsContextRo aArgs ) {
    cleanup();
    IList<IL2HalDeviceFactory> llFactories = loadFactories();
    // TODO create devices from loaded factories
    for( IL2HalDeviceFactory factory : llFactories ) {

      // factory.createDevice( cfg );

    }

    // TODO Auto-generated method stub

    return ValidationResult.SUCCESS;
  }

  @Override
  protected void doDoJob() {

    // TODO L2Hal.doDoJob()

  }

  @Override
  protected boolean doQueryStop() {

    // TODO L2Hal.doQueryStop()

    return true;
  }

  // ------------------------------------------------------------------------------------
  // L2AbstractComponent
  //

  // ------------------------------------------------------------------------------------
  // IL2Component
  //

  @Override
  final public EL2ComponentKind kind() {
    return EL2ComponentKind.HAL;
  }

  // ------------------------------------------------------------------------------------
  // IL2Hal
  //

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IStridablesList<IL2Tag> tags() {
    return (IStridablesList)tagsList;
  }

  @Override
  public IStridablesList<IL2HalDevice> deviceList() {
    // TODO Auto-generated method stub
    // TODO реализовать L2Hal.deviceList()
    throw new TsUnderDevelopmentRtException( "L2Hal.deviceList()" );
  }

}

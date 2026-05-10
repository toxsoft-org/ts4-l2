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

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  private final IStridablesList<IL2Tag>       apiTagsList = (IStridablesList)tagsList;
  @SuppressWarnings( { "rawtypes", "unchecked" } )
  private final IStridablesList<IL2HalDevice> apiDevsList = (IStridablesList)deviceList;

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

  private IMap<L2ModuleConfigFile, IL2HalDeviceFactory> loadFactories() {
    // read configurations of of the HAL I/O device drivers
    IMap<File, L2ModuleConfigFile> mmDevCfgs = cfgDir().readConfigs();
    // create factories of device drivers
    IMapEdit<L2ModuleConfigFile, IL2HalDeviceFactory> mmFactories = new ElemMap<>();
    for( File f : mmDevCfgs.keys() ) {
      L2ModuleConfigFile cfg = mmDevCfgs.getByKey( f );
      IL2HalDeviceFactory factory;
      try {
        factory = getFactory( cfg );
        mmFactories.put( cfg, factory );
      }
      catch( Exception ex ) {
        logger().error( FMT_ERR_NO_HAL_DEVICE_FACTORY, f.getName(), ex.getMessage() );
        continue;
      }
    }
    return mmFactories;
  }

  // ------------------------------------------------------------------------------------
  // AbstractTsCoopCompMultiUse
  //

  @Override
  protected ValidationResult doDoInit( ITsContextRo aArgs ) {
    cleanup();
    ValidationResult vr = ValidationResult.SUCCESS;
    // create devices from loaded factories
    IMap<L2ModuleConfigFile, IL2HalDeviceFactory> mmFactories = loadFactories();
    IStridablesListEdit<L2AbstractHalDevice> tmpDeviceList = new StridablesList<>();
    for( L2ModuleConfigFile cfg : mmFactories.keys() ) {
      IL2HalDeviceFactory factory = mmFactories.getByKey( cfg );
      L2AbstractHalDevice device;
      try {
        device = factory.createDevice( cfg );
        TsInternalErrorRtException.checkNull( device );
        TsInternalErrorRtException.checkFalse( device.id().equals( cfg.id() ) );
        tmpDeviceList.add( device );
      }
      catch( Exception ex ) {
        logger().error( ex );
        vr = ValidationResult.firstNonOk( vr, ValidationResult.warn(
            "Device configuration '%s' ignored, factory can not create valid device: %s", cfg.id(), ex.getMessage() ) );
      }
    }
    // TODO initialize devices from list tmpDeviceList, initialized devices will be added to #deviceList
    for( L2AbstractHalDevice device : tmpDeviceList ) {
      try {
        device.start();
        deviceList.add( device );
      }
      catch( Exception ex ) {
        logger().error( ex );
        vr = ValidationResult.firstNonOk( vr,
            ValidationResult.warn( "Device '%s' ignored, error while starting: %s", device.id(), ex.getMessage() ) );
        device.destroy();
      }
    }
    return vr;
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

  @Override
  protected boolean doStopping() {
    // TODO Auto-generated method stub
    return super.doStopping();
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

  @Override
  public IStridablesList<IL2Tag> tags() {
    return apiTagsList;
  }

  @Override
  public IStridablesList<IL2HalDevice> deviceList() {
    return apiDevsList;
  }

}

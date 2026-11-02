package org.toxsoft.l2.lib.api.module.hal;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.api.module.*;

/**
 * HDLH (HAL Driver Lower Half) implementation base.
 * <p>
 * HDLH is the part working in the separate hardware I/O thread (one per HAL driver). Each HAL driver creates one and
 * only instance of the HDLH.
 * <p>
 * TODO describe working with hardware, buffer, {@link #getErrorAndAnyOtherInfoAndMaybeQuitCommand()}
 *
 * @author hazard157
 */
public abstract class L2AbstractHDLH
    implements IParameterized {

  private final L2AbstractHalDriver owner;

  protected L2AbstractHDLH( L2AbstractHalDriver aOwner ) {
    TsNullArgumentRtException.checkNull( aOwner );
    owner = aOwner;
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  final public IOptionSet params() {
    return owner.params();
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  final L2AbstractHalDriver owner() {
    return owner;
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  /**
   * Returns configuration of the module.
   * <p>
   * Returns the same
   *
   * @return {@link L2ModuleConfig} - the module configuration
   */
  final public L2ModuleConfig moduleConfig() {
    return owner.moduleConfig();
  }

  // ------------------------------------------------------------------------------------
  // To implement/override
  //

  /**
   * Implementation must initialize hardware only in this method.
   * <p>
   * This method is called from hardware thread.
   *
   * @return {@link ValidationResult} - the initialization result
   */
  protected abstract ValidationResult initHardware();

  /**
   * Implementation must reset (de-initialize) hardware only in this method.
   * <p>
   * This method is called from separate thread and is not involved in the cooperative multi-taskable loop.
   */
  protected abstract void closeHardware();

  protected abstract Object getErrorAndAnyOtherInfoAndMaybeQuitCommand(); // TODO ???

  protected abstract void sendBufferValuesToHardware();

  protected abstract void receiveBufferValuesFromHardware();

  protected abstract void getFromTagsToInternalBuffer( IStridablesList<IL2TagForHDLH> aTags );

  protected abstract void setToTagsFromInternalBuffer( IStridablesList<IL2TagForHDLH> aTags );

}

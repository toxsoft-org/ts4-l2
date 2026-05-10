package org.toxsoft.l2.lib;

import org.toxsoft.core.tslib.utils.valobj.*;

/**
 * Library helper methods.
 *
 * @author hazard157
 */
public class L2LibUtils {

  /**
   * The library initialization must be called before any action to access classes in this plugin.
   */
  public static void initialize() {
    TsValobjUtils.registerKeeper( EL2ComponentKind.KEEPER_ID, EL2ComponentKind.KEEPER );
  }

  /**
   * No subclasses.
   */
  private L2LibUtils() {
    // nop
  }

}

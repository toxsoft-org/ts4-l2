package org.toxsoft.l2.lib;

import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.l2.lib.devel.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * Initialization and utility methods.
 *
 * @author hazard157
 */
public class L2LibUtils {

  /**
   * The plugin initialization must be called before any action to access classes in this plugin.
   */
  public static void initialize() {
    TsValobjUtils.registerKeeper( EL2SweepStep.KEEPER_ID, EL2SweepStep.KEEPER );
    TsValobjUtils.registerKeeper( L2TagInfo.KEEPER_ID, L2TagInfo.KEEPER );
  }

  /**
   * No subclasses.
   */
  private L2LibUtils() {
    // nop
  }

}

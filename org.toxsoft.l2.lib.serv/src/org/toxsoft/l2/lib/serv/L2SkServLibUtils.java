package org.toxsoft.l2.lib.serv;

import org.toxsoft.core.tslib.utils.valobj.*;

/**
 * The library helper and initialization methods.
 *
 * @author hazard157
 */
public class L2SkServLibUtils {

  /**
   * The library initialization must be called before any action to access classes in this plugin.
   */
  public static void initialize() {
    // nop
  }

  /**
   * No subclasses.
   */
  private L2SkServLibUtils() {
    TsValobjUtils.registerKeeperIfNone( EL2AppRunState.KEEPER_ID, EL2AppRunState.KEEPER );
  }

}

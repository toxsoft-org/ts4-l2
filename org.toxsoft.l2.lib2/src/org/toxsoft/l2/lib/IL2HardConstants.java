package org.toxsoft.l2.lib;

import static org.toxsoft.core.tslib.ITsHardConstants.*;

import org.toxsoft.core.tslib.math.*;

/**
 * L2 subsystem hard-coded constants.
 *
 * @author hazard157
 */
public interface IL2HardConstants {

  /**
   * L2 entities short ID prefix.
   */
  String L2_ID = "l2"; //$NON-NLS-1$ general short ID prefix (IDname)

  /**
   * L2 entities full ID prefix.
   */
  String L2_FULL_ID = TS_FULL_ID + "." + L2_ID; //$NON-NLS-1$ general full ID prefix (IDpath)

  /**
   * Allowed range of the L2 entities health value.
   * <p>
   * The {@link IntRange#maxValue()} means absolutely healthy entity, {@link IntRange#minValue()} is a dead one.
   */
  IntRange L2_HEALTH_RANGE = new IntRange( 0, 100 );

  /**
   * Allowed range of the L2 entities health contribution measure to the system overall health.
   * <p>
   * The {@link IntRange#maxValue()} means most influent entity, {@link IntRange#minValue()} is a least important one.
   */
  IntRange L2_HEALTH_CONTRIBUTION_RANGE = new IntRange( 1, 10 );

  /**
   * Default value for health contribution if not specified.
   */
  int DEFAULT_HEALTH_CONTRIBUTION = 5;

}

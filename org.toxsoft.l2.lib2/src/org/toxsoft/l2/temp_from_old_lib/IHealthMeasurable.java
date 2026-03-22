package org.toxsoft.l2.temp_from_old_lib;

import static org.toxsoft.l2.lib2.IL2HardConstants.*;

import org.toxsoft.l2.lib2.*;

/**
 * Mix-in interface of the entities with health <code>int</code> property in range
 * {@link IL2HardConstants#L2_HEALTH_RANGE}.
 *
 * @author max
 */
public interface IHealthMeasurable {

  /**
   * Returns the health level as a value in range {@link IL2HardConstants#L2_HEALTH_RANGE}.
   *
   * @return int - the health value
   */
  default int getHealth() {
    return L2_HEALTH_RANGE.maxValue();
  }

  /**
   * Returns relative contribution to system health in range {@link IL2HardConstants#L2_HEALTH_CONTRIBUTION_RANGE}.
   *
   * @return int - relative contribution to the system health
   */
  default int getWeight() {
    return DEFAULT_HEALTH_CONTRIBUTION;
  }

}

package org.toxsoft.l2.main.to_remove;

import static org.toxsoft.l2.lib.to_remove.IL2HardConstants.*;

import org.toxsoft.l2.lib.to_remove.app.*;
import org.toxsoft.l2.lib.to_remove.impl.*;

/**
 * {@link IL2Application} implementation used by {@link L2Main}.
 *
 * @author hazard157
 */
public class L2AppImpl
    extends L2Application {

  /**
   * The application ID.
   */
  public static final String APP_ID = L2_FULL_ID + ".MainApplication"; //$NON-NLS-1$

  /**
   * Constructor.
   */
  public L2AppImpl() {
    super( APP_ID );
  }

}

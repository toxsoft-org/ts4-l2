package org.toxsoft.l2.main;

import static org.toxsoft.l2.lib.IL2HardConstants.*;

import org.toxsoft.l2.lib.app.*;
import org.toxsoft.l2.lib.impl.*;

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

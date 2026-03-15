package org.toxsoft.l2.main.new_l2app.main;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.l2.lib.IL2LibConstants.*;
import static org.toxsoft.l2.main.new_l2app.l10n.IL2MainSharedResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.l2.main.new_l2app.app.*;

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

  private static final IOptionSet APP_PARAMS = OptionSetUtils.createOpSet( //
      TSID_NAME, STR_L2APP_IMPL, //
      TSID_DESCRIPTION, STR_L2APP_IMPL_D //
  );

  /**
   * Constructor.
   */
  public L2AppImpl() {
    super( APP_ID, APP_PARAMS );
  }

}

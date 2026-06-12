package org.toxsoft.l2.lib.serv;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.l2.lib.app.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Service manager {@link IL2Application} instances and configurations,
 * <p>
 * TODO tell about peer objects???
 *
 * @author hazard157
 */
public interface ISkL2ApplicationService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_SYSEXT_SERVICE_ID_PREFIX + ".L2Applications"; //$NON-NLS-1$

  /**
   * Lists all L2 application peer objects
   *
   * @return {@link IList}&lt;{@link IL2Application}&gt; - peer objects list
   */
  IList<ISkL2AppPeer> listL2AppPeers();

  /**
   * Creates new or edits existing L2 application peer object.
   *
   * @param aDto {@link DtoFullObject} - data to create peer object
   * @return {@link ISkL2AppPeer} - created/edited peer object
   */
  ISkL2AppPeer defineL2AppPer( DtoFullObject aDto );

  /**
   * Removes L2 application peer object.
   *
   * @param aSkid {@link Skid} - the SKID of peer to remove
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation failed
   */
  void removeL2AppPeer( Skid aSkid );

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkL2ApplicationServiceValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkL2ApplicationServiceValidator> svs();

  /**
   * Returns the eventer of the L2 application lifetime events.
   *
   * @return {@link ITsEventer}&lt;{@link ISkL2ApplicationServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkL2ApplicationServiceListener> eventer();

}

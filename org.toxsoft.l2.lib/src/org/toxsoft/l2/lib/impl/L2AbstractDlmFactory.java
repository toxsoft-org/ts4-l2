package org.toxsoft.l2.lib.impl;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.l2.lib.IL2HardConstants.*;
import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.l2.lib.dlms.*;

/**
 * {@link IDlmFactory} base implementation.
 * <p>
 * The constructor implementation in the inheritor must be <b>no arguments</b> so that the plugin manager can
 * instantiate it using <code>class.getDeclaredConstructor().newInstance()</code>.
 *
 * @author hazard157
 */
public abstract class L2AbstractDlmFactory
    implements IDlmFactory {

  private final DlmInfo info;

  protected L2AbstractDlmFactory( IPluginInfo aPluginInfo ) {
    TsNullArgumentRtException.checkNull( aPluginInfo );
    String moduleName = aPluginInfo.userProperties().getByKey( MF_ATTR_DLM_NAME );
    if( moduleName == null ) {
      moduleName = EMPTY_STRING;
    }
    info = new DlmInfo( aPluginInfo.pluginId(), aPluginInfo.pluginVersion(), moduleName );
  }

  // ------------------------------------------------------------------------------------
  // IDlmFactory
  //

  @Override
  final public DlmInfo info() {
    return info;
  }

  @Override
  final public L2AbstractDlm createDlm( String aInstanceId, IOptionSet aParams ) {
    StridUtils.checkValidIdPath( aInstanceId );
    TsNullArgumentRtException.checkNull( aParams );
    L2AbstractDlm dlm;
    try {
      dlm = doCreateDlm( aInstanceId, aParams );
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, MSG_ERR_DLM_CREATION_EXCEPTION, info.moduleId(), aInstanceId );
    }
    if( dlm == null ) {
      throw new TsInternalErrorRtException( MSG_ERR_NULL_DLM_CREATED, info.moduleId(), aInstanceId );
    }
    if( !info.equals( dlm.info() ) ) {
      throw new TsInternalErrorRtException( MSG_ERR_DLM_CREATED_WITH_BAD_INFO, info.moduleId(), aInstanceId );
    }
    return dlm;
  }

  // ------------------------------------------------------------------------------------
  // To override/implement
  //

  protected abstract L2AbstractDlm doCreateDlm( String aInstanceId, IOptionSet aParams );

}

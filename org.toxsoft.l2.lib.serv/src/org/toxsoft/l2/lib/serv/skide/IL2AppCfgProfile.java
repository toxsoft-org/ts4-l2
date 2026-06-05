package org.toxsoft.l2.lib.serv.skide;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.l2.lib.impl.*;

/**
 * The configuration profile stores content of the L2 application configuration directory.
 * <p>
 * L2 application configuration, as determined by <code>org.toxsoft.l2.lib</code> consists of:
 * <ul>
 * <li><i>global options</i> - are the {@link IOptionSet} passed to the {@link L2Application#init(ITsContextRo)} as an
 * argument context parameters;</li>
 * <li><i>MCF (Module Configuratio File)</i> - textual file containing L2 component modules configuration. MCF are read
 * from files during the L2Application initialization process.</li>
 * </ul>
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IL2AppCfgProfile
    extends IStridableParameterized, ITsClearable {

  // ------------------------------------------------------------------------------------
  // Global options

  IOptionSet getGlobalOptions();

  void setGlobalOptions( IOptionSet aGlobalOptions );

  // ------------------------------------------------------------------------------------
  // MCF managment
  //

  IStringList listMcfNames( EL2ComponentKind aKind );

  String getMcfContent( EL2ComponentKind aKind, String aCfgName );

  void setMcfContent( EL2ComponentKind aKind, String aCfgName );

  void addMcf( EL2ComponentKind aKind, String aCfgName );

  void removeMcf( EL2ComponentKind aKind, String aCfgName );

  // ------------------------------------------------------------------------------------
  // import/export data

  void exportToDirectory( File aLocalDir );

  void importFromDirectory( File aLocalDir );

}

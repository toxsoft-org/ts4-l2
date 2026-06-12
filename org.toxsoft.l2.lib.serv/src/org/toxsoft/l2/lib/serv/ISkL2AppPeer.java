package org.toxsoft.l2.lib.serv;

import java.util.concurrent.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.l2.lib.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * The peer Sk-object holds meta information about L2 application instance.
 *
 * @author hazard157
 */
public interface ISkL2AppPeer
    extends ISkObject {

  boolean isRunning();

  // FIXME MVK about Future<> ?

  // FIXME MVK login/password/role of the L2App ?

  // FIXME MVK ISkidList listConnectedSkids() ?

  Future<EL2AppRunState> getState( Skid aL2AppSkid );

  // TODO manage L2 application running instances (statistics, rebeet/top, etc.)

  // ------------------------------------------------------------------------------------
  // manage configuration

  Future<IOptionSet> readCfgGlobalOps();

  Future<ValidationResult> writeCfgGobalOps( IOptionSet aOps );

  IStringList listMcfFiles( EL2ComponentKind aComponentKind );

  Future<String> readMcfFile( EL2ComponentKind aComponentKind, String aBareName );

  Future<ValidationResult> writeMcfFile( EL2ComponentKind aComponentKind, String aBareName );

  // ------------------------------------------------------------------------------------
  // Control commands

  Future<ValidationResult> quitApp();

  Future<ValidationResult> restartApp();

  Future<ValidationResult> sendMessage( String aMessage );

}

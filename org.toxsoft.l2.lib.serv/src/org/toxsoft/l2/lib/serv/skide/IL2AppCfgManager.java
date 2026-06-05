package org.toxsoft.l2.lib.serv.skide;

/**
 * Manages configurations of the L2 applications.
 *
 * @author hazard157
 */
public interface IL2AppCfgManager {

  // FIXME move profiles of all apps here? or store per application?

  IL2AppCfgProfile copy( String aProfileId );

}

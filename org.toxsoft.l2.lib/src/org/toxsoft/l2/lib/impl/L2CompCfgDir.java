package org.toxsoft.l2.lib.impl;

import java.io.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.files.*;

/**
 * Represents L2 application component configuration directory.
 * <p>
 * Features:
 * <ul>
 * <li>lists content of directory in various ways;</li>
 * <li>monitors and informs about changes in the directory content.</li>
 * </ul>
 *
 * @author hazard157
 */
class L2CompCfgDir
    implements ICooperativeMultiTaskable {

  // FIXME watch directory changes via watcher!

  private final File dir;

  public L2CompCfgDir( File aDir ) {
    TsFileUtils.checkDirReadable( aDir );
    dir = aDir;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns child files (not directories) in the L2 component configuration file directory.
   *
   * @return {@link IList}&lt;{@link File}&gt; - sorted list of the child file objects
   */
  public IList<File> listAll() {
    return TsFileUtils.listChildsSorted( dir, TsFileFilter.FF_FILES );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //

  @Override
  public void doJob() {
    // TODO while not using watcher, check directory changes manually
  }

}

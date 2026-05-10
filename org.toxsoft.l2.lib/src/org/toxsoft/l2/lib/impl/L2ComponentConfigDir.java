package org.toxsoft.l2.lib.impl;

import static org.toxsoft.l2.lib.l10n.IL2LibSharedResources.*;

import java.io.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.files.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.l2.lib.common.*;

/**
 * Represents L2 application component configuration directory.
 * <p>
 * Manages MCFs {@link L2ModuleConfigFile} content of the directory. Class looks only for the files of extension
 * specified in the constructor.
 *
 * @author hazard157
 */
class L2ComponentConfigDir
    implements ICooperativeMultiTaskable {

  // FIXME watch directory changes via watcher!

  private final ILogger      logger = LoggerUtils.getLogger( L2ComponentConfigDir.class );
  private final File         dir;
  private final String       fileExt;
  private final TsFileFilter cfgFileFilter;

  /**
   * Constructor.
   *
   * @param aDir {@link File} - MCcomponent MCFs directory
   * @param aCfgFileExt String - configuration files extension (without dot)
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException extension is a blank string
   */
  public L2ComponentConfigDir( File aDir, String aCfgFileExt ) {
    dir = TsFileUtils.checkDirReadable( aDir );
    fileExt = TsErrorUtils.checkNonBlank( aCfgFileExt );
    cfgFileFilter = TsFileFilter.ofFileExt( fileExt );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns configuration files (not directories) in the L2 component configuration file directory.
   *
   * @return {@link IList}&lt;{@link File}&gt; - sorted list of the child files
   */
  public IList<File> listFiles() {
    return TsFileUtils.listChildsSorted( dir, cfgFileFilter );
  }

  /**
   * Reads configurations from the files specified by the extension.
   * <p>
   * If reading configuration from the file fails, the error is logged and such file is ignored.
   *
   * @return {@link IMap}&lt;{@link File},{@link L2ModuleConfigFile}&gt; - map "config file" - "read configuration"
   */
  public IMap<File, L2ModuleConfigFile> readConfigs() {
    IList<File> llFiles = listFiles();
    if( llFiles.isEmpty() ) {
      return IMap.EMPTY;
    }
    IMapEdit<File, L2ModuleConfigFile> mmCfgs = new ElemMap<>();
    IStringListEdit llModuleIds = new StringArrayList();
    for( File f : llFiles ) {
      L2ModuleConfigFile cfg;
      // read configuration
      try {
        cfg = L2ModuleConfigFile.KEEPEER.read( f );
      }
      catch( Exception ex ) {
        logger.error( FMT_ERR_BAD_CFG_FILE_IGNORED, f.getAbsolutePath(), ex.getMessage() );
        continue;
      }
      // check for duplicate IDs
      if( llModuleIds.hasElem( cfg.id() ) ) {
        logger.error( FMT_ERR_DUP_MID_CFG_FILE_IGNORED, f.getAbsolutePath(), cfg.id() );
        continue;
      }
      mmCfgs.put( f, cfg );
      llModuleIds.add( cfg.id() );
    }
    return mmCfgs;
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //

  @Override
  public void doJob() {
    // TODO while not using watcher, check directory changes manually
  }

}

package org.toxsoft.l2.lib.common;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * L2 modules configuration as it is stored in the file.
 * <p>
 * Used to configure HAL device
 *
 * @author hazard157
 * @param id String - the module ID (an IDpath)
 * @param nmName String - mandatory short name
 * @param description String - detailed description
 * @param cfg {@link IAvTree} - module-specific configuration data
 */
public record L2ModuleConfigFile ( String id, String nmName, String description, IAvTree cfg )
    implements IStridable {

  private static final String KW_MODULE_ID = "ModuleId"; //$NON-NLS-1$
  private static final String KW_NAME = "Name"; //$NON-NLS-1$
  private static final String KW_DESCRIPTION = "Description"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<L2ModuleConfigFile> KEEPEER =
      new AbstractEntityKeeper<>( L2ModuleConfigFile.class, EEncloseMode.NOT_IN_PARENTHESES, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, L2ModuleConfigFile aEntity ) {
          boolean saved = aSw.setIndented( true );
          StrioUtils.writeKeywordHeader( aSw, KW_MODULE_ID );
          aSw.writeAsIs( aEntity.id() );
          aSw.writeEol();
          StrioUtils.writeKeywordHeader( aSw, KW_NAME );
          aSw.writeQuotedString( aEntity.nmName() );
          aSw.writeEol();
          StrioUtils.writeKeywordHeader( aSw, KW_DESCRIPTION );
          aSw.writeQuotedString( aEntity.description() );
          aSw.writeEol();
          AvTreeKeeperCompact.KEEPER.write( aSw, aEntity.cfg() );
          aSw.writeEol();
          aSw.setIndented( saved );
        }

        @Override
        protected L2ModuleConfigFile doRead( IStrioReader aSr ) {
          StrioUtils.ensureKeywordHeader( aSr, KW_MODULE_ID );
          String id = aSr.readIdPath();
          StrioUtils.ensureKeywordHeader( aSr, KW_NAME );
          String name = aSr.readQuotedString();
          StrioUtils.ensureKeywordHeader( aSr, KW_DESCRIPTION );
          String description = aSr.readQuotedString();
          IAvTree cfg = AvTreeKeeperCompact.KEEPER.read( aSr );
          return new L2ModuleConfigFile( id, name, description, cfg );
        }
      };

  /**
   * Constructor.
   *
   * @param id String - the module ID (an IDpath)
   * @param nmName String - mandatory short name
   * @param description String - detailed description
   * @param cfg {@link IAvTree} - module-specific configuration data
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an ID path
   * @throws TsIllegalArgumentRtException name is a blank string
   */
  public L2ModuleConfigFile( String id, String nmName, String description, IAvTree cfg ) {
    this.id = StridUtils.checkValidIdPath( id );
    this.nmName = TsErrorUtils.checkNonBlank( nmName );
    TsNullArgumentRtException.checkNulls( description, cfg );
    this.description = description;
    this.cfg = cfg;
  }

}

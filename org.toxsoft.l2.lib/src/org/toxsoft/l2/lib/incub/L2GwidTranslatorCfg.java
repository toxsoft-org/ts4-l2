package org.toxsoft.l2.lib.incub;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Configuration data of the {@link IL2GwidTranslator}.
 *
 * @author hazard157
 * @param id String - the factory ID, one of the {@link IL2GwidTranslatorFactory#id()}
 * @param tree {@link IAvTree} - the configuration data as a tree of atomic values
 */
public record L2GwidTranslatorCfg ( String id, IAvTree tree ) {

  public static final IEntityKeeper<L2GwidTranslatorCfg> KEEPER =
      new AbstractEntityKeeper<>( L2GwidTranslatorCfg.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, L2GwidTranslatorCfg aEntity ) {
          aSw.writeAsIs( aEntity.id );
          aSw.writeSeparatorChar();
          boolean saved = aSw.setIndented( true );
          AvTreeKeeperCompact.KEEPER.write( aSw, aEntity.tree );
          aSw.setIndented( saved );
        }

        @Override
        protected L2GwidTranslatorCfg doRead( IStrioReader aSr ) {
          String id = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          IAvTree tree = AvTreeKeeperCompact.KEEPER.read( aSr );
          return new L2GwidTranslatorCfg( id, tree );
        }
      };

  /**
   * Constructor.
   *
   * @param id String - the factory ID, one of the {@link IL2GwidTranslatorFactory#id()}
   * @param tree {@link IAvTree} - the configuration data as a tree of atomic values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public L2GwidTranslatorCfg( String id, IAvTree tree ) {
    StridUtils.checkValidIdPath( id );
    TsNullArgumentRtException.checkNull( tree );
    this.id = id;
    this.tree = tree;
  }

}

package org.toxsoft.l2.lib.devel;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.rwkind.*;

/**
 * Information about tag.
 *
 * @author hazard157
 * @param id String - the tag ID
 * @param kind {@link ERwKind} - tag kind
 * @param atomicType {@link EAtomicType} - the value type
 * @param params {@link IOptionSet} - additional parameters
 */
public record L2TagInfo ( String id, ERwKind kind, EAtomicType atomicType, IOptionSet params )
    implements IStridableParameterized {

  /**
   * Registsred valobj ID.
   */
  public static final String KEEPER_ID = "L2TagInfo"; //$NON-NLS-1$

  /**
   * The registered keeper.
   */
  public static final IEntityKeeper<L2TagInfo> KEEPER =
      new AbstractEntityKeeper<>( L2TagInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, L2TagInfo aEntity ) {
          aSw.writeAsIs( aEntity.id );
          aSw.writeSeparatorChar();
          aSw.writeAsIs( aEntity.kind.id() );
          aSw.writeSeparatorChar();
          aSw.writeAsIs( aEntity.atomicType.id() );
          aSw.writeSeparatorChar();
          aSw.saveAndIndent( false );
          OptionSetKeeper.KEEPER.write( aSw, aEntity.params );
          aSw.restoreIndent();
        }

        @Override
        protected L2TagInfo doRead( IStrioReader aSr ) {
          String id = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          ERwKind kind = ERwKind.getById( aSr.readIdPath() );
          aSr.ensureSeparatorChar();
          EAtomicType atomicType = EAtomicType.getById( aSr.readIdPath() );
          aSr.ensureSeparatorChar();
          IOptionSet params = OptionSetKeeper.KEEPER.read( aSr );
          return new L2TagInfo( id, kind, atomicType, params );
        }

      };

  /**
   * Constructor.
   *
   * @param id String - the tag ID
   * @param kind {@link ERwKind} - tag kind
   * @param atomicType {@link EAtomicType} - the value type
   * @param params {@link IOptionSet} - additional parameters
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public L2TagInfo( String id, ERwKind kind, EAtomicType atomicType, IOptionSet params ) {
    TsNullArgumentRtException.checkNulls( kind, atomicType, params );
    StridUtils.checkValidIdPath( id );
    this.id = id;
    this.kind = kind;
    this.atomicType = atomicType;
    this.params = new OptionSet( params );
  }

  // ------------------------------------------------------------------------------------
  // IStridableParameterized1
  //

  @Override
  final public String nmName() {
    return DDEF_NAME.getValue( params ).asString();
  }

  @Override
  final public String description() {
    return DDEF_DESCRIPTION.getValue( params ).asString();
  }

}

package org.toxsoft.l2.lib.incub;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Configuration of the recalculator {@link IL2AvDataRecalculator}.
 *
 * @author hazard157
 * @param id String - the factory ID, one of the {@link IL2AvDataRecalculatorFactory#id()}
 * @param params {@link IOptionSet} - the configuration parameters
 */
public record L2AvDataRecalculatorCfg ( String id, IOptionSet params )
    implements IParameterized {

  public static final IEntityKeeper<L2AvDataRecalculatorCfg> KEEPER =
      new AbstractEntityKeeper<>( L2AvDataRecalculatorCfg.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, L2AvDataRecalculatorCfg aEntity ) {
          aSw.writeAsIs( aEntity.id );
          aSw.writeSeparatorChar();
          boolean saved = aSw.setIndented( true );
          OptionSetKeeper.KEEPER.write( aSw, aEntity.params );
          aSw.setIndented( saved );
        }

        @Override
        protected L2AvDataRecalculatorCfg doRead( IStrioReader aSr ) {
          String id = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          IOptionSet params = OptionSetKeeper.KEEPER.read( aSr );
          return new L2AvDataRecalculatorCfg( id, params );
        }
      };

  /**
   * Constructor.
   *
   * @param id String - the factory ID, one of the {@link IL2AvDataRecalculatorFactory#id()}
   * @param params {@link IOptionSet} - the configuration parameters
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public L2AvDataRecalculatorCfg( String id, IOptionSet params ) {
    StridUtils.checkValidIdPath( id );
    TsNullArgumentRtException.checkNull( params );
    this.id = id;
    this.params = params;
  }

}

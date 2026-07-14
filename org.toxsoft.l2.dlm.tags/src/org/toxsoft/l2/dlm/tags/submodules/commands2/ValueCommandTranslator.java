package org.toxsoft.l2.dlm.tags.submodules.commands2;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.l2.dlm.tags.*;
import org.toxsoft.l2.lib.hal.*;

/**
 * Транслятор, подающий на тег указанное в команде значение.
 *
 * @author max
 */
public class ValueCommandTranslator
    implements IDataGwidTranslator {

  private IGwidValueGetter cmdGetter;

  private IGwidValueGetter valGetter;

  private IL2Tag cmdTag;

  private IGwidValueSetter stateSetter;

  private String currCmdInstanceId = null;

  @Override
  public boolean translate( long aTime ) {
    if( currCmdInstanceId == null ) {
      IAtomicValue cmdId = cmdGetter.getGwidValue( aTime );
      if( cmdId != null && cmdId.isAssigned() && cmdId.asString() != null ) {
        currCmdInstanceId = cmdId.asString();

        IAtomicValue paramVal = valGetter.getGwidValue( aTime );

        if( paramVal == null ) {
          // loggerr - error
        }

        setTagBit( cmdTag, -1, paramVal, null );
        stateSetter.setGwidValue( AvUtils.avStr( currCmdInstanceId ), System.currentTimeMillis() );

        currCmdInstanceId = null;

        return true;
      }
    }
    return false;
  }

  public static void setTagBit( IL2Tag aTag, int aBitIndex, IAtomicValue aValue, ILogger aLogger ) {
    if( aBitIndex >= 0 ) {

      int newBitValue = aValue.asBool() ? 1 : 0;

      IAtomicValue currTagValue = aTag.get();
      int currTagValueInt = currTagValue.asInt();

      aLogger.debug( "bitIndex = %d, newBitVal = %d, currTagValue = %d", aBitIndex, newBitValue, currTagValueInt ); //$NON-NLS-1$

      if( currTagValueInt < 0 ) {
        currTagValueInt = Short.toUnsignedInt( (short)currTagValueInt );
        aLogger.debug( "unsigned currTagValue = %d", currTagValueInt );
      }

      int currBitValue = (currTagValueInt >> aBitIndex) % 2;

      int sign = newBitValue - currBitValue;

      int newTagValueInt = currTagValueInt + sign * (1 << aBitIndex);

      aLogger.debug( "currBitValue = %d, sign = %d, newTagValueInt = %d", currBitValue, sign, newTagValueInt ); //$NON-NLS-1$

      aValue = AvUtils.avInt( newTagValueInt );
    }

    aLogger.debug( "Value = %s", aValue.asString() );

    aTag.set( aValue );

    // aLogger.debug( "in do exec isDirty = %s", String.valueOf( aTag.isDirty() ) ); //$NON-NLS-1$
  }

  @Override
  public void config( IAvTree aParams ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void start( IGwidValueSetter[] aDataSetters, IGwidValueGetter[] aDataGetters, IList<IL2Tag> aTags ) {
    // TODO Auto-generated method stub

  }

}

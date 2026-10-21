package org.toxsoft.l2.lib.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.rwkind.*;
import org.toxsoft.l2.lib.api.module.hal.*;

/**
 * {@link IL2Tag} abstract implementation.
 *
 * @author hazard157
 */
public abstract class L2AbstractTag
    implements IL2Tag {

  private final L2AbstractHalDriver driver;
  private final String              id;
  private final IOptionSet          params;
  private final IDataType           dataType;
  private final ERwKind             kind;

  /**
   * Constructor.
   * <p>
   * Note: arguments <code>aParams</code>, <code>aDataType</code> are stored as a reference, ensure these argument are
   * new instances passed to this constructor.
   *
   * @param aId String - the signal ID
   * @param aParams {@link IOptionSet} - {@link #params()} values
   * @param aDataType {@link IDataType} - the signal type
   * @param aKind {@link ERwKind} - the signal R/W kind
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  protected L2AbstractTag( L2AbstractHalDriver aDevice, String aId, IOptionSet aParams, IDataType aDataType,
      ERwKind aKind ) {
    TsNullArgumentRtException.checkNulls( aDevice, aParams, aDataType, aKind );
    id = StridUtils.checkValidIdPath( aId );
    driver = aDevice;
    params = aParams;
    dataType = aDataType;
    kind = aKind;
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  final public String id() {
    return id;
  }

  @Override
  final public String nmName() {
    return params.getStr( DDEF_NAME );
  }

  @Override
  final public String description() {
    return params.getStr( DDEF_DESCRIPTION );
  }

  // ------------------------------------------------------------------------------------
  // IParamaterized
  //

  @Override
  final public IOptionSet params() {
    return params;
  }

  // ------------------------------------------------------------------------------------
  // IL2Tag
  //

  @Override
  final public ERwKind kind() {
    return kind;
  }

  @Override
  final public IDataType dataType() {
    return dataType;
  }

  @Override
  final public IAtomicValue get() {
    TsUnsupportedFeatureRtException.checkFalse( kind.canRead() );
    IAtomicValue value = doGet();
    TsInternalErrorRtException.checkNull( value );
    TsInternalErrorRtException
        .checkFalse( AvTypeCastRtException.canAssign( dataType.atomicType(), value.atomicType() ) );
    return value;
  }

  @Override
  final public void set( IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    TsUnsupportedFeatureRtException.checkFalse( kind.canWrite() );
    AvTypeCastRtException.checkCanAssign( dataType.atomicType(), aValue.atomicType() );
    doSet( aValue );
  }

  @Override
  public L2AbstractHalDriver driver() {
    return driver;
  }

  // ------------------------------------------------------------------------------------
  // To implement/override
  //

  /**
   * Implementation must return input value if applicable.
   * <p>
   * For write-only signals this method may return any value because it is never called.
   *
   * @return {@link IAtomicValue} - read value, must be {@link IAtomicValue#NULL} or of signal type
   */
  protected abstract IAtomicValue doGet();

  /**
   * Implementation must set output value iof applicable.
   * <p>
   * For read-only signals this method is never called.
   *
   * @param aValue {@link IAtomicValue} - value of correct atomic type, never is <code>null</code>
   */
  protected abstract void doSet( IAtomicValue aValue );

}

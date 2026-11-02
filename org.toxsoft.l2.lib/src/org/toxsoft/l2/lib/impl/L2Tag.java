package org.toxsoft.l2.lib.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.rwkind.*;
import org.toxsoft.l2.lib.api.module.hal.*;

/**
 * {@link IL2Tag} final implementation.
 *
 * @author hazard157
 */
final public class L2Tag
    implements IL2Tag, IL2TagForHDLH {

  private final L2AbstractHalDriver driver;
  private final String              id;
  private final ERwKind             kind;
  private final IOptionSet          params;
  private final EAtomicType         atomicType;

  /**
   * Value used in {@link #read()} and {@link #set(IAtomicValue)} methods.
   */
  private IAtomicValue readValue = IAtomicValue.NULL;

  /**
   * Flag is set in {@link #set(IAtomicValue)} and reset in {@link #read()}.
   */
  private boolean isNewReadValue = false;

  /**
   * Value used in {@link #write(IAtomicValue)} and {@link #get()} methods.
   */
  private IAtomicValue writeValue = IAtomicValue.NULL;

  /**
   * Flag is returned by {@link #isNewValueToGet()}, set in {@link #write(IAtomicValue)} and reset in {@link #get()}.
   */
  private boolean isNewWriteValue = false;

  /**
   * Constructor.
   * <p>
   * Note: arguments <code>aParams</code>, <code>aDataType</code> are stored as a reference, ensure these argument are
   * new instances passed to this constructor.
   *
   * @param aDevice {@link L2AbstractHalDriver} - owner component
   * @param aId String - the tag ID
   * @param aKind {@link ERwKind} - the tag kind
   * @param aType {@link EAtomicType} - the tag value type
   * @param aParams {@link IOptionSet} - {@link #params()} values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public L2Tag( L2AbstractHalDriver aDevice, String aId, ERwKind aKind, EAtomicType aType, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aDevice, aKind, aType, aParams );
    id = StridUtils.checkValidIdPath( aId );
    kind = aKind;
    driver = aDevice;
    params = aParams;
    atomicType = aType;
  }

  // ------------------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return params.getStr( DDEF_NAME );
  }

  @Override
  public String description() {
    return params.getStr( DDEF_DESCRIPTION );
  }

  // ------------------------------------------------------------------------------------
  // IParamaterized
  //

  @Override
  public IOptionSet params() {
    return params;
  }

  // ------------------------------------------------------------------------------------
  // IL2Tag
  //

  @Override
  public ERwKind kind() {
    return kind;
  }

  @Override
  public EAtomicType atomicType() {
    return atomicType;
  }

  @Override
  public boolean isNewValueToRead() {
    return isNewReadValue;
  }

  @Override
  public IAtomicValue read() {
    TsUnsupportedFeatureRtException.checkFalse( kind().canRead() );
    isNewReadValue = false;
    return readValue;
  }

  @Override
  public void write( IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    TsUnsupportedFeatureRtException.checkFalse( kind().canWrite() );
    AvTypeCastRtException.checkCanAssign( atomicType, aValue.atomicType() );
    if( !writeValue.equals( aValue ) ) { // TODO do we need this check? or use option to allow this check?
      isNewWriteValue = true;
      writeValue = aValue;
    }
  }

  @Override
  public L2AbstractHalDriver driver() {
    return driver;
  }

  // ------------------------------------------------------------------------------------
  // IL2TagForHDLH
  //

  @Override
  public boolean isNewValueToGet() {
    return isNewWriteValue;
  }

  /**
   * Gets the value written in the tag and resets {@link #isNewValueToGet()} flag.
   *
   * @return {@link IAtomicValue} - value written to the tag
   */
  @Override
  public IAtomicValue get() {
    isNewWriteValue = false;
    return writeValue;
  }

  /**
   * Sets the tag value to be read by the upper part.
   *
   * @param aValue {@link IAtomicValue} - the value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws AvTypeCastRtException value type is not compatible to the tag {@link #atomicType()}
   */
  @Override
  public void set( IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    AvTypeCastRtException.checkCanAssign( atomicType, aValue.atomicType() );
    isNewReadValue = true;
    readValue = aValue;
  }

}

package org.toxsoft.l2.thd.opcua;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.rwkind.*;
import org.toxsoft.l2.lib.impl.*;

public class TagImpl
    extends L2AbstractTag {

  protected TagImpl( L2AbstractHalDevice aDevice, String aId, IOptionSet aParams, IDataType aDataType, ERwKind aKind ) {
    super( aDevice, aId, aParams, aDataType, aKind );
    // TODO Auto-generated constructor stub
  }

  @Override
  protected IAtomicValue doGet() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doSet( IAtomicValue aValue ) {
    // TODO Auto-generated method stub

  }

}

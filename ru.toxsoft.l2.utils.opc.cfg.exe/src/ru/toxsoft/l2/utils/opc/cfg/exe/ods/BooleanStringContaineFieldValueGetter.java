package ru.toxsoft.l2.utils.opc.cfg.exe.ods;

import org.jopendocument.dom.spreadsheet.MutableCell;

public class BooleanStringContaineFieldValueGetter
    extends AbstractFieldValueGetter<Boolean> {

  private String checkFragment;

  public BooleanStringContaineFieldValueGetter( int aColumnNumber, String aCheckFragment ) {
    super( false, aColumnNumber );
    defaultValue = Boolean.FALSE;
    checkFragment = aCheckFragment;
  }

  @Override
  protected boolean isEmpty( MutableCell<?> aCell ) {
    return aCell.isEmpty();
  }

  @Override
  protected Boolean getValue( MutableCell<?> aCell ) {
    if( !(aCell.getValue() instanceof String) ) {
      return Boolean.FALSE;
    }
    return ((String)aCell.getValue()).contains( checkFragment );
  }

}

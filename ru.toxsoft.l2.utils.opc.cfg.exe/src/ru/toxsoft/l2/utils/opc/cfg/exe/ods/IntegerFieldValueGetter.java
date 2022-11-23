package ru.toxsoft.l2.utils.opc.cfg.exe.ods;

import java.math.*;

import org.jopendocument.dom.spreadsheet.*;

public class IntegerFieldValueGetter
    extends AbstractFieldValueGetter<Integer> {

  public IntegerFieldValueGetter( boolean aIsErrorWhenEmpty, int aColumnNumber, Integer aDefaultValue ) {
    super( aIsErrorWhenEmpty, aColumnNumber );
    defaultValue = aDefaultValue;
  }

  public IntegerFieldValueGetter( int aColumnNumber, Integer aDefaultValue ) {
    super( false, aColumnNumber );
    defaultValue = aDefaultValue;
  }

  public IntegerFieldValueGetter( int aColumnNumber ) {
    super( false, aColumnNumber );
    defaultValue = 0;
  }

  public IntegerFieldValueGetter( boolean aIsErrorWhenEmpty, int aColumnNumber ) {
    super( aIsErrorWhenEmpty, aColumnNumber );
    defaultValue = 0;
  }

  @Override
  protected boolean isEmpty( MutableCell<?> aCell ) {
    return aCell.isEmpty() || (aCell.getValue() instanceof String && ((String)aCell.getValue()).trim().length() == 0);
  }

  @Override
  protected Integer getValue( MutableCell<?> aCell ) {
    if( aCell.getValue() instanceof BigDecimal ) {
      return ((BigDecimal)aCell.getValue()).intValue();
    }
    else
      if( aCell.getValue() instanceof String ) {
        String strValue = ((String)aCell.getValue()).trim();
        if( strValue.trim().length() > 0 ) {
          return Integer.valueOf( strValue );
        }
      }
    return null;
  }

}

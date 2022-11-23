package ru.toxsoft.l2.utils.opc.cfg.exe.ods;

import org.jopendocument.dom.spreadsheet.MutableCell;

public class StringFieldValueGetter extends AbstractFieldValueGetter<String> {

  public StringFieldValueGetter( boolean aIsErrorWhenEmpty, int aColumnNumber, String aDefaultValue ) {
    super( aIsErrorWhenEmpty, aColumnNumber );
    defaultValue = aDefaultValue;
  }
  
  public StringFieldValueGetter(  int aColumnNumber, String aDefaultValue ) {
    super( false, aColumnNumber );
    defaultValue = aDefaultValue;
  }
  
  public StringFieldValueGetter(  int aColumnNumber ) {
    super( false, aColumnNumber );
    defaultValue = "";
  }
  
  public StringFieldValueGetter(boolean aIsErrorWhenEmpty,  int aColumnNumber ) {
    super( aIsErrorWhenEmpty, aColumnNumber );
    defaultValue = "";
  }

  @Override
  protected boolean isEmpty( MutableCell<?> aCell ) {
    return aCell.isEmpty();
  }

  @Override
  protected String getValue( MutableCell<?> aCell ) {
    if(!(aCell.getValue() instanceof String)){
      return "";
    }
    return ((String)aCell.getValue()).trim();
  }

}

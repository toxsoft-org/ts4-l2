package ru.toxsoft.l2.utils.opc.cfg.exe.ods;

import org.jopendocument.dom.spreadsheet.*;

public class ConstFieldValueGetter<T>
    implements IFieldValueGetter<T> {

  private T constValue;

  public ConstFieldValueGetter( T aConstValue ) {
    super();
    constValue = aConstValue;
  }

  @Override
  public T getValue( Sheet aSheet, int aRowNumber ) {
    return constValue;
  }

}

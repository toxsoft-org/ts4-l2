package ru.toxsoft.l2.utils.opc.cfg.exe.ods;

import org.jopendocument.dom.spreadsheet.MutableCell;
import org.jopendocument.dom.spreadsheet.Sheet;

public abstract class AbstractFieldValueGetter<T>
    implements IFieldValueGetter<T> {

  private boolean isErrorWhenEmpty = false;

  private int columnNumber;

  protected T defaultValue;

  public AbstractFieldValueGetter( boolean isErrorWhenEmpty, int columnNumber ) {
    super();
    this.isErrorWhenEmpty = isErrorWhenEmpty;
    this.columnNumber = columnNumber;
  }

  @Override
  public T getValue( Sheet aSheet, int aRowNumber ) {
    MutableCell<?> cell = aSheet.getCellAt( columnNumber, aRowNumber );

    if( isEmpty( cell ) ) {
      if( isErrorWhenEmpty ) {
        throw new IllegalArgumentException();
      }
      else {
        return defaultValue;
      }
    }

    return getValue( cell );
  }

  protected abstract boolean isEmpty( MutableCell<?> aCell );

  protected abstract T getValue( MutableCell<?> aCell );

}

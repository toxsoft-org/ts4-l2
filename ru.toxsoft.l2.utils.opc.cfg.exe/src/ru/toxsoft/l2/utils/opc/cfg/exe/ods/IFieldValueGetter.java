package ru.toxsoft.l2.utils.opc.cfg.exe.ods;

import org.jopendocument.dom.spreadsheet.Sheet;

public interface IFieldValueGetter<T> {

  T getValue(Sheet aSheet, int aRowNumber);

}

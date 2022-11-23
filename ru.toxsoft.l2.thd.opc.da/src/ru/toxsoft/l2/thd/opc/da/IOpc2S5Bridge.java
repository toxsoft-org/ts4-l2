/**
 *
 */
package ru.toxsoft.l2.thd.opc.da;

import ru.toxsoft.l2.thd.opc.*;

/**
 * API для мостов OPC реализованных на разных движках (JEasyOpc, openSCADA и т.д.)
 *
 * @author Dima
 */
interface IOpc2S5Bridge
    extends ITsOpc {

  void readValuesFromLL();

  void writeValuesOnLL();

  void putInBufferOutputValues();

  void getFromBufferInputValues();

  void closeApparatResources();

}

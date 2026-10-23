package org.toxsoft.l2.thd.s7.gui;

import org.toxsoft.core.tslib.av.*;

import ru.toxsoft.l2.thd.opc.*;

/**
 * Описание одного тега S7
 *
 * @author dima
 */
public class GuiS7Tag {

  private GuiS7DataBlock dataBlock;
  public int             bufferOffset = 0;
  public EAtomicType     valueType    = EAtomicType.FLOATING;
  public int             bitNumber    = 0;                   // только для типа Boolean
  public EKind           tagKind      = EKind.R;

  /**
   * @param aGuiS7DataBlock parent DB
   * @param aBufferOffset - offset in DB buffer
   * @param aValueType - type of value
   * @param aBitNumber - bit number
   * @param aTagKind - R/W/RW
   */
  public GuiS7Tag( GuiS7DataBlock aGuiS7DataBlock, int aBufferOffset, EAtomicType aValueType, int aBitNumber,
      EKind aTagKind ) {
    dataBlock = aGuiS7DataBlock;
    bufferOffset = aBufferOffset;
    valueType = aValueType;
    tagKind = aTagKind;
  }

  /**
   * @return id
   */
  @SuppressWarnings( "nls" )
  public String getId() {
    String ID_FMT_STR = "%s.tagOffset_%d.valType_%s";
    String retVal = String.format( ID_FMT_STR, dataBlock.getId(), Integer.valueOf( bufferOffset ), valueType.id() );
    return retVal;
  }

  /**
   * @return string
   */
  @Override
  public String toString() {
    return String.format( "%s : %d", dataBlock.getId(), Integer.valueOf( bufferOffset ) );
  }

}

package org.toxsoft.l2.thd.s7.gui;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.utils.*;

/**
 * Описание одного блока данных S7
 *
 * @author dima
 */
public class GuiS7DataBlock {

  private GuiS7Device guiS7Device = GuiS7Device.DEFAULT_DEVICE;

  private int dbNumber;

  private String params = TsLibUtils.EMPTY_STRING;

  private IListEdit<GuiS7Tag> tags = new ElemArrayList<>();

  /**
   * @param aGuiS7Device - S7 Device {@link GuiS7Device}
   * @param aDBNumber - number of DB
   * @param aIsOutput - output DB
   */
  public GuiS7DataBlock( GuiS7Device aGuiS7Device, int aDBNumber ) {
    guiS7Device = aGuiS7Device;
    dbNumber = aDBNumber;
  }

  /**
   * @return id
   */
  @SuppressWarnings( "nls" )
  public String getId() {
    String deviceID = guiS7Device.getDeviceId();
    String ID_FMT_STR = "%s.dataBlock%d";
    String retVal = String.format( ID_FMT_STR, deviceID, Integer.valueOf( dbNumber ) );
    return retVal;
  }

  /**
   * @return S7 Device {@link GuiS7Device}
   */
  public GuiS7Device getS7Device() {
    return guiS7Device;
  }

  /**
   * Set new S7 Device
   *
   * @param aGuiS7Device {@link GuiS7Device}
   */
  public void setS7Device( GuiS7Device aGuiS7Device ) {
    guiS7Device = aGuiS7Device;
  }

  /**
   * @return DB number
   */
  public int getDBNumber() {
    return dbNumber;
  }

  /**
   * Set new DB number
   *
   * @param aDBNumber - DB number
   */
  public void setDBNumber( int aDBNumber ) {
    dbNumber = aDBNumber;
  }

  /**
   * Returns parameters of node as string
   *
   * @return String - parameters of node as string
   */
  public String getParams() {
    return params;
  }

  /**
   * Sets params of node as string
   *
   * @param aParams - params of node as strin
   */
  public void setParams( String aParams ) {
    params = aParams;
  }

  /**
   * @return string
   */
  @SuppressWarnings( "boxing" )
  @Override
  public String toString() {
    return String.format( "%s : %d", guiS7Device.getDeviceId(), dbNumber );
  }

  public IList<GuiS7Tag> getTags() {
    return tags;
  }

  public void addTag( GuiS7Tag aTag ) {
    tags.add( aTag );
  }

}

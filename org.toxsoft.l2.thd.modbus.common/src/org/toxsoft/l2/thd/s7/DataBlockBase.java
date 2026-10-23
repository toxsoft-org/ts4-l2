/**
 *
 */
package org.toxsoft.l2.thd.s7;

import static org.toxsoft.l2.thd.s7.IS7ThdConstants.*;

import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;

import ru.toxsoft.l2.thd.opc.*;

import Moka7.*;

/**
 * Базовый функционал работы с одним блоком данных S7. <br>
 *
 * @author dima
 */
abstract public class DataBlockBase {

  /**
   * S7 server API
   */
  protected final S7Client s7Server;

  /**
   * Data block number
   */
  protected final int dbNumber;

  /**
   * buffer for data to read from S7 server
   */
  protected byte[] dataBuffer;

  /**
   * buffer size (in bytes)
   */
  protected int bufferSize = 2; // initial value - minimum usefull size

  /**
   * Tags of that data block.
   */
  protected IStringMapEdit<ITag> dbTags = new StringMap<>();

  /**
   * Constructor.
   *
   * @param aS7Server S7 server API
   * @param aBlockNumber number of block in S7
   */
  public DataBlockBase( S7Client aS7Server, int aBlockNumber ) {
    s7Server = aS7Server;
    dbNumber = aBlockNumber;
  }

  /**
   * Read description of tag in that block. Create tag reader and tune paramters to read whole data block.
   *
   * @param aTagDefinition - {@link IAvTree} container with metainfo about one tag of that data block
   */
  abstract void addTagDefinition( IAvTree aTagDefinition );

  void configYourself( IAvTree aDbParams ) {
    IAvTree tagsArray = aDbParams.nodes().findByKey( DATA_BLOCK_TAGS );

    for( int i = 0; i < tagsArray.arrayLength(); i++ ) {
      IAvTree tagParams = tagsArray.arrayElement( i );
      addTagDefinition( tagParams );
    }
  }

  int dbNumber() {
    return dbNumber;
  }

  IStringMap<? extends ITag> getTags() {
    return dbTags;
  }
}

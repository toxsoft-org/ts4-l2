package org.toxsoft.l2.thd.s7;

/**
 * Constants for S7 driver.
 *
 * @author dima
 */
@SuppressWarnings( { "nls", "javadoc" } )
public interface IS7ThdConstants {

  String S7_DEVICES_PARAM_ID = "s7.devices";
  String IP_ID               = "ip";
  String RACK_ID             = "rack";
  String SLOT_ID             = "slot";
  String READ_DATA_BLOCKS    = "read.data.blocks";
  String WRITE_DATA_BLOCKS   = "write.data.blocks";
  String DATA_BLOCK_NUM      = "datablock.num";
  String DATA_BLOCK_TAGS     = "datablock.tags";
  String TAG_ID              = "tag.id";
  String TAG_BUFFER_OFFSET   = "tag.buffer.offset";
  String TAG_VALUE_TYPE_ID   = "tag.value.type.id";
  String BIT_NUMBER          = "bit.number";
}

package org.toxsoft.l2.thd.s7;

/**
 * Constants for S7 driver.
 *
 * @author dima
 */
@SuppressWarnings( { "nls", "javadoc" } )
public interface IS7ThdConstants {

  String S7_DEVICES_PARAM_ID = "s7.devices";

  String DEVICE_DEF_FORMAT     = "device.%s.def";
  String DEVICE_ID             = "device.id";
  String DEVICE_IP_ADDRESS     = "IP";
  String DEVICE_RACK           = "rack";
  String DEVICE_SLOT           = "slot";
  String DATABLOCKS_ARRAY_NAME = "data.blocks";

  String DB_DEF_FORMAT      = "db.%s.def";
  String DB_ID              = "db.id";
  String DB_NUM             = "db.num";
  String DB_TAGS_ARRAY_NAME = "db.tags";

  String TAG_DEF_FORMAT    = "tag.%s.def";
  String TAG_ID            = "tag.id";
  String TAG_BUFFER_OFFSET = "tag.buffer.offset";
  String TAG_VALUE_TYPE_ID = "tag.value.type.id";
  String BIT_NUMBER        = "bit.number";
  String TAG_KIND_ID       = "tag.kind";         // «R\W\RW»

}

package ru.toxsoft.l2.utils.opc.cfg.exe;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;

/**
 * Описание параметров из закладки "класс"
 *
 * @author max
 */
public interface ISysClassRowDef {

  IDataDef CLASS_TAG_ID_PARAM = create( "class.tag.id", STRING, TSID_DESCRIPTION, "Class+Tag Identificator", TSID_NAME,
      "class tag id", TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef CLASS_ID_PARAM = create( "class.id", STRING, TSID_DESCRIPTION, "Class Identificator", TSID_NAME, "class id",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( true ) );

  IDataDef TAG_NAME_PARAM = create( "tag.name", STRING, TSID_DESCRIPTION, "Tag Name", TSID_NAME, "tag name",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( true ) );

  IDataDef BIT_INDEX_PARAM = create( "bit.index", INTEGER, TSID_DESCRIPTION, "Bit Index", TSID_NAME, "bit index",
      TSID_DEFAULT_VALUE, avInt( -1 ), TSID_IS_MANDATORY, avBool( false ) );

  IDataDef SYNCH_TYPE_PARAM = create( "synch.type", STRING, TSID_DESCRIPTION, "Synch Type", TSID_NAME, "synch type",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef DATA_ID_PARAM = create( "data.id", STRING, TSID_DESCRIPTION, "Data Id", TSID_NAME, "data id",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef EVENT_ID_PARAM = create( "event.id", STRING, TSID_DESCRIPTION, "Event Id", TSID_NAME, "event id",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef EVENT_ON_MESSAGE_PARAM = create( "event.on.message", STRING, TSID_DESCRIPTION, "Event On Message", TSID_NAME,
      "event on message", TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef EVENT_OFF_MESSAGE_PARAM = create( "event.off.message", STRING, TSID_DESCRIPTION, "Event Off Message",
      TSID_NAME, "event off message", TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef EVENT_ON_PARAM = create( "event.on", BOOLEAN, TSID_DESCRIPTION, "Event On", TSID_NAME, "event on",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef EVENT_OFF_PARAM = create( "event.off", BOOLEAN, TSID_DESCRIPTION, "Event Off", TSID_NAME, "event off",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef COMMAND_ID_PARAM = create( "cmd.id", STRING, TSID_DESCRIPTION, "Command Id", TSID_NAME, "cmd id",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef IS_WRITE_PARAM = create( "is.write", BOOLEAN, TSID_DESCRIPTION, "Is Write", TSID_NAME, "is write",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef IS_READ_PARAM = create( "is.read", BOOLEAN, TSID_DESCRIPTION, "Is Read", TSID_NAME, "is read",
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, TSID_IS_MANDATORY, avBool( false ) );

  IDataDef VAL_TYPE_PARAM = create( "val.type", STRING, TSID_DESCRIPTION, "Value Type", TSID_NAME, "val type",
      TSID_DEFAULT_VALUE, avStr( "Integer" ), TSID_IS_MANDATORY, avBool( true ) );

}

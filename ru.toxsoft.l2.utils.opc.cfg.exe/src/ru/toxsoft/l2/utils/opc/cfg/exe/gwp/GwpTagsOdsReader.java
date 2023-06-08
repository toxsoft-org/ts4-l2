package ru.toxsoft.l2.utils.opc.cfg.exe.gwp;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static ru.toxsoft.l2.utils.opc.cfg.exe.ISysClassRowDef.*;

import java.io.*;
import java.util.*;

import org.jopendocument.dom.spreadsheet.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.*;
import ru.toxsoft.l2.utils.opc.cfg.exe.ods.*;

public class GwpTagsOdsReader
    extends CommonOdsFileReader {

  protected IFieldValueGetter<String> CLASS_ID_COLUMN = new StringFieldValueGetter( 0 );

  protected IFieldValueGetter<String> OBJ_NAME_COLUMN = new StringFieldValueGetter( 1 );

  protected IFieldValueGetter<String> DATA_ID_COLUMN = new StringFieldValueGetter( 2 );

  protected IFieldValueGetter<String> TAG_NAME_COLUMN = new StringFieldValueGetter( 4 );

  protected IFieldValueGetter<String> TAG_FULL_PATH_COLUMN = new StringFieldValueGetter( 6 );

  protected TagTypeFieldValueGetter TAG_TYPE_COLUMN = new TagTypeFieldValueGetter( 7 );

  protected IFieldValueGetter<String> TAG_RAW_TYPE_COLUMN = new StringFieldValueGetter( 7, "" );

  private List<IOptionSet> optSetRows = new ArrayList<>();

  public GwpTagsOdsReader( File odsFile, String aSheetName ) {
    super( odsFile, aSheetName );
  }

  @Override
  protected void readRow( Sheet aSheet, int aRowNumber ) {

    IOptionSet prevOptSet = optSetRows.size() == 0 ? null : optSetRows.get( optSetRows.size() - 1 );

    IOptionSetEdit newOpt = new OptionSet();

    String classId = CLASS_ID_COLUMN.getValue( aSheet, aRowNumber );
    String objName = OBJ_NAME_COLUMN.getValue( aSheet, aRowNumber );
    String dataId = DATA_ID_COLUMN.getValue( aSheet, aRowNumber );
    String tagName = TAG_NAME_COLUMN.getValue( aSheet, aRowNumber );
    String tagFullPath = TAG_FULL_PATH_COLUMN.getValue( aSheet, aRowNumber );
    ETagValueType valType = TAG_TYPE_COLUMN.getValue( aSheet, aRowNumber );
    String vaRawType = TAG_RAW_TYPE_COLUMN.getValue( aSheet, aRowNumber );

    if( classId.trim().length() == 0 ) {
      return;
    }

    CLASS_ID_PARAM.setValue( newOpt, avStr( classId ) );
    OBJ_NAME_PARAM.setValue( newOpt, avStr( objName ) );
    DATA_ID_PARAM.setValue( newOpt, avStr( dataId ) );
    TAG_NAME_PARAM.setValue( newOpt, avStr( tagName ) );
    TAG_PATH_PARAM.setValue( newOpt, avStr( tagFullPath ) );

    VAL_TYPE_PARAM.setValue( newOpt, avStr( valType.getName() ) );
    VAL_RAW_TYPE_PARAM.setValue( newOpt, avStr( vaRawType ) );

    optSetRows.add( newOpt );
  }

  public List<IOptionSet> getAllRows() {
    return optSetRows;
  }

}

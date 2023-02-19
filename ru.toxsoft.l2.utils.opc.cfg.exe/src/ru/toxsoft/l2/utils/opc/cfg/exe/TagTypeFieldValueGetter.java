package ru.toxsoft.l2.utils.opc.cfg.exe;

import java.math.*;

import org.jopendocument.dom.spreadsheet.*;

import ru.toxsoft.l2.utils.opc.cfg.exe.ods.*;

public class TagTypeFieldValueGetter
    extends AbstractFieldValueGetter<ETagValueType> {

  public TagTypeFieldValueGetter( int columnNumber ) {
    super( false, columnNumber );
    defaultValue = ETagValueType.INTEGER;
  }

  @Override
  protected boolean isEmpty( MutableCell<?> aCell ) {
    return aCell.isEmpty() || (aCell.getValue() instanceof String && ((String)aCell.getValue()).trim().length() == 0);
  }

  @Override
  protected ETagValueType getValue( MutableCell<?> aCell ) {
    String strValue = ETagValueType.INTEGER.getName();
    Object value = aCell.getValue();
    if( value instanceof BigDecimal ) {
      int typeInt = ((BigDecimal)value).intValue();
      if( typeInt == 2 || typeInt == 3 || typeInt == 12 ) {
        strValue = ETagValueType.INTEGER.getName();
      }
      if( typeInt == 4 ) {
        strValue = ETagValueType.FLOAT.getName();
        // 11, 13
      }
    }
    else
      if( value instanceof String ) {
        String typeStr = ((String)value).trim();

        if( typeStr.toLowerCase().contains( "int" ) ) {
          return ETagValueType.INTEGER;
        }
        else
          if( typeStr.toLowerCase().contains( "float" ) || typeStr.toLowerCase().contains( "real" ) ) {
            return ETagValueType.FLOAT;
          }
          else
            if( typeStr.toLowerCase().contains( "bool" ) ) {
              return ETagValueType.BOOLEAN;
            }
            else
              if( typeStr.toLowerCase().contains( "str" ) ) {
                return ETagValueType.STRING;
              }

        if( typeStr.equals( "B" ) ) {
          strValue = ETagValueType.BOOLEAN.getName();
        }
      }

    return ETagValueType.searchTypeByName( strValue );
  }

}

package ru.toxsoft.l2.dlm.opc_bridge.submodules.rri;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.gw.gwid.*;

import ru.toxsoft.l2.core.util.*;

/**
 * Класс, расширяющий класс описания данного для работы с НСИ атрибутом.
 *
 * @author dima
 */
public class RriDataObjNameExtension
    extends DataObjName {

  private String rriSectionId;

  /**
   * Конструктор по параметрам инициализации, содержащихся в наборе.
   *
   * @param aInitParams {@link IOptionSet} - набор параметров инициализации.
   */
  public RriDataObjNameExtension( IOptionSet aInitParams ) {
    super( aInitParams.getStr( CLASS_ID ), aInitParams.getStr( OBJ_NAME ), aInitParams.getStr( RRI_ATTR_ID ) );
    rriSectionId = aInitParams.getStr( RRI_SECTION_ID );
  }

  /**
   * @return Возвращает id секции НСИ
   */
  public String rriSectionId() {
    return rriSectionId;
  }

  /**
   * @return Gwid НСИ`шного атрибута
   */
  public Gwid convertToRriGwid() {
    return Gwid.createAttr( getClassId(), getObjName(), getDataId() );
  }

}

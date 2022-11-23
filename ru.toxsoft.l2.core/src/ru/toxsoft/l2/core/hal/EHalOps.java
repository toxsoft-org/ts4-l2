package ru.toxsoft.l2.core.hal;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static ru.toxsoft.l2.core.hal.IL2Resources.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;

/**
 * Параметры настройки программы HAL.
 * <p>
 * Значения параметров доступны через {@link IHal#options()}.
 * <p>
 *
 * @author MAX
 */
@SuppressWarnings( "nls" )
public interface EHalOps {

  /**
   * Максимальное типичное время СЧИТЫВАНИЯ с физ. уровня по всем аппаратам, для которых это время следует
   * контролировать.
   */
  IDataDef MAX_TYPICAL_READ_DEVICE_TIME = create( "TypicalReadDeviceTime", INTEGER, TSID_DESCRIPTION,
      E_HO_MAX_READ_DEV_T, TSID_NAME, E_HO_N_MAX_READ_DEV_T, TSID_DEFAULT_VALUE, avInt( 10 ) );

  /**
   * Максимальное типичное время ЗАПИСИ на физ. уровень по всем аппаратам, для которых это время следует контролировать.
   */
  IDataDef MAX_TYPICAL_WRITE_DEVICE_TIME = create( "TypicalWriteDeviceTime", INTEGER, TSID_DESCRIPTION,
      E_HO_MAX_WRITE_DEV_T, TSID_NAME, E_HO_N_MAX_WRITE_DEV_T, TSID_DEFAULT_VALUE, avInt( 10 ) );

  OptionSet optionSet = new OptionSet();

  /**
   * Возвращает набор всех опции в виде {@link IOptionSet}.
   *
   * @return IOptionSet - набор опции компоненты по умолчанию
   */
  static IOptionSet asOptionSet() {
    if( optionSet.size() == 0 ) {
      MAX_TYPICAL_READ_DEVICE_TIME.setValue( optionSet, MAX_TYPICAL_READ_DEVICE_TIME.defaultValue() );
      MAX_TYPICAL_WRITE_DEVICE_TIME.setValue( optionSet, MAX_TYPICAL_WRITE_DEVICE_TIME.defaultValue() );
    }
    return optionSet;
  }

}

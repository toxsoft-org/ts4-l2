package org.toxsoft.l2.lib.hal.devices;

import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Один вход-выход устройства (одна ножка устройства)
 *
 * @author max
 */
public interface IPin
    extends IStridable {

  /**
   * Тип входа-выхода
   *
   * @return EPinKind - тип входа-выхода.
   */
  EPinKind kind();
}

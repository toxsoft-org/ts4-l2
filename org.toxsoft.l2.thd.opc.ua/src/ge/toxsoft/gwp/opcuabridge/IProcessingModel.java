package ge.toxsoft.gwp.opcuabridge;

import org.toxsoft.core.tslib.av.avtree.*;

public interface IProcessingModel {

  void config( IAvTree aCfgInfo );

  void start();

  void job();

  void stop();

  boolean isStopped();
}

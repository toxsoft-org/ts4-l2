package ru.toxsoft.l2.dlm.opc_bridge.submodules.commands;

import static ru.toxsoft.l2.dlm.opc_bridge.IDlmsBaseConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * Импульсная команда, подающая на пин значение 1 в течение некоторого времени. Время по умолчанию задаётся в файле
 * конфигурации, если в пришедшей команде указано время, то используется оно.
 *
 * @author max
 */
public class PulseCommandExec
    extends AbstractOneTagCommandExec {

  /**
   * Журнал работы
   */
  static ILogger logger = LoggerWrapper.getLogger( PulseCommandExec.class );

  private long startTime = 0;

  private long pulseDuration;

  private long defaultPulseDuration = 1000L;

  @Override
  protected void doExecCommand( IDtoCommand aCmd, long aTime ) {

    // установка длительности импульса
    if( aCmd.argValues().hasValue( DURATION_PARAM_NAME ) ) {
      IAtomicValue duration = aCmd.argValues().getValue( DURATION_PARAM_NAME );
      pulseDuration = duration.asLong();
    }
    else {
      pulseDuration = defaultPulseDuration;
    }

    tag.set( AvUtils.avBool( true ) );
    // TODO
    // Dima, for debug
    logger.info( "Pulse tag id %s = TRUE", tag.id() ); //$NON-NLS-1$

    startTime = System.currentTimeMillis();
  }

  @Override
  public void doJob( long aTime ) {
    if( startTime > 0 ) {
      if( System.currentTimeMillis() > startTime + pulseDuration ) {
        tag.set( AvUtils.avBool( false ) );
        startTime = 0;
      }
    }

  }

}
